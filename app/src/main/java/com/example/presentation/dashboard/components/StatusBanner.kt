package com.example.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.SimCardAlert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.TelephonyStatus
import com.example.presentation.localization.AppStrings

@Composable
fun StatusBanner(
    status: TelephonyStatus,
    language: AppLanguage,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (status) {
        TelephonyStatus.PERMISSION_REQUIRED -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("status_banner_permission"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.permissionTitle(language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.permissionMessage(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_permission_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = AppStrings.grantPermissions(language),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        TelephonyStatus.AIRPLANE_MODE -> {
            WarningCard(
                title = AppStrings.airplaneModeTitle(language),
                message = AppStrings.airplaneModeMessage(language),
                icon = Icons.Default.AirplanemodeActive,
                cardColor = Color(0xFFFFF3E0),
                contentColor = Color(0xFFE65100),
                testTag = "status_banner_airplane",
                modifier = modifier
            )
        }
        TelephonyStatus.NO_SIM -> {
            WarningCard(
                title = AppStrings.noSimTitle(language),
                message = AppStrings.noSimMessage(language),
                icon = Icons.Default.SimCardAlert,
                cardColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFC62828),
                testTag = "status_banner_no_sim",
                modifier = modifier
            )
        }
        TelephonyStatus.NO_SIGNAL -> {
            WarningCard(
                title = AppStrings.noSignalTitle(language),
                message = AppStrings.noSignalMessage(language),
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                cardColor = Color(0xFFECEFF1),
                contentColor = Color(0xFF37474F),
                testTag = "status_banner_no_signal",
                modifier = modifier
            )
        }
        TelephonyStatus.LOCATION_SERVICES_DISABLED -> {
            WarningCard(
                title = "GPS / Location Off",
                message = AppStrings.locationServiceDisabled(language),
                icon = Icons.Default.LocationOff,
                cardColor = Color(0xFFE0F7FA),
                contentColor = Color(0xFF006064),
                testTag = "status_banner_location_off",
                modifier = modifier
            )
        }
        TelephonyStatus.NORMAL -> {
            // No banner needed in normal status
        }
    }
}

@Composable
private fun WarningCard(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cardColor: Color,
    contentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
