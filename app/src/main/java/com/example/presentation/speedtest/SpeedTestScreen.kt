package com.example.presentation.speedtest

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SpeedTestProgress
import com.example.domain.model.SpeedTestRecord
import com.example.domain.model.SpeedTestState
import com.example.presentation.dashboard.DashboardUiState
import com.example.presentation.dashboard.components.SimSelectorTab
import com.example.presentation.localization.AppStrings
import com.example.presentation.speedtest.components.SpeedGauge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpeedTestScreen(
    uiState: DashboardUiState,
    onRequestTest: () -> Unit,
    onCancelTest: () -> Unit,
    onConfirmConsent: () -> Unit,
    onDismissConsent: () -> Unit,
    onSelectSimSlot: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress = uiState.speedTestProgress
    val isTesting = progress.state in listOf(
        SpeedTestState.PREPARING,
        SpeedTestState.PINGING,
        SpeedTestState.DOWNLOADING,
        SpeedTestState.UPLOADING
    )

    val currentSpeed = when (progress.state) {
        SpeedTestState.DOWNLOADING -> progress.currentDownloadMbps
        SpeedTestState.UPLOADING -> progress.currentUploadMbps
        SpeedTestState.COMPLETED -> progress.currentDownloadMbps
        else -> 0.0
    }

    Box(
        modifier = modifier
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Multi-SIM switcher tabs
            if (uiState.simSlots.size > 1) {
                SimSelectorTab(
                    simSlots = uiState.simSlots,
                    selectedSlot = uiState.selectedSimSlot,
                    language = uiState.currentLanguage,
                    onSelectSlot = onSelectSimSlot
                )
            }

            // Inactive SIM check & warning banner (Requirement 13: Never claim to test an inactive SIM)
            val selectedSim = uiState.simSlots.firstOrNull { it.slotIndex == uiState.selectedSimSlot }
            val activeDataSim = uiState.simSlots.firstOrNull { it.isDataDefault }
            if (uiState.simSlots.size > 1 && selectedSim != null && !selectedSim.isDataDefault && activeDataSim != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inactive_sim_warning_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notice: Cellular Routing Constraint",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.inactiveSimWarning(
                                uiState.currentLanguage,
                                "SIM ${activeDataSim.slotIndex + 1} (${activeDataSim.operator.brandName})"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS))
                                } catch (_: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                                }
                            },
                            modifier = Modifier.testTag("open_sim_settings_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.openSimSettings(uiState.currentLanguage),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Error display card if speed test failed
            AnimatedVisibility(visible = progress.state == SpeedTestState.ERROR && progress.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speed_test_error_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = progress.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Circular Speed Gauge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("speed_gauge_container"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SpeedGauge(
                        currentSpeedMbps = currentSpeed,
                        testState = progress.state,
                        language = uiState.currentLanguage
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Linear progress during active test
                    if (isTesting) {
                        LinearProgressIndicator(
                            progress = { progress.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val stateDesc = when (progress.state) {
                            SpeedTestState.PINGING -> AppStrings.statePinging(uiState.currentLanguage)
                            SpeedTestState.DOWNLOADING -> AppStrings.stateDownloading(uiState.currentLanguage)
                            SpeedTestState.UPLOADING -> AppStrings.stateUploading(uiState.currentLanguage)
                            else -> "Preparing..."
                        }
                        Text(
                            text = stateDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Real-time Metrics 4-Box Grid: Download, Upload, Ping, Jitter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricDisplayCard(
                    icon = Icons.Default.ArrowDownward,
                    label = AppStrings.downloadSpeed(uiState.currentLanguage),
                    value = progress.currentDownloadMbps.takeIf { it > 0 }?.let { String.format("%.1f", it) } ?: "--",
                    unit = AppStrings.mbpsUnit(uiState.currentLanguage),
                    accentColor = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f)
                )

                MetricDisplayCard(
                    icon = Icons.Default.ArrowUpward,
                    label = AppStrings.uploadSpeed(uiState.currentLanguage),
                    value = progress.currentUploadMbps.takeIf { it > 0 }?.let { String.format("%.1f", it) } ?: "--",
                    unit = AppStrings.mbpsUnit(uiState.currentLanguage),
                    accentColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricDisplayCard(
                    icon = Icons.Default.NetworkPing,
                    label = AppStrings.pingLatency(uiState.currentLanguage),
                    value = progress.currentPingMs?.let { String.format("%.0f", it) } ?: "--",
                    unit = AppStrings.msUnit(uiState.currentLanguage),
                    accentColor = Color(0xFFFFD600),
                    modifier = Modifier.weight(1f)
                )

                MetricDisplayCard(
                    icon = Icons.Default.SyncAlt,
                    label = AppStrings.jitter(uiState.currentLanguage),
                    value = progress.currentJitterMs?.let { String.format("%.1f", it) } ?: "--",
                    unit = AppStrings.msUnit(uiState.currentLanguage),
                    accentColor = Color(0xFFFF6D00),
                    modifier = Modifier.weight(1f)
                )
            }

            // Packet Loss Row (if measured reliably)
            progress.currentPacketLossPercent?.let { loss ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.packetLoss(uiState.currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.1f%%", loss),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (loss == 0.0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Primary Action Button (Start / Cancel)
            if (isTesting) {
                Button(
                    onClick = onCancelTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("cancel_speed_test_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.stopSpeedTest(uiState.currentLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                Button(
                    onClick = onRequestTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_speed_test_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.startSpeedTest(uiState.currentLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Recent speed tests section
            if (uiState.speedTestHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Speed Tests",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.speedTestHistory.size} tests",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.speedTestHistory.take(3).forEach { testRecord ->
                    SpeedTestHistoryItem(
                        record = testRecord,
                        language = uiState.currentLanguage
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // User Consent Dialog (Requirement 3: With user consent & reasonable data consumption)
        if (uiState.showConsentDialog) {
            AlertDialog(
                onDismissRequest = onDismissConsent,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.consentTitle(uiState.currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(
                        text = AppStrings.consentMessage(uiState.currentLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmConsent,
                        modifier = Modifier.testTag("confirm_consent_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = AppStrings.proceed(uiState.currentLanguage))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = onDismissConsent,
                        modifier = Modifier.testTag("dismiss_consent_button")
                    ) {
                        Text(text = AppStrings.cancel(uiState.currentLanguage))
                    }
                },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("speed_consent_dialog")
            )
        }
    }
}

@Composable
private fun MetricDisplayCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SpeedTestHistoryItem(
    record: SpeedTestRecord,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.operatorBrand,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (record.isWifi) "Wi-Fi" else "SIM ${record.simSlotIndex + 1}",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "↓ ${record.downloadMbps} M",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "↑ ${record.uploadMbps} M",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00E676)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${record.pingMs.toInt()} ms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    record.signalRsrp?.let { rsrp ->
                        Text(
                            text = "$rsrp dBm",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
