package com.example.presentation.speedtest.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SpeedTestState
import com.example.presentation.localization.AppStrings
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    currentSpeedMbps: Double,
    testState: SpeedTestState,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Speed range 0 to 100 Mbps for cellular benchmarking
    val maxSpeed = 100.0
    val targetFraction = (currentSpeedMbps / maxSpeed).coerceIn(0.0, 1.0).toFloat()

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 350),
        label = "speed_gauge_anim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("speed_gauge_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            val startAngle = 140f
            val sweepAngle = 260f

            // Background Track Arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Gradient Arc
            val activeSweep = sweepAngle * animatedFraction
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF),
                            Color(0xFF00E676),
                            Color(0xFFFFD600)
                        ),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Outer Tick Marks
            val centerOffset = center
            val radius = (size.minDimension / 2f) - (strokeWidth / 2f)
            val tickCount = 10
            for (i in 0..tickCount) {
                val tickAngleDeg = startAngle + (i.toFloat() / tickCount) * sweepAngle
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val outerPoint = Offset(
                    (centerOffset.x + (radius + 6.dp.toPx()) * cos(tickAngleRad)).toFloat(),
                    (centerOffset.y + (radius + 6.dp.toPx()) * sin(tickAngleRad)).toFloat()
                )
                val innerPoint = Offset(
                    (centerOffset.x + (radius - 2.dp.toPx()) * cos(tickAngleRad)).toFloat(),
                    (centerOffset.y + (radius - 2.dp.toPx()) * sin(tickAngleRad)).toFloat()
                )
                drawLine(
                    color = trackColor,
                    start = innerPoint,
                    end = outerPoint,
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Center Digital Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            val statusLabel = when (testState) {
                SpeedTestState.PINGING -> AppStrings.pingLatency(language)
                SpeedTestState.DOWNLOADING -> AppStrings.downloadSpeed(language)
                SpeedTestState.UPLOADING -> AppStrings.uploadSpeed(language)
                SpeedTestState.COMPLETED -> AppStrings.stateCompleted(language)
                SpeedTestState.ERROR -> "Error"
                SpeedTestState.CANCELLED -> "Cancelled"
                else -> AppStrings.tabSpeed(language)
            }

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%.1f", currentSpeedMbps),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = AppStrings.mbpsUnit(language),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
