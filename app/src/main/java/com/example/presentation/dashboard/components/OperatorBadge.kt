package com.example.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.PakistaniOperator
import com.example.domain.model.SignalReading
import com.example.domain.model.SimSlotInfo
import com.example.presentation.localization.AppStrings

@Composable
fun OperatorBadge(
    reading: SignalReading?,
    simSlotInfo: SimSlotInfo?,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val operator = reading?.operator ?: simSlotInfo?.operator ?: PakistaniOperator.OTHER
    val brandColor = Color(operator.primaryColorHex)
    val displayOperatorName = if (language == AppLanguage.URDU) {
        operator.urduName
    } else {
        operator.brandName
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("operator_badge_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Operator Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(brandColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = "Operator Icon",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = displayOperatorName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = operator.legalName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Network generation pill (e.g. 4G LTE, 5G NR)
                val generation = reading?.networkGeneration ?: "4G LTE"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = brandColor.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(brandColor.copy(alpha = 0.4f)))
                ) {
                    Text(
                        text = generation,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = brandColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges row: MCC/MNC, SIM Slot, Roaming status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MCC - MNC Tag
                val mccMnc = reading?.mccMncString ?: run {
                    if (simSlotInfo?.mcc != null && simSlotInfo.mnc != null) {
                        "${simSlotInfo.mcc}-${simSlotInfo.mnc}"
                    } else {
                        "MCC 410"
                    }
                }
                TagPill(label = "MCC/MNC: $mccMnc", icon = Icons.Default.Public)

                // Sim Slot Tag
                val slotIndex = reading?.simSlotIndex ?: simSlotInfo?.slotIndex ?: 0
                TagPill(label = AppStrings.simSlot(language, slotIndex), icon = Icons.Default.SimCard)

                // Roaming Tag if active
                val isRoaming = reading?.isRoaming ?: simSlotInfo?.isRoaming ?: false
                if (isRoaming) {
                    TagPill(
                        label = AppStrings.roamingActive(language),
                        icon = Icons.Default.Language,
                        tintColor = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun TagPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
