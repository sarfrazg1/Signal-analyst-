package com.example.presentation.dashboard

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.AppLanguage
import com.example.presentation.comparison.ComparisonScreen
import com.example.presentation.diagnostics.DiagnosticsScreen
import com.example.presentation.dashboard.components.LteMetricsCard
import com.example.presentation.dashboard.components.OperatorBadge
import com.example.presentation.dashboard.components.SavedDiagnosticsBottomSheet
import com.example.presentation.dashboard.components.SignalMeterCard
import com.example.presentation.dashboard.components.SimSelectorTab
import com.example.presentation.dashboard.components.StatusBanner
import com.example.presentation.localization.AppStrings
import com.example.presentation.speedtest.SpeedTestScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Permission launcher for Location and Phone State
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    fun requestAppPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
        )
    }

    // React to notifications
    LaunchedEffect(uiState.saveNotificationMessage) {
        when (uiState.saveNotificationMessage) {
            "RECORD_SAVED" -> {
                snackbarHostState.showSnackbar(AppStrings.savedSuccessfully(uiState.currentLanguage))
                viewModel.clearNotification()
            }
            "SPEED_SAVED" -> {
                snackbarHostState.showSnackbar(AppStrings.speedSavedSuccessfully(uiState.currentLanguage))
                viewModel.clearNotification()
            }
        }
    }

    // Determine layout direction for proper Urdu RTL support
    val layoutDirection = if (uiState.currentLanguage == AppLanguage.URDU) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CellTower,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = AppStrings.appTitle(uiState.currentLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (uiState.currentTab) {
                                        1 -> AppStrings.tabSpeed(uiState.currentLanguage)
                                        2 -> AppStrings.tabDiagnostics(uiState.currentLanguage)
                                        3 -> AppStrings.tabCompare(uiState.currentLanguage)
                                        else -> AppStrings.liveDetection(uiState.currentLanguage)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Language switcher (EN / اردو)
                        LanguageTogglePill(
                            currentLanguage = uiState.currentLanguage,
                            onLanguageChanged = { viewModel.setLanguage(it) }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Refresh button with spin animation
                        val rotation = if (uiState.isScanning) {
                            val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
                            val angle by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = 800, easing = LinearEasing)
                                ),
                                label = "angle_anim"
                            )
                            angle
                        } else 0f

                        IconButton(
                            onClick = { viewModel.refresh() },
                            modifier = Modifier
                                .testTag("refresh_button")
                                .rotate(rotation)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Again",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == 0,
                        onClick = { viewModel.setCurrentTab(0) },
                        icon = { Icon(Icons.Default.CellTower, contentDescription = null) },
                        label = { Text(AppStrings.tabSignal(uiState.currentLanguage)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_signal")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == 1,
                        onClick = { viewModel.setCurrentTab(1) },
                        icon = { Icon(Icons.Default.Speed, contentDescription = null) },
                        label = { Text(AppStrings.tabSpeed(uiState.currentLanguage)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_speed")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == 2,
                        onClick = { viewModel.setCurrentTab(2) },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        label = { Text(AppStrings.tabDiagnostics(uiState.currentLanguage)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_diagnostics")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == 3,
                        onClick = { viewModel.setCurrentTab(3) },
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text(AppStrings.tabCompare(uiState.currentLanguage)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_compare")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState.currentTab) {
                    0 -> {
                        // Phase 1 Real-time Cellular Signal Meter
                        SignalTabContent(
                            uiState = uiState,
                            onRequestPermissions = { requestAppPermissions() },
                            onSelectSimSlot = { viewModel.selectSimSlot(it) },
                            onRecordSnapshot = { reading -> viewModel.recordSnapshot(reading) },
                            onOpenHistory = { viewModel.toggleHistorySheet(true) }
                        )
                    }
                    1 -> {
                        // Phase 2 Speed Test Benchmarking
                        SpeedTestScreen(
                            uiState = uiState,
                            onRequestTest = { viewModel.requestSpeedTest() },
                            onCancelTest = { viewModel.cancelSpeedTest() },
                            onConfirmConsent = { viewModel.confirmConsentAndStart() },
                            onDismissConsent = { viewModel.dismissConsentDialog() },
                            onSelectSimSlot = { viewModel.selectSimSlot(it) }
                        )
                    }
                    2 -> {
                        // Phase 3 Intelligent Signal Diagnostics
                        DiagnosticsScreen(
                            uiState = uiState,
                            onNavigateToSpeedTest = { viewModel.setCurrentTab(1) },
                            onRecordSnapshot = { reading -> viewModel.recordSnapshot(reading) },
                            onRefresh = { viewModel.refresh() }
                        )
                    }
                    3 -> {
                        // Phase 2 Multi-SIM Comparison & Analytics
                        ComparisonScreen(
                            uiState = uiState,
                            onExportCsv = { onReady -> viewModel.exportCsvReport(onReady) },
                            onFilterOperator = { viewModel.setFilterOperator(it) },
                            onDeleteSpeedTest = { viewModel.deleteSpeedTest(it) },
                            onClearAllTests = { viewModel.clearAllSpeedTests() }
                        )
                    }
                }
            }

            // History Bottom Sheet for Signal Diagnostics
            if (uiState.showHistorySheet) {
                SavedDiagnosticsBottomSheet(
                    sheetState = historySheetState,
                    records = uiState.savedDiagnostics,
                    language = uiState.currentLanguage,
                    onDismiss = { viewModel.toggleHistorySheet(false) },
                    onDeleteRecord = { viewModel.deleteDiagnostic(it) },
                    onClearAll = { viewModel.clearAllDiagnostics() }
                )
            }
        }
    }
}

@Composable
private fun SignalTabContent(
    uiState: DashboardUiState,
    onRequestPermissions: () -> Unit,
    onSelectSimSlot: (Int) -> Unit,
    onRecordSnapshot: (com.example.domain.model.SignalReading) -> Unit,
    onOpenHistory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status banner (e.g. Permission required, Airplane mode, No SIM, No signal)
            StatusBanner(
                status = uiState.telephonyStatus,
                language = uiState.currentLanguage,
                onRequestPermissions = onRequestPermissions
            )

            // Multi-SIM switcher tabs
            if (uiState.simSlots.size > 1) {
                SimSelectorTab(
                    simSlots = uiState.simSlots,
                    selectedSlot = uiState.selectedSimSlot,
                    language = uiState.currentLanguage,
                    onSelectSlot = onSelectSimSlot
                )
            }

            // Active Operator Card with MCC/MNC
            val selectedSim = uiState.simSlots.firstOrNull { it.slotIndex == uiState.selectedSimSlot }
                ?: uiState.simSlots.firstOrNull()
            OperatorBadge(
                reading = uiState.currentReading,
                simSlotInfo = selectedSim,
                language = uiState.currentLanguage
            )

            // Primary Signal Level & Quality Meter
            SignalMeterCard(
                reading = uiState.currentReading,
                language = uiState.currentLanguage
            )

            // LTE Advanced Radio Parameters (RSRP, RSRQ, SINR, PCI, Cell ID, TAC)
            LteMetricsCard(
                reading = uiState.currentReading,
                language = uiState.currentLanguage
            )

            // Action Buttons Row: Save Snapshot & Saved Diagnostics History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        uiState.currentReading?.let { reading ->
                            onRecordSnapshot(reading)
                        }
                    },
                    enabled = (uiState.currentReading != null),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("record_snapshot_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.saveSnapshot(uiState.currentLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("view_history_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${AppStrings.historyButton(uiState.currentLanguage)} (${uiState.savedDiagnostics.size})",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Authenticity Guarantee Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("authenticity_notice_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = AppStrings.genuineDataNotice(uiState.currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageTogglePill(
    currentLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.testTag("language_toggle_pill")
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageItem(
                label = "EN",
                isSelected = currentLanguage == AppLanguage.ENGLISH,
                onClick = { onLanguageChanged(AppLanguage.ENGLISH) },
                testTag = "lang_en_button"
            )
            Spacer(modifier = Modifier.width(2.dp))
            LanguageItem(
                label = "اردو",
                isSelected = currentLanguage == AppLanguage.URDU,
                onClick = { onLanguageChanged(AppLanguage.URDU) },
                testTag = "lang_ur_button"
            )
        }
    }
}

@Composable
private fun LanguageItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
