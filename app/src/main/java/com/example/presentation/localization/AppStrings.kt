package com.example.presentation.localization

import com.example.domain.model.AppLanguage

object AppStrings {

    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Pakistan Signal Analyzer AI"
        AppLanguage.URDU -> "پاکستان سگنل اینالائزر AI"
    }

    fun appSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Authentic Telephony & Radio Spectrum Diagnostics"
        AppLanguage.URDU -> "اصلی سیلولر ریڈیو اور سگنل کا تجزیہ"
    }

    // Navigation Tabs
    fun tabSignal(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Signal Meter"
        AppLanguage.URDU -> "سگنل میٹر"
    }

    fun tabSpeed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Speed Test"
        AppLanguage.URDU -> "اسپیڈ ٹیسٹ"
    }

    fun tabCompare(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "SIM Compare"
        AppLanguage.URDU -> "سم موازنہ"
    }

    fun tabDiagnostics(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Diagnostics"
        AppLanguage.URDU -> "نیٹ ورک تشخیص"
    }

    fun liveDetection(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "LIVE TELEPHONY"
        AppLanguage.URDU -> "براہ راست سگنل"
    }

    fun activeOperator(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Active Operator"
        AppLanguage.URDU -> "موجودہ آپریٹر"
    }

    fun simSlot(lang: AppLanguage, slot: Int) = when (lang) {
        AppLanguage.ENGLISH -> "SIM ${slot + 1}"
        AppLanguage.URDU -> "سم ${slot + 1}"
    }

    fun defaultData(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Default Mobile Data"
        AppLanguage.URDU -> "بنیادی موبائل ڈیٹا"
    }

    fun roamingActive(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Roaming Active"
        AppLanguage.URDU -> "رومنگ فعال ہے"
    }

    fun networkGeneration(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Network Generation"
        AppLanguage.URDU -> "نیٹ ورک جنریشن"
    }

    fun signalLevel(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Raw Signal Level"
        AppLanguage.URDU -> "مجموعی سگنل لیول"
    }

    fun signalQuality(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Signal Quality"
        AppLanguage.URDU -> "سگنل کا معیار"
    }

    fun lteAdvancedTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "LTE Advanced Radio Metrics"
        AppLanguage.URDU -> "ایل ٹی ای ریڈیو پیمائشیں"
    }

    fun rsrpTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "RSRP (Received Power)"
        AppLanguage.URDU -> "آر ایس آر پی (موصولہ پاور)"
    }

    fun rsrpDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Primary measure of LTE coverage signal level. Desired: > -90 dBm."
        AppLanguage.URDU -> "ایل ٹی ای سگنل کوریج کی بنیادی پیمائش۔ موزوں: -90 سے بہتر۔"
    }

    fun rsrqTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "RSRQ (Received Quality)"
        AppLanguage.URDU -> "آر ایس آر کیو (سگنل کا معیار)"
    }

    fun rsrqDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Signal purity & cell interference. Desired: > -10 dB."
        AppLanguage.URDU -> "سگنل کی شفافیت اور مداخلت کی پیمائش۔ موزوں: -10 سے بہتر۔"
    }

    fun sinrTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "SINR (Noise Ratio)"
        AppLanguage.URDU -> "ایس آئی این آر (شور کا تناسب)"
    }

    fun sinrDesc(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Signal-to-Interference-plus-Noise Ratio. Desired: > 10 dB."
        AppLanguage.URDU -> "سگنل اور پس پردہ شور کا باہمی تناسب۔ موزوں: 10 ڈی بی سے زیادہ۔"
    }

    fun cellParameters(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Serving Tower Identity"
        AppLanguage.URDU -> "سیل ٹاور کی شناخت"
    }

    fun cellId(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cell ID (CI)"
        AppLanguage.URDU -> "سیل آئی ڈی (CI)"
    }

    fun pci(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Physical Cell ID (PCI)"
        AppLanguage.URDU -> "فزیکل سیل آئی ڈی (PCI)"
    }

    fun tac(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Tracking Area Code (TAC)"
        AppLanguage.URDU -> "ٹریکنگ ایریا کوڈ (TAC)"
    }

    fun earfcn(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Radio Channel (EARFCN)"
        AppLanguage.URDU -> "ریڈیو چینل (EARFCN)"
    }

    fun cqi(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Channel Quality (CQI)"
        AppLanguage.URDU -> "چینل کوالٹی (CQI)"
    }

    fun timingAdvance(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Timing Advance (Distance)"
        AppLanguage.URDU -> "ٹاور سے تخمینہ فاصلہ"
    }

    fun saveSnapshot(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Record Diagnostic"
        AppLanguage.URDU -> "پیمائش محفوظ کریں"
    }

    fun historyButton(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Saved History"
        AppLanguage.URDU -> "محفوظ شدہ ریکارڈز"
    }

    fun savedSuccessfully(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Diagnostic snapshot recorded to local database!"
        AppLanguage.URDU -> "پیمائش لوکل ڈیٹابیس میں کامیابی سے محفوظ ہو گئی!"
    }

    fun speedSavedSuccessfully(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Speed test benchmark saved to database!"
        AppLanguage.URDU -> "اسپیڈ ٹیسٹ کا ریکارڈ محفوظ ہو گیا!"
    }

    fun genuineDataNotice(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Hardware Authenticated: Measurements are read directly from Android Telephony & Radio hardware. Zero simulated or fabricated values."
        AppLanguage.URDU -> "اصلی ہارڈویئر پیمائش: تمام معلومات اینڈرائڈ ٹیلی فون اور ریڈیو ہارڈویئر سے حاصل کی گئی ہیں۔ کوئی فرضی ڈیٹا شامل نہیں ہے۔"
    }

    fun permissionTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Hardware Access Permission Needed"
        AppLanguage.URDU -> "ہارڈویئر تک رسائی کی اجازت درکار ہے"
    }

    fun permissionMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Android requires Location & Phone State permissions to inspect raw cellular signal strengths, active SIMs, and serving tower IDs."
        AppLanguage.URDU -> "اینڈرائڈ پر سگنل کی طاقت، سم سلاٹ اور ٹاور کی معلومات پڑھنے کے لیے لوکیشن اور فون اسٹیٹ کی اجازت ضروری ہے۔"
    }

    fun grantPermissions(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Grant Required Permissions"
        AppLanguage.URDU -> "اجازتیں فراہم کریں"
    }

    fun airplaneModeTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Airplane Mode Is Enabled"
        AppLanguage.URDU -> "ہوائی جہاز موڈ آن ہے"
    }

    fun airplaneModeMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cellular baseband transceiver is turned off. Disable Airplane Mode to inspect active cellular signals."
        AppLanguage.URDU -> "سیلولر ٹرانسیور بند ہے۔ سگنل جانچنے کے لیے ہوائی جہاز موڈ بند کریں۔"
    }

    fun noSimTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No SIM Card Detected"
        AppLanguage.URDU -> "کوئی سم کارڈ موجود نہیں"
    }

    fun noSimMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Insert a valid SIM card (Jazz, Zong, Ufone, Telenor or SCOM) to analyze cellular network."
        AppLanguage.URDU -> "نیٹ ورک جانچنے کے لیے فون میں فعال سم کارڈ (جاز، زونگ، یوفون، ٹیلی نار یا ایس کام) داخل کریں۔"
    }

    fun noSignalTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No Signal / Out of Service"
        AppLanguage.URDU -> "کوئی سگنل نہیں / سروس دستیاب نہیں"
    }

    fun noSignalMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "The device is searching for cellular base station coverage or in an emergency calls only zone."
        AppLanguage.URDU -> "ڈیوائس ٹاور سگنل کی تلاش میں ہے یا کوریج ایریا سے باہر ہے۔"
    }

    fun locationServiceDisabled(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Note: Enable device Location service to populate serving Cell ID and Physical Cell parameters."
        AppLanguage.URDU -> "نوٹ: ٹاور کی شناختی معلومات کے لیے ڈیوائس کی لوکیشن سروس آن رکھیں۔"
    }

    fun historyTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Saved Signal Diagnostics"
        AppLanguage.URDU -> "محفوظ شدہ سگنل ریکارڈز"
    }

    fun noHistory(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No signal records saved yet. Tap 'Record Diagnostic' on the dashboard to store a snapshot."
        AppLanguage.URDU -> "ابھی تک کوئی ریکارڈ موجود نہیں۔ ڈیش بورڈ پر 'پیمائش محفوظ کریں' کا بٹن دبائیں۔"
    }

    fun clearAll(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Clear All"
        AppLanguage.URDU -> "تمام حذف کریں"
    }

    fun close(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.URDU -> "بند کریں"
    }

    // ============================================
    // Phase 2: Speed Test & SIM Comparison Strings
    // ============================================

    fun startSpeedTest(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Start Speed Test"
        AppLanguage.URDU -> "اسپیڈ ٹیسٹ شروع کریں"
    }

    fun stopSpeedTest(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cancel Test"
        AppLanguage.URDU -> "ٹیسٹ منسوخ کریں"
    }

    fun downloadSpeed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Download"
        AppLanguage.URDU -> "ڈاؤن لوڈ"
    }

    fun uploadSpeed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Upload"
        AppLanguage.URDU -> "اپ لوڈ"
    }

    fun pingLatency(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Ping (Latency)"
        AppLanguage.URDU -> "پنگ (تاخیر)"
    }

    fun jitter(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Jitter"
        AppLanguage.URDU -> "جٹر (تغیر)"
    }

    fun packetLoss(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Packet Loss"
        AppLanguage.URDU -> "پیکٹ لوس"
    }

    fun mbpsUnit(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Mbps"
        AppLanguage.URDU -> "ایم بی پی ایس"
    }

    fun msUnit(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "ms"
        AppLanguage.URDU -> "ملی سیکنڈ"
    }

    fun consentTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Data Consumption Consent"
        AppLanguage.URDU -> "ڈیٹا استعمال کی اجازت"
    }

    fun consentMessage(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "This benchmark transfers ~5 to 8 MB of data to measure real ping, jitter, download, and upload speeds against reliable public CDN nodes. Standard mobile carrier rates apply. Do you wish to continue?"
        AppLanguage.URDU -> "یہ اسپیڈ ٹیسٹ پنگ، جٹر، ڈاؤن لوڈ اور اپ لوڈ کی درست پیمائش کے لیے تقریباً 5 سے 8 ایم بی ڈیٹا استعمال کرے گا۔ موبائل آپریٹر کے ڈیٹا چارجز لاگو ہو سکتے ہیں۔ کیا آپ ٹیسٹ شروع کرنا چاہتے ہیں؟"
    }

    fun proceed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Proceed with Test"
        AppLanguage.URDU -> "ٹیسٹ شروع کریں"
    }

    fun cancel(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.URDU -> "منسوخ"
    }

    fun statePinging(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Measuring Ping, Jitter & Packet Loss..."
        AppLanguage.URDU -> "پنگ، جٹر اور پیکٹ لوس کی پیمائش جاری ہے..."
    }

    fun stateDownloading(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Measuring Download Bandwidth..."
        AppLanguage.URDU -> "ڈاؤن لوڈ اسپیڈ کی پیمائش جاری ہے..."
    }

    fun stateUploading(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Measuring Upload Bandwidth..."
        AppLanguage.URDU -> "اپ لوڈ اسپیڈ کی پیمائش جاری ہے..."
    }

    fun stateCompleted(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Benchmark Complete & Recorded"
        AppLanguage.URDU -> "اسپیڈ ٹیسٹ مکمل ہو گیا"
    }

    fun inactiveSimWarning(lang: AppLanguage, activeSim: String) = when (lang) {
        AppLanguage.ENGLISH -> "Mobile data is routed through $activeSim. Android does not allow sending internet traffic through an inactive SIM line."
        AppLanguage.URDU -> "موبائل ڈیٹا $activeSim پر فعال ہے۔ اینڈرائڈ غیر فعال سم لائن سے انٹرنیٹ ڈیٹا ٹرانسفر کی اجازت نہیں دیتا۔"
    }

    fun openSimSettings(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Open SIM Settings"
        AppLanguage.URDU -> "سم سیٹنگز کھولیں"
    }

    // SIM Comparison & Historical Analytics
    fun compareTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Multi-SIM & Operator Benchmark"
        AppLanguage.URDU -> "ملٹی سم اور آپریٹر موازنہ"
    }

    fun compareSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Side-by-side performance based on your actual recorded tests"
        AppLanguage.URDU -> "آپ کے اصل ریکارڈ کردہ ٹیسٹوں پر مبنی موازنہ"
    }

    fun rfSignalVsInternet(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Cellular Signal vs Internet Speed"
        AppLanguage.URDU -> "سیلولر سگنل بمقابلہ انٹرنیٹ اسپیڈ"
    }

    fun coverageScore(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Signal Quality Score"
        AppLanguage.URDU -> "سگنل کا معیار"
    }

    fun avgSpeed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Average Speed"
        AppLanguage.URDU -> "اوسط رفتار"
    }

    fun maxSpeed(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Peak Speed"
        AppLanguage.URDU -> "زیادہ سے زیادہ رفتار"
    }

    fun testsConducted(lang: AppLanguage, count: Int) = when (lang) {
        AppLanguage.ENGLISH -> "$count tests recorded"
        AppLanguage.URDU -> "$count ٹیسٹ ریکارڈ ہوئے"
    }

    fun noComparisonData(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "No benchmark tests found. Run speed tests on your SIM lines to populate the comparison dashboard."
        AppLanguage.URDU -> "ابھی تک کوئی اسپیڈ ٹیسٹ موجود نہیں۔ موازنہ دیکھنے کے لیے اپنی سم پر اسپیڈ ٹیسٹ چلائیں۔"
    }

    fun exportCsv(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Export CSV Report"
        AppLanguage.URDU -> "سی ایس وی رپورٹ ایکسپورٹ کریں"
    }

    fun shareReport(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Share Summary Report"
        AppLanguage.URDU -> "خلاصہ رپورٹ شیئر کریں"
    }

    fun filterOperator(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Filter Operator"
        AppLanguage.URDU -> "آپریٹر فلٹر"
    }

    fun allOperators(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "All Operators"
        AppLanguage.URDU -> "تمام آپریٹرز"
    }

    fun historyCharts(lang: AppLanguage) = when (lang) {
        AppLanguage.ENGLISH -> "Performance Trends"
        AppLanguage.URDU -> "کارکردگی کا رجحان"
    }
}
