package com.example.presentation.dashboard

import com.example.domain.model.AppLanguage
import com.example.domain.model.SignalDiagnosticRecord
import com.example.domain.model.SignalReading
import com.example.domain.model.SimComparisonStats
import com.example.domain.model.SimSlotInfo
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.TelephonyStatus

data class DashboardUiState(
    val currentLanguage: AppLanguage = AppLanguage.URDU,
    val currentTab: Int = 0, // 0: Signal, 1: Speed Test, 2: Diagnostics, 3: Compare, 4: About Us
    val selectedSimSlot: Int = 0,
    val simSlots: List<SimSlotInfo> = emptyList(),
    val currentReading: SignalReading? = null,
    val telephonyStatus: TelephonyStatus = TelephonyStatus.NORMAL,
    val savedDiagnostics: List<SignalDiagnosticRecord> = emptyList(),
    val isScanning: Boolean = false,
    val showHistorySheet: Boolean = false,
    val saveNotificationMessage: String? = null,

    // Phase 2 State
    val speedTestProgress: SpeedTestProgress = SpeedTestProgress(),
    val speedTestHistory: List<SpeedTestRecord> = emptyList(),
    val comparisonStats: List<SimComparisonStats> = emptyList(),
    val showConsentDialog: Boolean = false,
    val selectedFilterOperator: String? = null,
    val isExporting: Boolean = false
)
