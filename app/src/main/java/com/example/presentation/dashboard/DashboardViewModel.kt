package com.example.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.AppLanguage
import com.example.domain.model.SignalReading
import com.example.domain.model.SimComparisonStats
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.SpeedTestState
import com.example.domain.model.TelephonyStatus
import com.example.domain.repository.SignalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: SignalRepository,
    private val appPreferences: com.example.data.local.preferences.AppPreferences
) : ViewModel() {

    private val _selectedSlot = MutableStateFlow(0)
    private val _language = appPreferences.languageFlow
    private val _showHistorySheet = MutableStateFlow(false)
    private val _notificationMessage = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)

    // Phase 2 state flows
    private val _currentTab = MutableStateFlow(0)
    private val _speedTestProgress = MutableStateFlow(SpeedTestProgress())
    private val _showConsentDialog = MutableStateFlow(false)
    private val _selectedFilterOperator = MutableStateFlow<String?>(null)
    private val _isExporting = MutableStateFlow(false)

    private var speedTestJob: Job? = null

    private val _signalReading: Flow<SignalReading?> = _selectedSlot.flatMapLatest { slot ->
        repository.observeSignal(slot)
    }

    private data class LocalUiState(
        val language: AppLanguage,
        val slot: Int,
        val showHistory: Boolean,
        val notif: String?,
        val tab: Int,
        val consentDialog: Boolean,
        val filterOperator: String?,
        val isExporting: Boolean
    )

    private val _localState = combine(
        _language,
        _selectedSlot,
        _showHistorySheet,
        _notificationMessage,
        _currentTab,
        _showConsentDialog,
        _selectedFilterOperator,
        _isExporting
    ) { args: Array<Any?> ->
        LocalUiState(
            language = args[0] as AppLanguage,
            slot = args[1] as Int,
            showHistory = args[2] as Boolean,
            notif = args[3] as? String,
            tab = args[4] as Int,
            consentDialog = args[5] as Boolean,
            filterOperator = args[6] as? String,
            isExporting = args[7] as Boolean
        )
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _localState,
        repository.observeSimSlots(),
        _signalReading,
        repository.observeTelephonyStatus(),
        repository.getSavedDiagnostics(),
        repository.observeSpeedTests(),
        repository.observeComparisonStats(),
        _speedTestProgress
    ) { args: Array<Any?> ->
        val local = args[0] as LocalUiState
        @Suppress("UNCHECKED_CAST")
        val sims = args[1] as List<com.example.domain.model.SimSlotInfo>
        val reading = args[2] as? SignalReading
        val status = args[3] as TelephonyStatus
        @Suppress("UNCHECKED_CAST")
        val history = args[4] as List<com.example.domain.model.SignalDiagnosticRecord>
        @Suppress("UNCHECKED_CAST")
        val speedHistory = args[5] as List<SpeedTestRecord>
        @Suppress("UNCHECKED_CAST")
        val comparisons = args[6] as List<SimComparisonStats>
        val speedProgress = args[7] as SpeedTestProgress

        val adjustedSlot = if (sims.isNotEmpty() && sims.none { it.slotIndex == local.slot }) {
            sims.first().slotIndex
        } else {
            local.slot
        }

        DashboardUiState(
            currentLanguage = local.language,
            currentTab = local.tab,
            selectedSimSlot = adjustedSlot,
            simSlots = sims,
            currentReading = reading,
            telephonyStatus = status,
            savedDiagnostics = history,
            isScanning = _isRefreshing.value,
            showHistorySheet = local.showHistory,
            saveNotificationMessage = local.notif,
            speedTestProgress = speedProgress,
            speedTestHistory = speedHistory,
            comparisonStats = comparisons,
            showConsentDialog = local.consentDialog,
            selectedFilterOperator = local.filterOperator,
            isExporting = local.isExporting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setLanguage(language: AppLanguage) {
        appPreferences.setLanguage(language)
    }

    fun selectSimSlot(slot: Int) {
        _selectedSlot.value = slot
    }

    fun setCurrentTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun toggleHistorySheet(show: Boolean) {
        _showHistorySheet.value = show
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refresh()
            kotlinx.coroutines.delay(600)
            _isRefreshing.value = false
        }
    }

    fun recordSnapshot(reading: SignalReading, note: String? = null) {
        viewModelScope.launch {
            repository.saveDiagnostic(reading, note)
            _notificationMessage.value = "RECORD_SAVED"
        }
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    fun deleteDiagnostic(id: Long) {
        viewModelScope.launch {
            repository.deleteDiagnostic(id)
        }
    }

    fun clearAllDiagnostics() {
        viewModelScope.launch {
            repository.clearDiagnostics()
        }
    }

    // ============================================
    // Phase 2: Speed Testing & SIM Analysis Methods
    // ============================================

    fun requestSpeedTest() {
        // Show consent dialog regarding ~5-8MB data transfer
        _showConsentDialog.value = true
    }

    fun dismissConsentDialog() {
        _showConsentDialog.value = false
    }

    fun confirmConsentAndStart() {
        _showConsentDialog.value = false
        val currentState = uiState.value

        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            repository.runSpeedTest(
                slotIndex = currentState.selectedSimSlot,
                simSlots = currentState.simSlots,
                currentSignal = currentState.currentReading
            ).collect { progress ->
                _speedTestProgress.value = progress

                if (progress.state == SpeedTestState.COMPLETED && progress.completedRecord != null) {
                    // Save to Room Database automatically
                    repository.saveSpeedTest(progress.completedRecord)
                    _notificationMessage.value = "SPEED_SAVED"
                }
            }
        }
    }

    fun cancelSpeedTest() {
        speedTestJob?.cancel()
        _speedTestProgress.value = SpeedTestProgress(state = SpeedTestState.CANCELLED)
    }

    fun resetSpeedTest() {
        _speedTestProgress.value = SpeedTestProgress(state = SpeedTestState.IDLE)
    }

    fun deleteSpeedTest(id: Long) {
        viewModelScope.launch {
            repository.deleteSpeedTest(id)
        }
    }

    fun clearAllSpeedTests() {
        viewModelScope.launch {
            repository.clearSpeedTests()
        }
    }

    fun setFilterOperator(brand: String?) {
        _selectedFilterOperator.value = brand
    }

    fun exportCsvReport(onFileReady: (File) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val records = uiState.value.speedTestHistory
                val file = repository.generateCsvExport(records)
                onFileReady(file)
            } finally {
                _isExporting.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speedTestJob?.cancel()
    }

    class Factory(
        private val repository: SignalRepository,
        private val appPreferences: com.example.data.local.preferences.AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(repository, appPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
