package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.RefreshRateTarget
import com.example.ui.components.RealTimeThermalMonitorCard
import com.example.ui.components.RefreshRateLockControlCard
import com.example.ui.components.ThermalThrottlingAlertBanner
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloGlassGradient
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedDim
import com.example.ui.theme.DiabloRedGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LiquidGlassGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryGreenGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    state: PerformanceTelemetryState,
    onStartPerformanceMode: () -> Unit,
    onStopPerformanceMode: () -> Unit,
    onSelectProfile: (PerformanceProfile) -> Unit,
    onSelectRefreshRate: (RefreshRateTarget) -> Unit = {},
    onDismissThermalAlert: () -> Unit = {},
    onToggleFloatingFpsOverlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Real-Time Thermal Throttling Alert (Shown only when throttling is detected)
        item {
            ThermalThrottlingAlertBanner(
                state = state,
                onDismiss = onDismissThermalAlert
            )
        }

        // 2. Raunak Exploits Branding Hero Card + Live CPU/GPU Temp Pill
        item {
            RaunakExploitsBrandingHero(state = state)
        }

        // 3. Refresh Rate Lock Selector (90 / 120 / 144 / 165 Hz) + 1% Battery Guard
        item {
            RefreshRateLockControlCard(
                state = state,
                onSelectRefreshRate = onSelectRefreshRate
            )
        }

        // 4. Clean 3-Mode Selector (No Extra Clutter Buttons)
        item {
            Text(
                text = "SELECT PERFORMANCE TUNING MODE",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        item {
            BigLiquidGlassModeCard(
                title = "🔥 DIABLO MODE",
                powerBadge = "100% UNCLAMPED LOCK",
                detail = "Zero-Contention 120+ FPS Lock • Frees 99% CPU/GPU for Games • Zero 1% Battery Drop",
                isSelected = state.selectedProfile == PerformanceProfile.DIABLO_MODE,
                accentColor = DiabloRed,
                glowColor = DiabloRedGlow,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectProfile(PerformanceProfile.DIABLO_MODE)
                },
                testTag = "mode_card_diablo"
            )
        }

        item {
            BigLiquidGlassModeCard(
                title = "⚖️ BALANCE MODE",
                powerBadge = "70% ADAPTIVE FLOOR",
                detail = "Optimal Gaming & Frame Pacing • 70% Steady Compute Floor • Cool Thermals & Smooth FPS",
                isSelected = state.selectedProfile == PerformanceProfile.BALANCE_MODE,
                accentColor = ElectricCyan,
                glowColor = ElectricCyanGlow,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectProfile(PerformanceProfile.BALANCE_MODE)
                },
                testTag = "mode_card_balance"
            )
        }

        item {
            BigLiquidGlassModeCard(
                title = "🍃 BATTERY SAVING MODE",
                powerBadge = "25% COOL SAVER",
                detail = "Thermal Cool-Down Mode • Keeps Selected Hz Pinned While Cooling CPU & GPU",
                isSelected = state.selectedProfile == PerformanceProfile.SAVING_MODE,
                accentColor = TelemetryGreen,
                glowColor = TelemetryGreenGlow,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectProfile(PerformanceProfile.SAVING_MODE)
                },
                testTag = "mode_card_saving"
            )
        }

        // 5. Single Primary Start/Stop Reactor Action Button
        item {
            LiquidGlassReactorActionButton(
                state = state,
                onStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartPerformanceMode()
                },
                onStop = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStopPerformanceMode()
                }
            )
        }

        // 6. Real-Time CPU/GPU Thermal Monitor & FPS Stability Card
        item {
            RealTimeThermalMonitorCard(
                state = state,
                onToggleFloatingFpsOverlay = onToggleFloatingFpsOverlay
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RaunakExploitsBrandingHero(state: PerformanceTelemetryState) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val isActive = state.isSessionActive
    val activeColor = when (state.selectedProfile) {
        PerformanceProfile.DIABLO_MODE -> DiabloRed
        PerformanceProfile.BALANCE_MODE -> ElectricCyan
        PerformanceProfile.SAVING_MODE -> TelemetryGreen
    }
    val thermalColor = when {
        state.thermalStatus.isThrottling -> DiabloRed
        state.thermalStatus.cpuTempCelsius >= 60f -> DiabloOrange
        else -> TelemetryGreen
    }

    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            if (isActive) activeColor.copy(alpha = pulseAlpha) else Color.White.copy(alpha = 0.25f),
            if (isActive) activeColor.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.15f),
            Color.Transparent
        )
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, glassBorderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = if (isActive) activeColor else ElectricCyan, spotColor = if (isActive) activeColor else ElectricCyan)
    ) {
        Box(
            modifier = Modifier
                .background(if (isActive && state.selectedProfile == PerformanceProfile.DIABLO_MODE) DiabloGlassGradient else LiquidGlassGradient)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isActive) activeColor else ElectricCyan, CircleShape)
                            .shadow(8.dp, CircleShape, ambientColor = ElectricCyan, spotColor = ElectricCyan)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.raunak_logo),
                            contentDescription = "Raunak Exploits Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RAUNAK EXPLOITS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ZERO-DROP GAMING • THERMAL GUARD",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isActive) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) activeColor else GlassBorder
                        )
                    ) {
                        Text(
                            text = if (isActive) "${state.lockedPowerPercent}% • ${state.selectedRefreshRate.hz}Hz" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) activeColor else TextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.deviceSpecs.phoneDisplayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = thermalColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, thermalColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Thermostat,
                                contentDescription = "CPU GPU Temperature",
                                tint = thermalColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${state.thermalStatus.cpuTempCelsius}° / ${state.thermalStatus.gpuTempCelsius}°C",
                                style = MaterialTheme.typography.labelSmall,
                                color = thermalColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isActive) {
                        val modeText = if (state.antiFpsDropGuardEnabled) "Anti-Drop (${state.gameCpuHeadroomPercent}% Free)" else "Unclamped Max"
                        "$modeText • CPU ${state.realMeasuredThreadDutyPercent}% / GPU ${state.gpuLockedDutyPercent}% • ${state.selectedRefreshRate.label}"
                    } else {
                        "${state.deviceSpecs.androidVersionLabel} • Pick Hz & Mode, then tap Start"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (isActive && state.sessionElapsedSeconds > 0L) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val min = state.sessionElapsedSeconds / 60
                    val sec = state.sessionElapsedSeconds % 60
                    Text(
                        text = "Session Active: ${String.format("%02d:%02d", min, sec)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = activeColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BigLiquidGlassModeCard(
    title: String,
    powerBadge: String,
    detail: String,
    isSelected: Boolean,
    accentColor: Color,
    glowColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else if (isSelected) 1.01f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "modeCardScale"
    )

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            colors = listOf(
                accentColor,
                Color.White.copy(alpha = 0.4f),
                accentColor.copy(alpha = 0.5f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(GlassBorder, Color.White.copy(alpha = 0.05f))
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) GlassSurfaceElevated else GlassSurface,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .then(
                if (isSelected) Modifier.shadow(14.dp, RoundedCornerShape(18.dp), ambientColor = glowColor, spotColor = glowColor) else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.22f),
                                GlassSurfaceElevated,
                                accentColor.copy(alpha = 0.08f)
                            )
                        )
                    } else {
                        LiquidGlassGradient
                    }
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = "Selected",
                            tint = if (isSelected) accentColor else TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.9f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) accentColor else GlassBorder
                        )
                    ) {
                        Text(
                            text = powerBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) accentColor else TextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassReactorActionButton(
    state: PerformanceTelemetryState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isActive = state.isSessionActive
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "reactorScale"
    )

    val activeColor = when (state.selectedProfile) {
        PerformanceProfile.DIABLO_MODE -> DiabloRed
        PerformanceProfile.BALANCE_MODE -> ElectricCyan
        PerformanceProfile.SAVING_MODE -> TelemetryGreen
    }

    val glowColor = if (isActive) DiabloRedGlow else when (state.selectedProfile) {
        PerformanceProfile.DIABLO_MODE -> DiabloRedGlow
        PerformanceProfile.BALANCE_MODE -> ElectricCyanGlow
        PerformanceProfile.SAVING_MODE -> TelemetryGreenGlow
    }

    val buttonBackground = if (isActive) {
        Brush.horizontalGradient(
            listOf(
                DiabloRedDim,
                DiabloRed,
                Color(0xFFFF5252)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                activeColor.copy(alpha = 0.9f),
                CobaltDeep,
                activeColor.copy(alpha = 0.95f)
            )
        )
    }

    val borderBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.6f),
            if (isActive) DiabloRed else activeColor,
            Color.White.copy(alpha = 0.2f)
        )
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderBrush),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = glowColor, spotColor = glowColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (isActive) onStop() else onStart() }
            )
            .testTag(if (isActive) "stop_performance_button" else "start_performance_button")
    ) {
        Box(
            modifier = Modifier
                .background(buttonBackground)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.FlashOn,
                        contentDescription = "Trigger",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                val targetPowerLabel = if (state.customCpuDutyOverride != null || state.customGpuDutyOverride != null) {
                    val basePct = when (state.selectedProfile) {
                        PerformanceProfile.DIABLO_MODE -> 100
                        PerformanceProfile.BALANCE_MODE -> 70
                        PerformanceProfile.SAVING_MODE -> 25
                    }
                    val cpuTarget = state.customCpuDutyOverride ?: if (state.selectedWorkloadFocus.enableCpuHints) basePct else 0
                    val gpuTarget = state.customGpuDutyOverride ?: if (state.selectedWorkloadFocus.enableGpuGameHints) basePct else 0
                    "CPU $cpuTarget% / GPU $gpuTarget%"
                } else {
                    when (state.selectedProfile) {
                        PerformanceProfile.DIABLO_MODE -> "100% MAX POWER"
                        PerformanceProfile.BALANCE_MODE -> "70% BALANCE POWER"
                        PerformanceProfile.SAVING_MODE -> "25% COOL SAVER"
                    }
                }
                val hzLabel = state.selectedRefreshRate.label

                Column {
                    Text(
                        text = if (isActive) "🔥 $targetPowerLabel + $hzLabel LOCKED" else "⚡ START $targetPowerLabel + $hzLabel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) {
                            "${if (state.antiFpsDropGuardEnabled) "Game Anti-Drop" else "Unclamped Max"} • Thermal Monitor ON • Tap to stop"
                        } else {
                            "Stable FPS Lock & Real-Time Thermal Guard (Even at 1%)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
