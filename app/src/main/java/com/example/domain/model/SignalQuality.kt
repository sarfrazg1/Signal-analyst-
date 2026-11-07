package com.example.domain.model

enum class SignalQuality(
    val englishLabel: String,
    val urduLabel: String,
    val colorHex: Long,
    val barCount: Int,
    val englishDescription: String,
    val urduDescription: String
) {
    EXCELLENT(
        englishLabel = "Excellent",
        urduLabel = "بہترین",
        colorHex = 0xFF2E7D32, // Green
        barCount = 5,
        englishDescription = "Strongest signal. Optimal for 4K streaming, low-latency gaming, and HD voice calls.",
        urduDescription = "بہترین سگنل۔ ویڈیو اسٹریمنگ اور بغیر تعطل انٹرنیٹ کے لیے انتہائی موزوں۔"
    ),
    GOOD(
        englishLabel = "Good",
        urduLabel = "اچھا",
        colorHex = 0xFF43A047, // Light Green
        barCount = 4,
        englishDescription = "Strong signal. Seamless HD video, web browsing and voice calling.",
        urduDescription = "اچھا سگنل۔ تیز رفتار براؤزنگ اور ایچ ڈی کالز کے لیے موزوں۔"
    ),
    FAIR(
        englishLabel = "Fair",
        urduLabel = "مناسب",
        colorHex = 0xFFFBC02D, // Amber / Yellow
        barCount = 3,
        englishDescription = "Moderate signal. Standard web browsing and standard definition media.",
        urduDescription = "درمیانہ سگنل۔ عام انٹرنیٹ اور سوشل میڈیا استعمال کے لیے کافی۔"
    ),
    POOR(
        englishLabel = "Poor",
        urduLabel = "کمزور",
        colorHex = 0xFFF57C00, // Orange
        barCount = 2,
        englishDescription = "Weak signal. Potential buffering, slower data rates, possible call drops indoors.",
        urduDescription = "کمزور سگنل۔ انٹرنیٹ میں تاخیر اور کال منقطع ہونے کا خدشہ۔"
    ),
    VERY_POOR(
        englishLabel = "Very Poor",
        urduLabel = "انتہائی کمزور",
        colorHex = 0xFFE53935, // Red
        barCount = 1,
        englishDescription = "Critical signal edge. Severe packet loss, high latency, barely connected.",
        urduDescription = "نہایت کمزور سگنل۔ ڈیٹا کنکشن میں شدید رکاوٹ۔"
    ),
    NO_SIGNAL(
        englishLabel = "No Signal",
        urduLabel = "کوئی سگنل نہیں",
        colorHex = 0xFF757575, // Grey
        barCount = 0,
        englishDescription = "Out of cellular range or disconnected from tower.",
        urduDescription = "کوئی سگنل موصول نہیں ہو رہا۔ کوریج سے باہر۔"
    ),
    UNKNOWN(
        englishLabel = "Unknown / Awaiting",
        urduLabel = "نامعلوم",
        colorHex = 0xFF9E9E9E,
        barCount = 0,
        englishDescription = "Radio measurements pending or unavailable.",
        urduDescription = "سگنل پیمائش دستیاب نہیں ہے۔"
    );

    companion object {
        /**
         * Standard 3GPP LTE RSRP classification:
         * Excellent: >= -80 dBm
         * Good: -81 to -90 dBm
         * Fair: -91 to -100 dBm
         * Poor: -101 to -115 dBm
         * Very Poor: < -115 dBm
         */
        fun fromLteRsrp(rsrp: Int?): SignalQuality {
            if (rsrp == null || rsrp == Int.MAX_VALUE || rsrp == 0) return UNKNOWN
            return when {
                rsrp >= -80 -> EXCELLENT
                rsrp >= -90 -> GOOD
                rsrp >= -100 -> FAIR
                rsrp >= -115 -> POOR
                rsrp > -140 -> VERY_POOR
                else -> NO_SIGNAL
            }
        }

        /**
         * Generic dBm classification for 2G/3G or legacy cells:
         * Excellent: >= -70 dBm
         * Good: -71 to -85 dBm
         * Fair: -86 to -100 dBm
         * Poor: -101 to -110 dBm
         * Very Poor: < -110 dBm
         */
        fun fromDbm(dbm: Int?): SignalQuality {
            if (dbm == null || dbm == Int.MAX_VALUE || dbm == 0) return UNKNOWN
            return when {
                dbm >= -70 -> EXCELLENT
                dbm >= -85 -> GOOD
                dbm >= -100 -> FAIR
                dbm >= -110 -> POOR
                dbm > -140 -> VERY_POOR
                else -> NO_SIGNAL
            }
        }

        /**
         * Comprehensive classification using RSRP if available, falling back to dBm.
         */
        fun evaluate(rsrp: Int?, dbm: Int?): SignalQuality {
            return if (rsrp != null && rsrp != Int.MAX_VALUE && rsrp < 0) {
                fromLteRsrp(rsrp)
            } else if (dbm != null && dbm != Int.MAX_VALUE && dbm < 0) {
                fromDbm(dbm)
            } else {
                UNKNOWN
            }
        }
    }
}
