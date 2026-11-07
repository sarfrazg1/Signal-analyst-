package com.example.domain.engine

import com.example.domain.model.DiagnosticCategory
import com.example.domain.model.DiagnosticReport
import com.example.domain.model.DiagnosticSeverity
import com.example.domain.model.SignalQuality
import com.example.domain.model.SignalReading
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.TelephonyStatus

object DiagnosticEngine {

    fun analyze(
        currentSignal: SignalReading?,
        latestSpeedTest: SpeedTestRecord?,
        telephonyStatus: TelephonyStatus,
        activeSim: SimSlotInfo?
    ): DiagnosticReport {
        val evidence = mutableMapOf<String, String>()

        // Collect authentic evidence metrics
        currentSignal?.let { sig ->
            evidence["Operator"] = sig.operator.brandName
            evidence["Network"] = sig.networkGeneration
            sig.rawDbm?.let { evidence["Raw Signal"] = "$it dBm" }
            sig.lteRsrp?.let { evidence["LTE RSRP"] = "$it dBm" }
            sig.lteRsrq?.let { evidence["LTE RSRQ"] = "$it dB" }
            sig.lteSinr?.let { evidence["LTE SINR"] = String.format("%.1f dB", it) }
            sig.cellId?.let { evidence["Cell ID (CI)"] = "$it" }
            sig.pci?.let { evidence["Physical Cell ID (PCI)"] = "$it" }
            sig.tac?.let { evidence["TAC"] = "$it" }
        }

        latestSpeedTest?.let { sp ->
            evidence["Download Speed"] = "${sp.downloadMbps} Mbps"
            evidence["Upload Speed"] = "${sp.uploadMbps} Mbps"
            evidence["Ping (Latency)"] = "${sp.pingMs.toInt()} ms"
            evidence["Jitter"] = "${sp.jitterMs} ms"
            evidence["Packet Loss"] = "${sp.packetLossPercent}%"
        }

        // Rule 1: Hardware & System States
        when (telephonyStatus) {
            TelephonyStatus.AIRPLANE_MODE -> {
                return DiagnosticReport(
                    category = DiagnosticCategory.HARDWARE_OR_SIM,
                    severity = DiagnosticSeverity.CRITICAL,
                    titleEn = "Airplane Mode Is Enabled",
                    titleUr = "ہوائی جہاز موڈ آن ہے",
                    explanationEn = "The device baseband cellular radio transceiver is powered down by system settings. No signals can be transmitted or received.",
                    explanationUr = "سیلولر ریڈیو ٹرانسیور سسٹم سیٹنگز کی وجہ سے بند ہے۔ سگنل موصول نہیں ہو سکتے۔",
                    recommendationsEn = listOf(
                        "Swipe down the quick settings shade and turn OFF Airplane Mode.",
                        "Allow 5-10 seconds for the modem to authenticate with the nearest tower."
                    ),
                    recommendationsUr = listOf(
                        "کوئیک سیٹنگز سے ہوائی جہاز موڈ بند کریں۔",
                        "قریبی ٹاور سے کنکشن بحال ہونے کے لیے 5 سے 10 سیکنڈ انتظار کریں۔"
                    ),
                    evidenceMetrics = evidence
                )
            }
            TelephonyStatus.NO_SIM -> {
                return DiagnosticReport(
                    category = DiagnosticCategory.HARDWARE_OR_SIM,
                    severity = DiagnosticSeverity.CRITICAL,
                    titleEn = "No SIM Card Identified",
                    titleUr = "کوئی سم کارڈ موجود نہیں",
                    explanationEn = "No active subscriber identity module was recognized in the cellular tray. Cellular services are unavailable.",
                    explanationUr = "فون کے سم سلاٹ میں کوئی فعال سم کارڈ موجود نہیں ہے، جس کی وجہ سے نیٹ ورک غیر فعال ہے۔",
                    recommendationsEn = listOf(
                        "Insert an active Pakistani SIM card (Jazz, Zong, Ufone, Telenor or SCOM).",
                        "Ensure the gold SIM contacts are clean and properly aligned in the SIM tray."
                    ),
                    recommendationsUr = listOf(
                        "فون میں فعال پاکستانی سم کارڈ (جاز، زونگ، یوفون، ٹیلی نار یا ایس کام) داخل کریں۔",
                        "یقینی بنائیں کہ سم کارڈ کی چپ صاف اور درست پوزیشن میں لگی ہے۔"
                    ),
                    evidenceMetrics = evidence
                )
            }
            TelephonyStatus.PERMISSION_REQUIRED -> {
                return DiagnosticReport(
                    category = DiagnosticCategory.HARDWARE_OR_SIM,
                    severity = DiagnosticSeverity.CRITICAL,
                    titleEn = "Telephony Permissions Required",
                    titleUr = "ٹیلی فون کی اجازت درکار ہے",
                    explanationEn = "Android restricts direct hardware access to cellular radio telemetry without explicit Location and Phone State permissions.",
                    explanationUr = "اینڈرائڈ پر سگنل اور ٹاور کی درست پیمائش کے لیے لوکیشن اور فون کی اجازت ضروری ہے۔",
                    recommendationsEn = listOf(
                        "Tap 'Grant Required Permissions' on the dashboard.",
                        "Allow 'Precise Location' while the app is in use to read serving tower identifiers."
                    ),
                    recommendationsUr = listOf(
                        "ڈیش بورڈ پر 'اجازتیں فراہم کریں' کا بٹن دبائیں۔",
                        "سیل ٹاور کی تفصیلات جانچنے کے لیے لوکیشن کی اجازت فراہم کریں۔"
                    ),
                    evidenceMetrics = evidence
                )
            }
            TelephonyStatus.NO_SIGNAL -> {
                return buildNoCoverageReport(evidence)
            }
            else -> {}
        }

        if (currentSignal == null || currentSignal.quality == SignalQuality.NO_SIGNAL) {
            return buildNoCoverageReport(evidence)
        }

        // Rule 2: Roaming Mode
        if (currentSignal.isRoaming) {
            return DiagnosticReport(
                category = DiagnosticCategory.ROAMING_MODE,
                severity = DiagnosticSeverity.MODERATE,
                titleEn = "Network Roaming Active",
                titleUr = "نیٹ ورک رومنگ فعال ہے",
                explanationEn = "Your device is registered on a visited partner cellular network rather than your home carrier's direct infrastructure.",
                explanationUr = "آپ کا فون ہوم نیٹ ورک کے بجائے کسی پارٹنر آپریٹر کے ٹاور پر رومنگ موڈ میں کام کر رہا ہے۔",
                recommendationsEn = listOf(
                    "Verify that 'Data Roaming' is toggled ON in Android SIM Settings if internet data is needed.",
                    "Check network roaming charges with your carrier."
                ),
                recommendationsUr = listOf(
                    "اگر انٹرنیٹ درکار ہو تو اینڈرائڈ سیٹنگز سے ڈیٹا رومنگ آن رکھیں۔",
                    "اپنے موبائل آپریٹر سے رومنگ ٹیرف کی تفصیلات معلوم کریں۔"
                ),
                evidenceMetrics = evidence
            )
        }

        // Rule 3: High RF Interference (Adequate RSRP but poor SINR / RSRQ)
        val rsrp = currentSignal.lteRsrp
        val sinr = currentSignal.lteSinr
        val rsrq = currentSignal.lteRsrq

        if (rsrp != null && rsrp >= -98 && ((sinr != null && sinr < 2.0) || (rsrq != null && rsrq <= -14))) {
            return DiagnosticReport(
                category = DiagnosticCategory.HIGH_INTERFERENCE,
                severity = DiagnosticSeverity.MODERATE,
                titleEn = "High RF Signal Interference",
                titleUr = "شدید ریڈیو سگنل مداخلت اور شور",
                explanationEn = "The received radio power (RSRP: $rsrp dBm) is sufficient, but signal purity is heavily degraded by interference (SINR: ${sinr?.let { "$it dB" } ?: "Low"}). This typically occurs on cell border boundaries where multiple neighboring sectors overlap, or inside buildings with high electromagnetic reflection.",
                explanationUr = "سگنل پاور مناسب ہے مگر سگنل کی شفافیت (SINR) شدید مداخلت کی وجہ سے خراب ہے۔ یہ مسئلہ اکثر دو سیل ٹاورز کے درمیانی بارڈر یا انڈور ریفلیکشن سے پیدا ہوتا ہے۔",
                recommendationsEn = listOf(
                    "Move away from heavy concrete walls, metallic cladding, or electronic transformers.",
                    "Step near an exterior window to secure a direct Line-of-Sight to the serving sector.",
                    "If indoors, utilize Wi-Fi Calling (VoWiFi) to avoid call drops."
                ),
                recommendationsUr = listOf(
                    "کنکریٹ کی موٹی دیواروں اور برقی آلات سے تھوڑا فاصلہ رکھیں۔",
                    "کھڑکی کے قریب جائیں تاکہ ٹاور سے براہ راست سگنل مل سکے۔",
                    "اگر کالز منقطع ہوں تو وائی فائی کالنگ کا استعمال کریں۔"
                ),
                evidenceMetrics = evidence
            )
        }

        // Rule 4: Weak Coverage / Path Loss (Cell Edge)
        if ((rsrp != null && rsrp < -108) || (currentSignal.rawDbm != null && currentSignal.rawDbm < -108)) {
            val signalVal = rsrp ?: currentSignal.rawDbm ?: -110
            return DiagnosticReport(
                category = DiagnosticCategory.WEAK_COVERAGE,
                severity = DiagnosticSeverity.CRITICAL,
                titleEn = "Weak Tower Signal (Cell Edge)",
                titleUr = "انتہائی کمزور ٹاور سگنل (سیل بارڈر)",
                explanationEn = "Cellular radio signal level is very weak ($signalVal dBm). Signal attenuation occurs naturally due to distance from base stations, physical terrain barriers (hills, valleys), or heavy building construction materials.",
                explanationUr = "موبائل سگنل انتہائی کمزور ہے ($signalVal dBm)۔ یہ مسئلہ ٹاور سے زیادہ فاصلے، پہاڑی رکاوٹوں یا عمارت کے اندرونی ڈھانچے کے باعث پیش آتا ہے۔",
                recommendationsEn = listOf(
                    "Move to an elevated floor or step outdoors toward open sky.",
                    "Toggle Airplane Mode ON and OFF for 5 seconds to prompt the baseband to re-scan for closer microcells.",
                    "Switch preferred network to '3G/2G auto' as lower frequency bands penetrate thick walls more effectively."
                ),
                recommendationsUr = listOf(
                    "کھلی فضا یا عمارت کی بالائی منزل کی جانب جائیں۔",
                    "ہوائی جہاز موڈ 5 سیکنڈ کے لیے آن کر کے دوبارہ بند کریں تاکہ فون قریبی ٹاور سے جڑے۔",
                    "اگر 4G کام نہ کرے تو وقتی طور پر 3G کا انتخاب کریں۔"
                ),
                evidenceMetrics = evidence
            )
        }

        // Rule 5: Base Station Congestion (Good signal but restricted bandwidth)
        if (rsrp != null && rsrp >= -95 && (sinr == null || sinr >= 6.0) &&
            latestSpeedTest != null && latestSpeedTest.downloadMbps < 3.0 && !latestSpeedTest.isWifi
        ) {
            return DiagnosticReport(
                category = DiagnosticCategory.TOWER_CONGESTION,
                severity = DiagnosticSeverity.MODERATE,
                titleEn = "Base Station Traffic Congestion",
                titleUr = "سیل ٹاور پر صارفین کا زیادہ لوڈ",
                explanationEn = "Your RF link to the cell tower is healthy, but download bandwidth is restricted (${latestSpeedTest.downloadMbps} Mbps). This indicates that the serving cellular sector is experiencing heavy subscriber demand or backhaul carrier throttling.",
                explanationUr = "آپ کا ٹاور سے ریڈیو سگنل اچھا ہے لیکن انٹرنیٹ اسپیڈ کم ہے (${latestSpeedTest.downloadMbps} Mbps)۔ یہ ٹاور پر زیادہ ٹریفک یا بینڈوڈتھ کی کمی ظاہر کرتا ہے۔",
                recommendationsEn = listOf(
                    "Network congestion is often transient; retry data-intensive tasks during off-peak hours.",
                    "Toggle mobile data off and on to prompt handover to a different carrier frequency (EARFCN).",
                    "If dual SIM is available, test your secondary operator line."
                ),
                recommendationsUr = listOf(
                    "کچھ دیر بعد دوبارہ چیک کریں جب ٹاور پر لوڈ کم ہو۔",
                    "موبائل ڈیٹا ایک بار بند کر کے دوبارہ آن کریں۔",
                    "اگر فون میں دوسری سم موجود ہے تو اس کا انٹرنیٹ ٹیسٹ کریں۔"
                ),
                evidenceMetrics = evidence
            )
        }

        // Rule 6: Legacy Network Fallback (2G / 3G)
        if (currentSignal.networkGeneration.contains("2G") || currentSignal.networkGeneration.contains("3G")) {
            return DiagnosticReport(
                category = DiagnosticCategory.LEGACY_FALLBACK,
                severity = DiagnosticSeverity.MODERATE,
                titleEn = "Connected via Legacy 2G/3G Network",
                titleUr = "پرانے 2G/3G نیٹ ورک پر کنکشن",
                explanationEn = "Your connection has fallen back from 4G LTE to legacy frequencies (${currentSignal.networkGeneration}). Legacy cellular channels offer basic voice connectivity but deliver higher latency and slower data rates.",
                explanationUr = "آپ کا فون 4G کے بجائے پرانے 2G/3G نیٹ ورک پر منتقل ہو چکا ہے جس میں انٹرنیٹ کی رفتار سست ہوتی ہے۔",
                recommendationsEn = listOf(
                    "Check Android SIM Settings and confirm that 'Preferred network type' is set to 5G/4G/LTE (Auto).",
                    "Move towards an open area with 4G LTE signal reception."
                ),
                recommendationsUr = listOf(
                    "اینڈرائڈ سیٹنگز میں چیک کریں کہ پریفرڈ نیٹ ورک 4G/LTE پر سیٹ ہو۔",
                    "کسی ایسے مقام کی طرف جائیں جہاں 4G سگنل دستیاب ہو۔"
                ),
                evidenceMetrics = evidence
            )
        }

        // Default: Optimal Cellular Link
        return DiagnosticReport(
            category = DiagnosticCategory.EXCELLENT_LINK,
            severity = DiagnosticSeverity.OPTIMAL,
            titleEn = "Optimal Cellular Radio Link",
            titleUr = "بہترین سیلولر ریڈیو کارکردگی",
            explanationEn = "RF signal parameters (RSRP: ${rsrp?.let { "$it dBm" } ?: "Clean"}, SINR: ${sinr?.let { "$it dB" } ?: "Optimal"}) are within ideal operating specifications. The serving base station is operating efficiently.",
            explanationUr = "ریڈیو سگنل کی طاقت اور کوالٹی بالکل موزوں ہے۔ قریبی بیس اسٹیشن سے بہترین کنکشن قائم ہے۔",
            recommendationsEn = listOf(
                "Your mobile line is primed for low-latency gaming, 4K streaming, and HD voice calls.",
                "Record a snapshot to store this optimal baseline in local diagnostics."
            ),
            recommendationsUr = listOf(
                "آپ کا کنکشن تیز رفتار اسٹریمنگ اور گیمنگ کے لیے تیار ہے۔",
                "اس بہترین کارکردگی کو ریکارڈ میں محفوظ رکھیں۔"
            ),
            evidenceMetrics = evidence
        )
    }

    private fun buildNoCoverageReport(evidence: Map<String, String>): DiagnosticReport {
        return DiagnosticReport(
            category = DiagnosticCategory.NO_SIGNAL_OUTAGE,
            severity = DiagnosticSeverity.CRITICAL,
            titleEn = "Out of Base Station Coverage",
            titleUr = "کوریج ایریا سے باہر",
            explanationEn = "The device radio baseband is scanning for pilot beacons but cannot detect a serving carrier sector. Signal loss occurs naturally due to geographic distance, deep basement shielding, natural terrain barriers, or temporary base station maintenance.",
            explanationUr = "فون قریبی ٹاور کے سگنل تلاش کر رہا ہے مگر کوئی سروس دستیاب نہیں ہے۔ یہ مسئلہ فاصلے، تہہ خانے یا ٹاور کی مرمت کے باعث ہو سکتا ہے۔",
            recommendationsEn = listOf(
                "Move away from basement or windowless rooms toward an exterior opening.",
                "Verify your SIM subscription is active and has valid balance/line status.",
                "If in an area with known coverage, restart the device to clear baseband radio locks."
            ),
            recommendationsUr = listOf(
                "تہہ خانے یا بند کمرے سے نکل کر کھلی فضا میں جائیں۔",
                "چیک کریں کہ سم میں بیلنس یا پیکج فعال ہے۔",
                "فون کو ایک بار ری اسٹارٹ کر کے چیک کریں۔"
            ),
            evidenceMetrics = evidence
        )
    }
}
