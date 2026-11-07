package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.SpeedTestRecord

@Entity(tableName = "speed_test_results")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val operatorName: String,
    val operatorBrand: String,
    val mccMnc: String,
    val simSlotIndex: Int,
    val networkGeneration: String,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val packetLossPercent: Double,
    val signalDbm: Int?,
    val signalRsrp: Int?,
    val isWifi: Boolean
) {
    fun toDomain(): SpeedTestRecord {
        return SpeedTestRecord(
            id = id,
            timestamp = timestamp,
            operatorName = operatorName,
            operatorBrand = operatorBrand,
            mccMnc = mccMnc,
            simSlotIndex = simSlotIndex,
            networkGeneration = networkGeneration,
            downloadMbps = downloadMbps,
            uploadMbps = uploadMbps,
            pingMs = pingMs,
            jitterMs = jitterMs,
            packetLossPercent = packetLossPercent,
            signalDbm = signalDbm,
            signalRsrp = signalRsrp,
            isWifi = isWifi
        )
    }

    companion object {
        fun fromDomain(record: SpeedTestRecord): SpeedTestResultEntity {
            return SpeedTestResultEntity(
                id = record.id,
                timestamp = record.timestamp,
                operatorName = record.operatorName,
                operatorBrand = record.operatorBrand,
                mccMnc = record.mccMnc,
                simSlotIndex = record.simSlotIndex,
                networkGeneration = record.networkGeneration,
                downloadMbps = record.downloadMbps,
                uploadMbps = record.uploadMbps,
                pingMs = record.pingMs,
                jitterMs = record.jitterMs,
                packetLossPercent = record.packetLossPercent,
                signalDbm = record.signalDbm,
                signalRsrp = record.signalRsrp,
                isWifi = record.isWifi
            )
        }
    }
}
