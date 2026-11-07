package com.example.domain.model

/**
 * Simplified 4-level quality classification designed for non-technical users.
 * Colors:
 * - Green (Best)
 * - Blue (Normal)
 * - Orange (Weak)
 * - Red (Unavailable)
 */
enum class SimpleQualityLevel(
    val colorHex: Long,
    val bgHex: Long
) {
    BEST(0xFF2E7D32, 0xFFE8F5E9),         // Green (#2E7D32)
    NORMAL(0xFF1976D2, 0xFFE3F2FD),       // Blue (#1976D2)
    WEAK(0xFFF57C00, 0xFFFFF3E0),         // Orange (#F57C00)
    UNAVAILABLE(0xFFD32F2F, 0xFFFFEBEE)   // Red (#D32F2F)
}

data class SimpleQualityStatus(
    val level: SimpleQualityLevel,
    val urduLabel: String,
    val englishLabel: String,
    val urduExplanation: String,
    val englishExplanation: String
)

object SimpleQualityEvaluator {

    /**
     * Evaluates Call Quality strictly based on genuine cellular radio signal measurements.
     */
    fun evaluateCallQuality(
        reading: SignalReading?,
        status: TelephonyStatus
    ): SimpleQualityStatus {
        if (status == TelephonyStatus.AIRPLANE_MODE ||
            status == TelephonyStatus.NO_SIM ||
            status == TelephonyStatus.NO_SIGNAL ||
            reading == null
        ) {
            return SimpleQualityStatus(
                level = SimpleQualityLevel.UNAVAILABLE,
                urduLabel = "دستیاب نہیں",
                englishLabel = "Unavailable",
                urduExplanation = "کال سروس دستیاب نہیں۔",
                englishExplanation = "Call service is currently unavailable."
            )
        }

        // Use LTE RSRP if available, otherwise raw dBm (GSM/3G/LTE)
        val lteRsrp = reading.lteRsrp
        val rawDbm = reading.rawDbm

        return if (lteRsrp != null && lteRsrp != Int.MAX_VALUE && lteRsrp < 0) {
            when {
                lteRsrp >= -90 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.BEST,
                    urduLabel = "بہترین",
                    englishLabel = "Best",
                    urduExplanation = "کال کے لیے بہترین سگنل — آرام سے بات ہو سکتی ہے۔",
                    englishExplanation = "Excellent signal for calls — Clear and comfortable conversation."
                )
                lteRsrp >= -105 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.NORMAL,
                    urduLabel = "نارمل",
                    englishLabel = "Normal",
                    urduExplanation = "کال کے لیے نارمل سگنل — عام طور پر بات ہو سکتی ہے۔",
                    englishExplanation = "Normal signal for calls — Standard calling quality."
                )
                lteRsrp > -130 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.WEAK,
                    urduLabel = "کمزور",
                    englishLabel = "Weak",
                    urduExplanation = "کال کا سگنل کمزور — آواز کٹ سکتی ہے یا کال متاثر ہو سکتی ہے۔",
                    englishExplanation = "Weak call signal — Voice may break or call may drop."
                )
                else -> SimpleQualityStatus(
                    level = SimpleQualityLevel.UNAVAILABLE,
                    urduLabel = "دستیاب نہیں",
                    englishLabel = "Unavailable",
                    urduExplanation = "کال سروس دستیاب نہیں۔",
                    englishExplanation = "Call service is currently unavailable."
                )
            }
        } else if (rawDbm != null && rawDbm != Int.MAX_VALUE && rawDbm < 0) {
            when {
                rawDbm >= -80 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.BEST,
                    urduLabel = "بہترین",
                    englishLabel = "Best",
                    urduExplanation = "کال کے لیے بہترین سگنل — آرام سے بات ہو سکتی ہے۔",
                    englishExplanation = "Excellent signal for calls — Clear and comfortable conversation."
                )
                rawDbm >= -95 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.NORMAL,
                    urduLabel = "نارمل",
                    englishLabel = "Normal",
                    urduExplanation = "کال کے لیے نارمل سگنل — عام طور پر بات ہو سکتی ہے۔",
                    englishExplanation = "Normal signal for calls — Standard calling quality."
                )
                rawDbm > -125 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.WEAK,
                    urduLabel = "کمزور",
                    englishLabel = "Weak",
                    urduExplanation = "کال کا سگنل کمزور — آواز کٹ سکتی ہے یا کال متاثر ہو سکتی ہے۔",
                    englishExplanation = "Weak call signal — Voice may break or call may drop."
                )
                else -> SimpleQualityStatus(
                    level = SimpleQualityLevel.UNAVAILABLE,
                    urduLabel = "دستیاب نہیں",
                    englishLabel = "Unavailable",
                    urduExplanation = "کال سروس دستیاب نہیں۔",
                    englishExplanation = "Call service is currently unavailable."
                )
            }
        } else {
            SimpleQualityStatus(
                level = SimpleQualityLevel.UNAVAILABLE,
                urduLabel = "دستیاب نہیں",
                englishLabel = "Unavailable",
                urduExplanation = "کال سروس دستیاب نہیں۔",
                englishExplanation = "Call service is currently unavailable."
            )
        }
    }

    /**
     * Evaluates Internet Quality separately from Call Quality.
     * Respects actual speed test measurements if present; otherwise evaluates genuine cellular data bearer.
     */
    fun evaluateInternetQuality(
        reading: SignalReading?,
        status: TelephonyStatus,
        latestSpeedTest: SpeedTestRecord?
    ): SimpleQualityStatus {
        if (status == TelephonyStatus.AIRPLANE_MODE ||
            status == TelephonyStatus.NO_SIM ||
            status == TelephonyStatus.NO_SIGNAL ||
            reading == null
        ) {
            return SimpleQualityStatus(
                level = SimpleQualityLevel.UNAVAILABLE,
                urduLabel = "دستیاب نہیں",
                englishLabel = "Unavailable",
                urduExplanation = "فی الحال انٹرنیٹ کنکشن دستیاب نہیں۔",
                englishExplanation = "No internet connection currently available."
            )
        }

        // If a real speed test was conducted, evaluate based on measured throughput and ping
        if (latestSpeedTest != null && latestSpeedTest.downloadMbps > 0) {
            val download = latestSpeedTest.downloadMbps
            val ping = latestSpeedTest.pingMs

            return when {
                download >= 10.0 || (download >= 5.0 && ping < 60) -> SimpleQualityStatus(
                    level = SimpleQualityLevel.BEST,
                    urduLabel = "بہترین",
                    englishLabel = "Best",
                    urduExplanation = "انٹرنیٹ بہترین ہے — ویڈیو، واٹس ایپ اور براؤزنگ اچھی چلیں گی۔",
                    englishExplanation = "Internet is excellent — Fast video, WhatsApp, and browsing."
                )
                download >= 2.0 -> SimpleQualityStatus(
                    level = SimpleQualityLevel.NORMAL,
                    urduLabel = "نارمل",
                    englishLabel = "Normal",
                    urduExplanation = "انٹرنیٹ نارمل ہے — واٹس ایپ اور عام براؤزنگ چل جائے گی۔",
                    englishExplanation = "Internet is normal — Good for WhatsApp and general browsing."
                )
                else -> SimpleQualityStatus(
                    level = SimpleQualityLevel.WEAK,
                    urduLabel = "کمزور",
                    englishLabel = "Weak",
                    urduExplanation = "انٹرنیٹ کمزور ہے — ویڈیو رک سکتی ہے اور صفحات دیر سے کھلیں گے۔",
                    englishExplanation = "Internet is weak — Video may buffer and pages load slowly."
                )
            }
        }

        // When speed test has not yet been executed, evaluate genuine radio network generation & signal
        val gen = reading.networkGeneration.uppercase()
        val rsrp = reading.lteRsrp
        val rawDbm = reading.rawDbm

        return when {
            gen.contains("5G") || (gen.contains("4G") && ((rsrp != null && rsrp >= -95) || (rawDbm != null && rawDbm >= -85))) -> {
                SimpleQualityStatus(
                    level = SimpleQualityLevel.BEST,
                    urduLabel = "بہترین",
                    englishLabel = "Best",
                    urduExplanation = "انٹرنیٹ بہترین ہے — ویڈیو، واٹس ایپ اور براؤزنگ اچھی چلیں گی۔",
                    englishExplanation = "Internet is excellent — Fast video, WhatsApp, and browsing."
                )
            }
            gen.contains("4G") || (gen.contains("3G") && (rawDbm != null && rawDbm >= -95)) -> {
                SimpleQualityStatus(
                    level = SimpleQualityLevel.NORMAL,
                    urduLabel = "نارمل",
                    englishLabel = "Normal",
                    urduExplanation = "انٹرنیٹ نارمل ہے — واٹس ایپ اور عام براؤزنگ چل جائے گی۔",
                    englishExplanation = "Internet is normal — Good for WhatsApp and general browsing."
                )
            }
            gen.contains("2G") || (rsrp != null && rsrp < -110) || (rawDbm != null && rawDbm < -105) -> {
                SimpleQualityStatus(
                    level = SimpleQualityLevel.WEAK,
                    urduLabel = "کمزور",
                    englishLabel = "Weak",
                    urduExplanation = "انٹرنیٹ کمزور ہے — ویڈیو رک سکتی ہے اور صفحات دیر سے کھلیں گے۔",
                    englishExplanation = "Internet is weak — Video may buffer and pages load slowly."
                )
            }
            else -> {
                SimpleQualityStatus(
                    level = SimpleQualityLevel.NORMAL,
                    urduLabel = "نارمل",
                    englishLabel = "Normal",
                    urduExplanation = "انٹرنیٹ نارمل ہے — واٹس ایپ اور عام براؤزنگ چل جائے گی۔",
                    englishExplanation = "Internet is normal — Good for WhatsApp and general browsing."
                )
            }
        }
    }
}
