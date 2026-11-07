package com.example.presentation.comparison

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.domain.model.AppLanguage
import com.example.domain.model.SimComparisonStats
import com.example.domain.model.SpeedTestRecord
import com.example.presentation.dashboard.DashboardUiState
import com.example.presentation.localization.AppStrings
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ComparisonScreen(
    uiState: DashboardUiState,
    onExportCsv: ((File) -> Unit) -> Unit,
    onFilterOperator: (String?) -> Unit,
    onDeleteSpeedTest: (Long) -> Unit,
    onClearAllTests: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val comparisonList = uiState.comparisonStats
    val filteredHistory = if (uiState.selectedFilterOperator.isNullOrBlank()) {
        uiState.speedTestHistory
    } else {
        uiState.speedTestHistory.filter { it.operatorBrand == uiState.selectedFilterOperator }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compare_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.compareTitle(uiState.currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppStrings.compareSubtitle(uiState.currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Export & Share Report Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onExportCsv { file ->
                            shareCsvFile(context, file)
                        }
                    },
                    enabled = uiState.speedTestHistory.isNotEmpty() && !uiState.isExporting,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_csv_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.exportCsv(uiState.currentLanguage),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = {
                        shareTextSummary(context, comparisonList, uiState.speedTestHistory, uiState.currentLanguage)
                    },
                    enabled = uiState.speedTestHistory.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_summary_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.shareReport(uiState.currentLanguage),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // SIM & Operator Comparison Cards (Requirement 7 & 8: Separates Cellular Signal from Internet Speed)
            if (comparisonList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.noComparisonData(uiState.currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = AppStrings.rfSignalVsInternet(uiState.currentLanguage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                comparisonList.forEach { stats ->
                    ComparisonOperatorCard(
                        stats = stats,
                        language = uiState.currentLanguage
                    )
                }
            }

            // Historical Performance Trend Chart (Pure Jetpack Compose Canvas)
            if (filteredHistory.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_chart_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppStrings.historyCharts(uiState.currentLanguage),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Download Mbps",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HistoricalSpeedChart(
                            records = filteredHistory.takeLast(10).reversed(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    }
                }
            }

            // Filter Chips Section
            val allOperators = listOf("All", "Jazz", "Zong", "Ufone", "Telenor", "SCOM", "Wi-Fi")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allOperators.forEach { op ->
                    val isSelected = if (op == "All") uiState.selectedFilterOperator == null else uiState.selectedFilterOperator == op
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (op == "All") onFilterOperator(null) else onFilterOperator(op)
                        },
                        label = { Text(op) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // History List of Filtered Speed Tests
            if (filteredHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recorded Test History (${filteredHistory.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(onClick = onClearAllTests) {
                        Text(
                            text = AppStrings.clearAll(uiState.currentLanguage),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                filteredHistory.forEach { item ->
                    SpeedTestHistoryRow(
                        record = item,
                        language = uiState.currentLanguage,
                        onDelete = { onDeleteSpeedTest(item.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ComparisonOperatorCard(
    stats: SimComparisonStats,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val brandColor = Color(stats.operator.primaryColorHex)
    val displayBrand = if (language == AppLanguage.URDU) stats.operator.urduName else stats.operatorBrand

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compare_card_${stats.operatorBrand}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Operator Info & Total Tests
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(brandColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = displayBrand,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.testsConducted(language, stats.totalTests),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "SIM ${stats.simSlotIndex + 1}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))

            // Cellular RF Signal Quality (Evaluated Separately from Data Speed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.coverageScore(language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val signalDbmDisplay = stats.avgSignalRsrp?.let { "$it dBm (RSRP)" }
                    ?: stats.avgSignalDbm?.let { "$it dBm" }
                    ?: "Awaiting RF"

                Text(
                    text = "$signalDbmDisplay • ${stats.signalQualityScorePercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { stats.signalQualityScorePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = brandColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Internet Speed & Latency Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatColumn(
                    title = AppStrings.avgSpeed(language),
                    value = "${stats.avgDownloadMbps} Mbps",
                    subValue = "Peak: ${stats.maxDownloadMbps}",
                    modifier = Modifier.weight(1f)
                )

                StatColumn(
                    title = AppStrings.uploadSpeed(language),
                    value = "${stats.avgUploadMbps} Mbps",
                    subValue = "Packet Loss: ${stats.avgPacketLossPercent}%",
                    modifier = Modifier.weight(1f)
                )

                StatColumn(
                    title = AppStrings.pingLatency(language),
                    value = "${stats.avgPingMs.toInt()} ms",
                    subValue = "Jitter: ${stats.avgJitterMs} ms",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatColumn(
    title: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun HistoricalSpeedChart(
    records: List<SpeedTestRecord>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    val maxSpeed = (records.maxOfOrNull { it.downloadMbps } ?: 50.0).coerceAtLeast(10.0)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = records.size
        val availableWidth = width / barCount
        val barWidth = (availableWidth * 0.55f).coerceAtMost(28.dp.toPx())

        for (i in records.indices) {
            val record = records[i]
            val speedFraction = (record.downloadMbps / maxSpeed).toFloat().coerceIn(0.05f, 1f)
            val barHeight = height * speedFraction * 0.85f

            val left = (i * availableWidth) + (availableWidth - barWidth) / 2f
            val top = height - barHeight

            // Draw Bar
            drawRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight)
            )

            // Draw baseline tick
            drawLine(
                color = Color(0xFF42474E),
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun SpeedTestHistoryRow(
    record: SpeedTestRecord,
    language: AppLanguage,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.operatorBrand,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SIM ${record.simSlotIndex + 1} • ${record.networkGeneration}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • Ping: ${record.pingMs.toInt()} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.downloadMbps} Mbps",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = "↑ ${record.uploadMbps} Mbps",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00E676)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun shareCsvFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Pakistan Signal Analyzer AI - Speed & Telemetry Benchmark")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Speed Benchmark CSV"))
    } catch (_: Exception) {}
}

private fun shareTextSummary(
    context: Context,
    stats: List<SimComparisonStats>,
    records: List<SpeedTestRecord>,
    lang: AppLanguage
) {
    val sb = StringBuilder()
    sb.append("📊 Pakistan Signal Analyzer AI - Performance Report\n")
    sb.append("=========================================\n\n")

    if (stats.isNotEmpty()) {
        sb.append("SIM & Operator Comparison:\n")
        stats.forEach { s ->
            sb.append("• ${s.operatorBrand} (SIM ${s.simSlotIndex + 1}):\n")
            sb.append("  Avg Download: ${s.avgDownloadMbps} Mbps (Peak: ${s.maxDownloadMbps} Mbps)\n")
            sb.append("  Avg Upload: ${s.avgUploadMbps} Mbps\n")
            sb.append("  Latency: ${s.avgPingMs.toInt()} ms, Jitter: ${s.avgJitterMs} ms\n")
            sb.append("  Signal Quality Score: ${s.signalQualityScorePercent}%\n\n")
        }
    }

    sb.append("Total Benchmarks Recorded: ${records.size}\n")
    sb.append("Verified by genuine Android Telephony & Network Subsystem.\n")

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Pakistan Cellular Signal & Speed Report")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Summary Report"))
}
