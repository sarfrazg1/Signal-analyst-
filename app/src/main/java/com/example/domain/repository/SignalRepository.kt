package com.example.domain.repository

import com.example.domain.model.SignalDiagnosticRecord
import com.example.domain.model.SignalReading
import com.example.domain.model.SimComparisonStats
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.TelephonyStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

interface SignalRepository {
    fun observeSignal(slotIndex: Int): Flow<SignalReading?>
    fun observeSimSlots(): Flow<List<SimSlotInfo>>
    fun observeTelephonyStatus(): Flow<TelephonyStatus>
    suspend fun refresh()
    suspend fun saveDiagnostic(reading: SignalReading, note: String? = null): Long
    fun getSavedDiagnostics(): Flow<List<SignalDiagnosticRecord>>
    suspend fun deleteDiagnostic(id: Long)
    suspend fun clearDiagnostics()

    // Phase 2: Speed test & SIM comparison
    fun runSpeedTest(slotIndex: Int, simSlots: List<SimSlotInfo>, currentSignal: SignalReading?): Flow<SpeedTestProgress>
    suspend fun saveSpeedTest(record: SpeedTestRecord): Long
    fun observeSpeedTests(): Flow<List<SpeedTestRecord>>
    suspend fun deleteSpeedTest(id: Long)
    suspend fun clearSpeedTests()
    fun observeComparisonStats(): Flow<List<SimComparisonStats>>
    suspend fun generateCsvExport(records: List<SpeedTestRecord>): File
}
