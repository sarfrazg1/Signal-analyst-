package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.SignalDiagnosticDao
import com.example.data.local.dao.SpeedTestResultDao
import com.example.data.local.entity.SignalDiagnosticEntity
import com.example.data.local.entity.SpeedTestResultEntity
import com.example.data.speedtest.SpeedTestEngine
import com.example.data.telephony.TelephonyDataSource
import com.example.domain.model.PakistaniOperator
import com.example.domain.model.SignalDiagnosticRecord
import com.example.domain.model.SignalReading
import com.example.domain.model.SimComparisonStats
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.TelephonyStatus
import com.example.domain.repository.SignalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.round

class SignalRepositoryImpl(
    private val context: Context,
    private val telephonyDataSource: TelephonyDataSource,
    private val diagnosticDao: SignalDiagnosticDao,
    private val speedTestResultDao: SpeedTestResultDao,
    private val speedTestEngine: SpeedTestEngine
) : SignalRepository {

    override fun observeSignal(slotIndex: Int): Flow<SignalReading?> {
        return telephonyDataSource.observeRealSignal(slotIndex)
    }

    override fun observeSimSlots(): Flow<List<SimSlotInfo>> {
        return telephonyDataSource.observeSimSlots()
    }

    override fun observeTelephonyStatus(): Flow<TelephonyStatus> {
        return telephonyDataSource.observeTelephonyStatus()
    }

    override suspend fun refresh() {
        // Handled reactively
    }

    override suspend fun saveDiagnostic(reading: SignalReading, note: String?): Long {
        val entity = SignalDiagnosticEntity(
            timestamp = System.currentTimeMillis(),
            operatorName = reading.operatorName,
            operatorBrand = reading.operator.brandName,
            mccMnc = reading.mccMncString,
            networkGeneration = reading.networkGeneration,
            simSlotIndex = reading.simSlotIndex,
            dbm = reading.rawDbm,
            rsrp = reading.lteRsrp,
            rsrq = reading.lteRsrq,
            sinr = reading.lteSinr,
            qualityLabel = reading.quality.englishLabel,
            cellId = reading.cellId,
            pci = reading.pci,
            tac = reading.tac,
            note = note
        )
        return diagnosticDao.insertDiagnostic(entity)
    }

    override fun getSavedDiagnostics(): Flow<List<SignalDiagnosticRecord>> {
        return diagnosticDao.getAllDiagnostics().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteDiagnostic(id: Long) {
        diagnosticDao.deleteDiagnostic(id)
    }

    override suspend fun clearDiagnostics() {
        diagnosticDao.clearAll()
    }

    // Phase 2 implementations
    override fun runSpeedTest(
        slotIndex: Int,
        simSlots: List<SimSlotInfo>,
        currentSignal: SignalReading?
    ): Flow<SpeedTestProgress> {
        return speedTestEngine.runSpeedTest(slotIndex, simSlots, currentSignal)
    }

    override suspend fun saveSpeedTest(record: SpeedTestRecord): Long {
        return speedTestResultDao.insertResult(SpeedTestResultEntity.fromDomain(record))
    }

    override fun observeSpeedTests(): Flow<List<SpeedTestRecord>> {
        return speedTestResultDao.getAllResults().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deleteSpeedTest(id: Long) {
        speedTestResultDao.deleteResult(id)
    }

    override suspend fun clearSpeedTests() {
        speedTestResultDao.clearAll()
    }

    override fun observeComparisonStats(): Flow<List<SimComparisonStats>> {
        return combine(
            speedTestResultDao.getAllResults(),
            diagnosticDao.getAllDiagnostics()
        ) { speedEntities, diagEntities ->
            val speedRecords = speedEntities.map { it.toDomain() }
            val diagRecords = diagEntities.map { it.toDomain() }

            // Group by operator brand
            val operatorGroups = speedRecords.groupBy { it.operatorBrand }

            operatorGroups.map { (brand, records) ->
                val operator = PakistaniOperator.fromMccMnc(
                    records.firstOrNull()?.mccMnc?.takeIf { it.contains("-") }?.substringBefore("-"),
                    records.firstOrNull()?.mccMnc?.takeIf { it.contains("-") }?.substringAfter("-"),
                    brand
                )

                val avgDown = round(records.map { it.downloadMbps }.average() * 10) / 10.0
                val maxDown = round((records.maxOfOrNull { it.downloadMbps } ?: 0.0) * 10) / 10.0
                val avgUp = round(records.map { it.uploadMbps }.average() * 10) / 10.0
                val avgPing = round(records.map { it.pingMs }.average() * 10) / 10.0
                val avgJitter = round(records.map { it.jitterMs }.average() * 10) / 10.0
                val avgLoss = round(records.map { it.packetLossPercent }.average() * 10) / 10.0

                // Match RF diagnostics for this operator
                val matchingDiags = diagRecords.filter { it.operatorBrand == brand || it.operatorName.contains(brand, ignoreCase = true) }
                val validDbms = matchingDiags.mapNotNull { it.dbm } + records.mapNotNull { it.signalDbm }
                val validRsrps = matchingDiags.mapNotNull { it.rsrp } + records.mapNotNull { it.signalRsrp }

                val avgDbm = if (validDbms.isNotEmpty()) validDbms.average().toInt() else null
                val avgRsrp = if (validRsrps.isNotEmpty()) validRsrps.average().toInt() else null

                // Quality score 0..100 based on RSRP (-130 to -65 dBm)
                val targetDbm = avgRsrp ?: avgDbm
                val qualityScore = when {
                    targetDbm == null -> 50
                    targetDbm >= -75 -> 95
                    targetDbm >= -85 -> 85
                    targetDbm >= -95 -> 70
                    targetDbm >= -105 -> 50
                    targetDbm >= -115 -> 30
                    else -> 15
                }

                SimComparisonStats(
                    operator = operator,
                    operatorBrand = brand,
                    simSlotIndex = records.firstOrNull()?.simSlotIndex ?: 0,
                    totalTests = records.size,
                    avgDownloadMbps = avgDown,
                    maxDownloadMbps = maxDown,
                    avgUploadMbps = avgUp,
                    avgPingMs = avgPing,
                    avgJitterMs = avgJitter,
                    avgPacketLossPercent = avgLoss,
                    avgSignalDbm = avgDbm,
                    avgSignalRsrp = avgRsrp,
                    signalQualityScorePercent = qualityScore
                )
            }.sortedByDescending { it.avgDownloadMbps }
        }
    }

    override suspend fun generateCsvExport(records: List<SpeedTestRecord>): File = withContext(Dispatchers.IO) {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(reportsDir, "pak_signal_speed_report_$timestamp.csv")

        FileWriter(file).use { writer ->
            // Header
            writer.append("ID,Timestamp,Date_Time,Operator,Brand,MCC_MNC,SIM_Slot,Network_Type,Download_Mbps,Upload_Mbps,Ping_ms,Jitter_ms,Packet_Loss_Percent,Signal_dBm,Signal_RSRP_dBm,Is_WiFi\n")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            for (rec in records) {
                val dateStr = dateFormat.format(Date(rec.timestamp))
                writer.append("${rec.id},")
                writer.append("${rec.timestamp},")
                writer.append("\"$dateStr\",")
                writer.append("\"${rec.operatorName}\",")
                writer.append("\"${rec.operatorBrand}\",")
                writer.append("\"${rec.mccMnc}\",")
                writer.append("${rec.simSlotIndex + 1},")
                writer.append("\"${rec.networkGeneration}\",")
                writer.append("${rec.downloadMbps},")
                writer.append("${rec.uploadMbps},")
                writer.append("${rec.pingMs},")
                writer.append("${rec.jitterMs},")
                writer.append("${rec.packetLossPercent},")
                writer.append("${rec.signalDbm ?: ""},")
                writer.append("${rec.signalRsrp ?: ""},")
                writer.append("${rec.isWifi}\n")
            }
            writer.flush()
        }

        file
    }
}
