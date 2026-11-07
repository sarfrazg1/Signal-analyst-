package com.example.presentation.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.SignalReading
import com.example.domain.model.SimpleQualityLevel
import com.example.domain.model.SimpleQualityStatus
import com.example.domain.model.SpeedTestRecord
import com.example.presentation.localization.AppStrings

@Composable
fun SimpleQualityCards(
    callQuality: SimpleQualityStatus,
    internetQuality: SimpleQualityStatus,
    reading: SignalReading?,
    latestSpeedTest: SpeedTestRecord?,
    language: AppLanguage,
    onNavigateToSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTechnicalExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // -------------------------------------------------------------
        // 1. Call Quality Card (کال کی کوالٹی)
        // -------------------------------------------------------------
        SingleQualityCard(
            title = AppStrings.callQualityTitle(language),
            quality = callQuality,
            icon = Icons.Default.Call,
            language = language,
            testTag = "call_quality_card"
        )

        // -------------------------------------------------------------
        // 2. Internet Quality Card (انٹرنیٹ کی کوالٹی)
        // -------------------------------------------------------------
        InternetQualityCard(
            title = AppStrings.internetQualityTitle(language),
            quality = internetQuality,
            latestSpeedTest = latestSpeedTest,
            language = language,
            onNavigateToSpeedTest = onNavigateToSpeedTest,
            testTag = "internet_quality_card"
        )

        // -------------------------------------------------------------
        // 3. Expandable Technical Information ("مزید تکنیکی معلومات")
        // -------------------------------------------------------------
        ExpandableTechnicalCard(
            isExpanded = isTechnicalExpanded,
            onToggleExpand = { isTechnicalExpanded = !isTechnicalExpanded },
            reading = reading,
            latestSpeedTest = latestSpeedTest,
            language = language
        )
    }
}

@Composable
private fun SingleQualityCard(
    title: String,
    quality: SimpleQualityStatus,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    language: AppLanguage,
    testTag: String
) {
    val statusColor = Color(quality.level.colorHex)
    val statusBg = Color(quality.level.bgHex)
    val statusLabel = if (language == AppLanguage.URDU) quality.urduLabel else quality.englishLabel
    val statusExplanation = if (language == AppLanguage.URDU) quality.urduExplanation else quality.englishExplanation

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Badge Pill (Best / Normal / Weak / Unavailable)
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation box with prominent readable text
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusIcon = when (quality.level) {
                        SimpleQualityLevel.BEST -> Icons.Default.CheckCircle
                        SimpleQualityLevel.NORMAL -> Icons.Default.Info
                        SimpleQualityLevel.WEAK -> Icons.Default.Warning
                        SimpleQualityLevel.UNAVAILABLE -> Icons.Default.Error
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = statusExplanation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InternetQualityCard(
    title: String,
    quality: SimpleQualityStatus,
    latestSpeedTest: SpeedTestRecord?,
    language: AppLanguage,
    onNavigateToSpeedTest: () -> Unit,
    testTag: String
) {
    val statusColor = Color(quality.level.colorHex)
    val statusBg = Color(quality.level.bgHex)
    val statusLabel = if (language == AppLanguage.URDU) quality.urduLabel else quality.englishLabel
    val statusExplanation = if (language == AppLanguage.URDU) quality.urduExplanation else quality.englishExplanation

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Icon + Title + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Status Badge Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = statusBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusIcon = when (quality.level) {
                        SimpleQualityLevel.BEST -> Icons.Default.CheckCircle
                        SimpleQualityLevel.NORMAL -> Icons.Default.Info
                        SimpleQualityLevel.WEAK -> Icons.Default.Warning
                        SimpleQualityLevel.UNAVAILABLE -> Icons.Default.Error
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = statusExplanation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed Test Status Section
            if (latestSpeedTest != null && latestSpeedTest.downloadMbps > 0) {
                // Show verified measured speed
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.URDU) {
                                    "ڈاؤن لوڈ: ${"%.1f".format(latestSpeedTest.downloadMbps)} ایم بی پی ایس | پنگ: ${latestSpeedTest.pingMs.toInt()} ms"
                                } else {
                                    "Download: ${"%.1f".format(latestSpeedTest.downloadMbps)} Mbps | Ping: ${latestSpeedTest.pingMs.toInt()} ms"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(onClick = onNavigateToSpeedTest)
                        )
                    }
                }
            } else {
                // Speed test not run yet notice + action button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.speedNotTestedNotice(language),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onNavigateToSpeedTest,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("run_speed_test_from_home")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.runSpeedTestNow(language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableTechnicalCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    reading: SignalReading?,
    latestSpeedTest: SpeedTestRecord?,
    language: AppLanguage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expandable_technical_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Clickable Header to Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleExpand)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isExpanded) {
                            AppStrings.collapseTechnicalDetails(language)
                        } else {
                            AppStrings.expandTechnicalDetails(language)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = AppStrings.technicalDetailsSubtitle(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    val unavailableText = AppStrings.infoUnavailable(language)

                    // 1. Signal Strength (dBm)
                    val rawDbmValue = reading?.rawDbm?.let {
                        if (it != Int.MAX_VALUE && it < 0) "$it dBm" else null
                    } ?: unavailableText

                    // 2. RSRP
                    val rsrpValue = reading?.lteRsrp?.let {
                        if (it != Int.MAX_VALUE && it < 0) "$it dBm" else null
                    } ?: unavailableText

                    // 3. RSRQ
                    val rsrqValue = reading?.lteRsrq?.let {
                        if (it != Int.MAX_VALUE) "$it dB" else null
                    } ?: unavailableText

                    // 4. SINR
                    val sinrValue = reading?.lteSinr?.let {
                        "${"%.1f".format(it)} dB"
                    } ?: unavailableText

                    // 5. Ping
                    val pingValue = latestSpeedTest?.pingMs?.let {
                        "${it.toInt()} ms"
                    } ?: unavailableText

                    // 6. Jitter
                    val jitterValue = latestSpeedTest?.jitterMs?.let {
                        "${it.toInt()} ms"
                    } ?: unavailableText

                    // 7. Download Speed
                    val downloadValue = latestSpeedTest?.downloadMbps?.let {
                        if (it > 0) "${"%.2f".format(it)} Mbps" else null
                    } ?: unavailableText

                    // 8. Upload Speed
                    val uploadValue = latestSpeedTest?.uploadMbps?.let {
                        if (it > 0) "${"%.2f".format(it)} Mbps" else null
                    } ?: unavailableText

                    // 9. Network Type
                    val networkTypeValue = reading?.let {
                        "${it.networkGeneration} (${it.networkTypeDetails})"
                    } ?: unavailableText

                    // Grid layout for 9 parameters
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        TechnicalParamRow(
                            label1 = if (language == AppLanguage.URDU) "سگنل پاور (dBm)" else "Signal Strength (dBm)",
                            value1 = rawDbmValue,
                            label2 = if (language == AppLanguage.URDU) "ایل ٹی ای RSRP" else "LTE RSRP",
                            value2 = rsrpValue
                        )
                        TechnicalParamRow(
                            label1 = if (language == AppLanguage.URDU) "سگنل کوالٹی (RSRQ)" else "Signal Quality (RSRQ)",
                            value1 = rsrqValue,
                            label2 = if (language == AppLanguage.URDU) "شور تناسب (SINR)" else "Noise Ratio (SINR)",
                            value2 = sinrValue
                        )
                        TechnicalParamRow(
                            label1 = if (language == AppLanguage.URDU) "پنگ تاخیر (Ping)" else "Ping Latency",
                            value1 = pingValue,
                            label2 = if (language == AppLanguage.URDU) "جٹر (Jitter)" else "Jitter",
                            value2 = jitterValue
                        )
                        TechnicalParamRow(
                            label1 = if (language == AppLanguage.URDU) "ڈاؤن لوڈ اسپیڈ" else "Download Speed",
                            value1 = downloadValue,
                            label2 = if (language == AppLanguage.URDU) "اپ لوڈ اسپیڈ" else "Upload Speed",
                            value2 = uploadValue
                        )
                        TechnicalParamRow(
                            label1 = if (language == AppLanguage.URDU) "نیٹ ورک جنریشن" else "Network Generation",
                            value1 = networkTypeValue,
                            label2 = if (language == AppLanguage.URDU) "سیل ٹاور ID" else "Cell Tower ID",
                            value2 = reading?.cellId?.let { if (it > 0) "$it" else null } ?: unavailableText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Authenticity Notice inside Technical Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == AppLanguage.URDU) {
                                "یہ تمام پیمائشیں اصلی اینڈرائڈ ہارڈویئر اور سگنل اینٹینا سے حاصل کی گئی ہیں۔ کوئی فرضی قیمت شامل نہیں ہے۔"
                            } else {
                                "All metrics are measured directly from genuine Android Telephony hardware. Zero simulated or fabricated values."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalParamRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TechnicalParamItem(label = label1, value = value1, modifier = Modifier.weight(1f))
        TechnicalParamItem(label = label2, value = value2, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TechnicalParamItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
