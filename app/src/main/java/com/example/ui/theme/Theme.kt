package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DiabloRed,
    onPrimary = TextPrimary,
    primaryContainer = DiabloRedDim,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = CobaltDeep,
    onSecondaryContainer = ElectricCyan,
    tertiary = TelemetryGreen,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CarbonBorder
)

@Composable
fun RESpoofingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
