package com.example

import com.example.domain.engine.DiagnosticEngine
import com.example.domain.model.DiagnosticCategory
import com.example.domain.model.DiagnosticSeverity
import com.example.domain.model.PakistaniOperator
import com.example.domain.model.SignalQuality
import com.example.domain.model.SignalReading
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.TelephonyStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.round

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_pakistani_operator_detection_by_mcc_mnc() {
        // Jazz: 410-01 and 410-07
        assertEquals(PakistaniOperator.JAZZ, PakistaniOperator.fromMccMnc("410", "01"))
        assertEquals(PakistaniOperator.JAZZ, PakistaniOperator.fromMccMnc("410", "07"))

        // Zong: 410-04
        assertEquals(PakistaniOperator.ZONG, PakistaniOperator.fromMccMnc("410", "04"))

        // Ufone: 410-03
        assertEquals(PakistaniOperator.UFONE, PakistaniOperator.fromMccMnc("410", "03"))

        // Telenor: 410-06
        assertEquals(PakistaniOperator.TELENOR, PakistaniOperator.fromMccMnc("410", "06"))

        // SCOM: 410-05
        assertEquals(PakistaniOperator.SCOM, PakistaniOperator.fromMccMnc("410", "05"))

        // Other international
        assertEquals(PakistaniOperator.OTHER, PakistaniOperator.fromMccMnc("310", "260"))
    }

    @Test
    fun verify_pakistani_operator_detection_by_carrier_name_fallback() {
        assertEquals(PakistaniOperator.JAZZ, PakistaniOperator.fromMccMnc(null, null, "Mobilink"))
        assertEquals(PakistaniOperator.JAZZ, PakistaniOperator.fromMccMnc(null, null, "Jazz PK"))
        assertEquals(PakistaniOperator.ZONG, PakistaniOperator.fromMccMnc(null, null, "CMPak"))
        assertEquals(PakistaniOperator.UFONE, PakistaniOperator.fromMccMnc(null, null, "PTML Ufone"))
        assertEquals(PakistaniOperator.TELENOR, PakistaniOperator.fromMccMnc(null, null, "Telenor PK"))
        assertEquals(PakistaniOperator.SCOM, PakistaniOperator.fromMccMnc(null, null, "SCO Telecom"))
    }

    @Test
    fun verify_signal_quality_classification() {
        // LTE RSRP: >= -80 Excellent, -81..-90 Good, -91..-100 Fair, -101..-115 Poor, < -115 Very Poor
        assertEquals(SignalQuality.EXCELLENT, SignalQuality.fromLteRsrp(-78))
        assertEquals(SignalQuality.GOOD, SignalQuality.fromLteRsrp(-88))
        assertEquals(SignalQuality.FAIR, SignalQuality.fromLteRsrp(-96))
        assertEquals(SignalQuality.POOR, SignalQuality.fromLteRsrp(-110))
        assertEquals(SignalQuality.VERY_POOR, SignalQuality.fromLteRsrp(-124))
        assertEquals(SignalQuality.UNKNOWN, SignalQuality.fromLteRsrp(null))
        assertEquals(SignalQuality.UNKNOWN, SignalQuality.fromLteRsrp(Int.MAX_VALUE))
    }

    @Test
    fun verify_bandwidth_mbps_calculation() {
        // 5 MB (5,242,880 bytes) transferred in 2.0 seconds:
        val bytes = 5_242_880L
        val seconds = 2.0
        val mbps = (bytes * 8.0) / (seconds * 1_000_000.0)
        val roundedMbps = round(mbps * 10) / 10.0
        assertEquals(21.0, roundedMbps, 0.01)
    }

    @Test
    fun verify_jitter_calculation() {
        // Ping samples: 25ms, 28ms, 24ms, 30ms
        val pings = listOf(25.0, 28.0, 24.0, 30.0)
        var sumDiff = 0.0
        for (i in 1 until pings.size) {
            sumDiff += abs(pings[i] - pings[i - 1])
        }
        val jitter = sumDiff / (pings.size - 1)
        assertEquals(4.33, jitter, 0.05)
    }

    @Test
    fun verify_diagnostic_engine_rf_interference() {
        val signal = SignalReading(
            operator = PakistaniOperator.JAZZ,
            operatorName = "Jazz",
            mcc = "410",
            mnc = "01",
            networkGeneration = "4G LTE",
            networkTypeDetails = "LTE",
            rawDbm = -85,
            lteRsrp = -85,
            lteRsrq = -16,
            lteSinr = 0.8, // Heavy interference
            lteCqi = 6,
            lteTimingAdvance = 2,
            cellId = 123456L,
            pci = 142,
            tac = 2301,
            earfcn = 1650,
            quality = SignalQuality.GOOD,
            simSlotIndex = 0,
            subscriptionId = 1,
            isRegistered = true,
            isRoaming = false
        )

        val report = DiagnosticEngine.analyze(
            currentSignal = signal,
            latestSpeedTest = null,
            telephonyStatus = TelephonyStatus.NORMAL,
            activeSim = null
        )

        assertEquals(DiagnosticCategory.HIGH_INTERFERENCE, report.category)
        assertEquals(DiagnosticSeverity.MODERATE, report.severity)
        assertTrue(report.recommendationsEn.isNotEmpty())
        assertTrue(report.recommendationsUr.isNotEmpty())
    }

    @Test
    fun verify_diagnostic_engine_weak_coverage() {
        val weakSignal = SignalReading(
            operator = PakistaniOperator.ZONG,
            operatorName = "Zong 4G",
            mcc = "410",
            mnc = "04",
            networkGeneration = "4G LTE",
            networkTypeDetails = "LTE",
            rawDbm = -118,
            lteRsrp = -118, // Cell edge
            lteRsrq = -15,
            lteSinr = 1.0,
            lteCqi = 2,
            lteTimingAdvance = 8,
            cellId = 445566L,
            pci = 88,
            tac = 502,
            earfcn = 3100,
            quality = SignalQuality.VERY_POOR,
            simSlotIndex = 0,
            subscriptionId = 1,
            isRegistered = true,
            isRoaming = false
        )

        val report = DiagnosticEngine.analyze(
            currentSignal = weakSignal,
            latestSpeedTest = null,
            telephonyStatus = TelephonyStatus.NORMAL,
            activeSim = null
        )

        assertEquals(DiagnosticCategory.WEAK_COVERAGE, report.category)
        assertEquals(DiagnosticSeverity.CRITICAL, report.severity)
    }

    @Test
    fun verify_diagnostic_engine_tower_congestion() {
        val goodSignal = SignalReading(
            operator = PakistaniOperator.UFONE,
            operatorName = "Ufone 4G",
            mcc = "410",
            mnc = "03",
            networkGeneration = "4G LTE",
            networkTypeDetails = "LTE",
            rawDbm = -80,
            lteRsrp = -80, // Strong signal
            lteRsrq = -8,
            lteSinr = 14.5, // Clean RF link
            lteCqi = 12,
            lteTimingAdvance = 1,
            cellId = 78910L,
            pci = 45,
            tac = 100,
            earfcn = 1800,
            quality = SignalQuality.EXCELLENT,
            simSlotIndex = 0,
            subscriptionId = 1,
            isRegistered = true,
            isRoaming = false
        )

        val lowSpeed = SpeedTestRecord(
            id = 1L,
            timestamp = System.currentTimeMillis(),
            operatorName = "Ufone",
            operatorBrand = "Ufone 4G",
            mccMnc = "410-03",
            simSlotIndex = 0,
            networkGeneration = "4G LTE",
            downloadMbps = 1.8, // Throttled or congested
            uploadMbps = 8.5,
            pingMs = 35.0,
            jitterMs = 4.0,
            packetLossPercent = 0.0,
            signalDbm = -80,
            signalRsrp = -80,
            isWifi = false
        )

        val report = DiagnosticEngine.analyze(
            currentSignal = goodSignal,
            latestSpeedTest = lowSpeed,
            telephonyStatus = TelephonyStatus.NORMAL,
            activeSim = null
        )

        assertEquals(DiagnosticCategory.TOWER_CONGESTION, report.category)
        assertEquals(DiagnosticSeverity.MODERATE, report.severity)
    }

    @Test
    fun verify_diagnostic_engine_airplane_mode() {
        val report = DiagnosticEngine.analyze(
            currentSignal = null,
            latestSpeedTest = null,
            telephonyStatus = TelephonyStatus.AIRPLANE_MODE,
            activeSim = null
        )

        assertEquals(DiagnosticCategory.HARDWARE_OR_SIM, report.category)
        assertEquals(DiagnosticSeverity.CRITICAL, report.severity)
    }
}
