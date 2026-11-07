package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TelecomPrimaryCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = TelecomPrimaryGreen,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = Color(0xFF6BFF9E),
    tertiary = TelecomAccentYellow,
    background = TelecomDarkBg,
    onBackground = Color(0xFFE2E7EE),
    surface = TelecomSurfaceDark,
    onSurface = Color(0xFFE2E7EE),
    surfaceVariant = TelecomSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC0CAD5)
)

private val LightColorScheme = lightColorScheme(
    primary = TelecomPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF006C35),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF8AC),
    onSecondaryContainer = Color(0xFF00210C),
    tertiary = Color(0xFF755B00),
    background = TelecomLightBg,
    onBackground = Color(0xFF191C1E),
    surface = TelecomSurfaceLight,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = TelecomSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF42474E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent analyzer palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
