package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VivoPerformanceColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF001F26),
    primaryContainer = CobaltDeep,
    onPrimaryContainer = TextPrimary,
    secondary = CobaltTurbo,
    onSecondary = Color.White,
    secondaryContainer = CarbonSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = TelemetryGreen,
    onTertiary = Color(0xFF00210E),
    error = TelemetryRed,
    onError = Color.White,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CarbonBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VivoPerformanceColorScheme,
        typography = Typography,
        content = content
    )
}
