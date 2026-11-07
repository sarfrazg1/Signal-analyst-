package com.example.domain.model

data class SimComparisonStats(
    val operator: PakistaniOperator,
    val operatorBrand: String,
    val simSlotIndex: Int,
    val totalTests: Int,
    val avgDownloadMbps: Double,
    val maxDownloadMbps: Double,
    val avgUploadMbps: Double,
    val avgPingMs: Double,
    val avgJitterMs: Double,
    val avgPacketLossPercent: Double,
    val avgSignalDbm: Int?,
    val avgSignalRsrp: Int?,
    val signalQualityScorePercent: Int // 0 to 100 based on RF quality
)
