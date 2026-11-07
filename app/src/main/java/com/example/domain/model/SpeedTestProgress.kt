package com.example.domain.model

data class SpeedTestProgress(
    val state: SpeedTestState = SpeedTestState.IDLE,
    val currentPingMs: Double? = null,
    val currentJitterMs: Double? = null,
    val currentPacketLossPercent: Double? = null,
    val currentDownloadMbps: Double = 0.0,
    val currentUploadMbps: Double = 0.0,
    val progressPercent: Float = 0f,
    val bytesTransferred: Long = 0L,
    val errorMessage: String? = null,
    val completedRecord: SpeedTestRecord? = null
)
