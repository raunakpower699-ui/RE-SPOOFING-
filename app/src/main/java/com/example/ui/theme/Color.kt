package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val DiabloRed = Color(0xFFFF2A4D)
val DiabloRedDim = Color(0xFFB81D35)
val DiabloRedGlow = Color(0x66FF2A4D)
val DiabloOrange = Color(0xFFFF6D00)

val ElectricCyan = Color(0xFF00E5FF)
val ElectricCyanGlow = Color(0x6600E5FF)
val CobaltTurbo = Color(0xFF2979FF)
val CobaltDeep = Color(0xFF0F172A)

val TelemetryGreen = Color(0xFF00E676)
val TelemetryGreenGlow = Color(0x6600E676)

val ObsidianBg = Color(0xFF07090E)
val GlassSurface = Color(0x2E1E293B)
val GlassSurfaceElevated = Color(0x40334155)
val GlassSurfaceHighlight = Color(0x20FFFFFF)
val GlassBorder = Color(0x3394A3B8)
val GlassBorderActive = Color(0x8000E5FF)

// Aliases for compatibility
val CarbonSurface = GlassSurface
val CarbonSurfaceElevated = GlassSurfaceElevated
val CarbonBorder = GlassBorder

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextDisabled = Color(0xFF475569)

val LiquidGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x28FFFFFF),
        Color(0x0F0F172A),
        Color(0x1F1E293B)
    )
)

val DiabloGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x40FF2A4D),
        Color(0x150F172A),
        Color(0x25B81D35)
    )
)

val CyanGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x3800E5FF),
        Color(0x150F172A),
        Color(0x202979FF)
    )
)

val GreenGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x3800E676),
        Color(0x150F172A),
        Color(0x2000B0FF)
    )
)
