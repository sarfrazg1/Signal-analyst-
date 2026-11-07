package com.example.domain.model

data class SignalReading(
    val operator: PakistaniOperator,
    val operatorName: String,
    val mcc: String?,
    val mnc: String?,
    val networkGeneration: String, // "5G NR", "4G LTE", "3G UMTS/HSPA", "2G GSM", "No Service", "Unknown"
    val networkTypeDetails: String, // Technical spec e.g. "LTE", "NR", "HSPAP", "EDGE"
    val rawDbm: Int?,              // e.g. -87 dBm
    val lteRsrp: Int?,             // LTE Reference Signal Received Power in dBm (-140 to -44)
    val lteRsrq: Int?,             // LTE Reference Signal Received Quality in dB (-20 to -3)
    val lteSinr: Double?,          // Signal-to-Interference-plus-Noise Ratio in dB (-20 to +30)
    val lteCqi: Int?,              // Channel Quality Indicator (1 to 15)
    val lteTimingAdvance: Int?,    // Timing advance distance metric
    val cellId: Long?,             // E-UTRAN Cell Identifier (CID / eNodeB ID)
    val pci: Int?,                 // Physical Cell ID (0 - 503)
    val tac: Int?,                 // Tracking Area Code
    val earfcn: Int?,              // E-UTRA Absolute Radio Frequency Channel Number
    val quality: SignalQuality,
    val simSlotIndex: Int,
    val subscriptionId: Int?,
    val isRegistered: Boolean,
    val isRoaming: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isActualHardwareReading: Boolean = true
) {
    val mccMncString: String
        get() {
            return if (!mcc.isNullOrBlank() && !mnc.isNullOrBlank()) {
                "$mcc-$mnc"
            } else if (!mcc.isNullOrBlank()) {
                mcc
            } else {
                "N/A"
            }
        }

    val primaryMetricValue: String
        get() {
            return if (lteRsrp != null && lteRsrp != Int.MAX_VALUE && lteRsrp < 0) {
                "$lteRsrp dBm"
            } else if (rawDbm != null && rawDbm != Int.MAX_VALUE && rawDbm < 0) {
                "$rawDbm dBm"
            } else {
                "-- dBm"
            }
        }
}
