package com.example.domain.model

data class SpeedTestRecord(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
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
)
