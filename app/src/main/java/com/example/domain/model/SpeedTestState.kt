package com.example.domain.model

enum class SpeedTestState {
    IDLE,
    CONSENT_REQUIRED,
    PREPARING,
    PINGING,
    DOWNLOADING,
    UPLOADING,
    COMPLETED,
    CANCELLED,
    ERROR
}
