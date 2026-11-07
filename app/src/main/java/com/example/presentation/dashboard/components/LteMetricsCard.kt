package com.example.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SignalReading
import com.example.presentation.localization.AppStrings

@Composable
fun LteMetricsCard(
    reading: SignalReading?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lte_metrics_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Card Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.lteAdvancedTitle(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // RSRP, RSRQ, SINR Grid (Primary RF parameters)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RSRP Metric
                val rsrpStr = reading?.lteRsrp?.let { "$it dBm" } ?: "N/A"
                val rsrpQualityColor = when {
                    reading?.lteRsrp == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    reading.lteRsrp >= -85 -> Color(0xFF2E7D32)
                    reading.lteRsrp >= -100 -> Color(0xFFFBC02D)
                    else -> Color(0xFFE53935)
                }
                MetricBox(
                    label = "RSRP",
                    subLabel = "Signal Power",
                    value = rsrpStr,
                    accentColor = rsrpQualityColor,
                    modifier = Modifier.weight(1f)
                )

                // RSRQ Metric
                val rsrqStr = reading?.lteRsrq?.let { "$it dB" } ?: "N/A"
                val rsrqQualityColor = when {
                    reading?.lteRsrq == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    reading.lteRsrq >= -10 -> Color(0xFF2E7D32)
                    reading.lteRsrq >= -15 -> Color(0xFFFBC02D)
                    else -> Color(0xFFE53935)
                }
                MetricBox(
                    label = "RSRQ",
                    subLabel = "Signal Quality",
                    value = rsrqStr,
                    accentColor = rsrqQualityColor,
                    modifier = Modifier.weight(1f)
                )

                // SINR Metric
                val sinrStr = reading?.lteSinr?.let { String.format("%.1f dB", it) } ?: "N/A"
                val sinrQualityColor = when {
                    reading?.lteSinr == null -> MaterialTheme.colorScheme.onSurfaceVariant
                    reading.lteSinr >= 10.0 -> Color(0xFF2E7D32)
                    reading.lteSinr >= 0.0 -> Color(0xFFFBC02D)
                    else -> Color(0xFFE53935)
                }
                MetricBox(
                    label = "SINR",
                    subLabel = "Noise Ratio",
                    value = sinrStr,
                    accentColor = sinrQualityColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Cell Parameters (PCI, Cell ID, TAC, EARFCN)
            Text(
                text = AppStrings.cellParameters(language),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            ParameterRow(
                label = AppStrings.cellId(language),
                value = reading?.cellId?.toString() ?: "N/A (Awaiting GPS)"
            )
            ParameterRow(
                label = AppStrings.pci(language),
                value = reading?.pci?.toString() ?: "N/A"
            )
            ParameterRow(
                label = AppStrings.tac(language),
                value = reading?.tac?.toString() ?: "N/A"
            )
            ParameterRow(
                label = AppStrings.earfcn(language),
                value = reading?.earfcn?.toString() ?: "N/A"
            )
            reading?.lteCqi?.let { cqi ->
                ParameterRow(
                    label = AppStrings.cqi(language),
                    value = "$cqi / 15"
                )
            }
            reading?.lteTimingAdvance?.let { ta ->
                ParameterRow(
                    label = AppStrings.timingAdvance(language),
                    value = "~${ta * 78} meters (TA: $ta)"
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    subLabel: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
        }
    }
}

@Composable
private fun ParameterRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
