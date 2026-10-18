package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DiabloRed,
    secondary = ElectricCyan,
    tertiary = TelemetryGreen,
    background = ObsidianBg,
    surface = CarbonSurface,
    onPrimary = TextPrimary,
    onSecondary = ObsidianBg,
    onTertiary = ObsidianBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun RESpoofingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
