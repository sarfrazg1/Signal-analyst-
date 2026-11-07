package com.example.presentation.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.domain.model.SignalQuality
import com.example.domain.model.SignalReading
import com.example.presentation.localization.AppStrings

@Composable
fun SignalMeterCard(
    reading: SignalReading?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val quality = reading?.quality ?: SignalQuality.UNKNOWN
    val qualityColor = Color(quality.colorHex)

    // Calculate percentage 0.0 to 1.0 based on real dBm (-130 dBm is 0%, -50 dBm is 100%)
    val targetDbm = reading?.lteRsrp ?: reading?.rawDbm
    val percent = when {
        targetDbm == null || targetDbm == Int.MAX_VALUE || targetDbm == 0 -> 0f
        targetDbm >= -55 -> 1.0f
        targetDbm <= -125 -> 0.05f
        else -> ((targetDbm - (-125f)) / 70f).coerceIn(0.05f, 1.0f)
    }

    val animatedPercent by animateFloatAsState(
        targetValue = percent,
        animationSpec = tween(durationMillis = 600),
        label = "signal_meter_anim"
    )

    val qualityLabel = if (language == AppLanguage.URDU) quality.urduLabel else quality.englishLabel
    val qualityDesc = if (language == AppLanguage.URDU) quality.urduDescription else quality.englishDescription

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_meter_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = "Signal Meter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.signalLevel(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Signal Quality Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = qualityColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(qualityColor))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(qualityColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = qualityLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = qualityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main dBm Reading Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = reading?.primaryMetricValue ?: "-- dBm",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (reading != null && reading.primaryMetricValue != "-- dBm") {
                            qualityColor
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                    Text(
                        text = if (reading?.lteRsrp != null) "LTE RSRP Metric" else "Raw Radio dBm",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Real 5-Bar Signal Visualizer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val activeBars = quality.barCount
                    for (i in 1..5) {
                        val barHeight = (12 + (i * 7)).dp
                        val isFilled = i <= activeBars
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                .background(
                                    if (isFilled) qualityColor else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Linear Progress Visualizer
            LinearProgressIndicator(
                progress = { animatedPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = qualityColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Technical interpretation
            Text(
                text = qualityDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
