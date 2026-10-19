package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloGlassGradient
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedDim
import com.example.ui.theme.DiabloRedGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GreenGlassGradient
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
        // 1. Raunak Exploits Branding Hero Card
        item {
            RaunakExploitsBrandingHero(state = state)
        }

        // 2. Big, Clear, High-Impact Liquid Glass Mode Selection Cards
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
                detail = "Continuous 100% CPU & GPU Duty • Zero Throttling • Max FPS in Competitive Games & Benchmarks",
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
                detail = "Optimal Gaming & Frame Pacing • 70% Steady Compute Floor • Balanced Thermals & Smooth Performance",
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
                detail = "Energy Saving & Rapid Thermal Cool-Down • 25% Duty Restriction • Maximum Battery Life & Cool Hardware",
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

        // 3. Liquid Glass Reactor Action Button (Redesigned, Futuristic, Elegant)
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

        // 4. Real Hardware Liquid Glass Telemetry Gauges
        item {
            LiquidHardwareTelemetryGauges(state = state)
        }

        // 5. Liquid Power Stability Wave Graph
        item {
            LiquidPowerStabilityGraph(state = state)
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
                            text = "BY RAUNAK EXPLOITS TECHNOLOGY",
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
                            text = if (isActive) "${state.lockedPowerPercent}% LOCKED" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) activeColor else TextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = state.deviceSpecs.phoneDisplayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isActive) {
                        "Hardware compute lock engaged at ${state.lockedPowerPercent}% • Zero governor throttling"
                    } else {
                        "Select a mode below and tap the reactor button to lock hardware clocks"
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

                Column {
                    Text(
                        text = if (isActive) "RELEASE HARDWARE LOCK" else "ENGAGE ${state.selectedProfile.title.uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isActive) "Tap to return CPU & GPU to normal system idle" else "Tap to lock ${state.selectedProfile.subtitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidHardwareTelemetryGauges(state: PerformanceTelemetryState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(14.dp)
        ) {
            Text(
                text = "REAL HARDWARE TELEMETRY GAUGES",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidTelemetryMetricBox(
                    label = "CPU POWER LOCK",
                    value = if (state.isSessionActive) "${state.lockedPowerPercent}%" else "NORMAL IDLE",
                    subtext = "${state.deviceSpecs.cpuCoreConfiguration} Active",
                    color = if (state.isSessionActive) DiabloRed else TextSecondary,
                    progress = if (state.isSessionActive) state.lockedPowerPercent / 100f else 0f,
                    modifier = Modifier.weight(1f)
                )

                LiquidTelemetryMetricBox(
                    label = "GPU FLOOR LOCK",
                    value = if (state.isSessionActive) "${state.gpuLockedDutyPercent}%" else "SYSTEM IDLE",
                    subtext = "eglSwapInterval(0)",
                    color = if (state.isSessionActive) ElectricCyan else TextSecondary,
                    progress = if (state.isSessionActive) state.gpuLockedDutyPercent / 100f else 0f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidTelemetryMetricBox(
                    label = "BATTERY TEMP",
                    value = "${state.batteryStatus.temperatureCelsius}°C",
                    subtext = "Battery: ${state.batteryStatus.levelPercent}%",
                    color = if (state.batteryStatus.temperatureCelsius > 44f) DiabloOrange else TelemetryGreen,
                    progress = (state.batteryStatus.temperatureCelsius / 60f).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )

                LiquidTelemetryMetricBox(
                    label = "THERMAL PROTECTION",
                    value = if (state.isSessionActive) "BYPASS ACTIVE" else "NOMINAL",
                    subtext = "Zero Governor Throttle",
                    color = if (state.isSessionActive) TelemetryGreen else TextSecondary,
                    progress = if (state.isSessionActive) 1f else 0.2f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LiquidTelemetryMetricBox(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "metricProgress"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GlassSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(color.copy(alpha = 0.6f), color)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LiquidPowerStabilityGraph(state: PerformanceTelemetryState) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTINUOUS DUTY STABILITY (24 SAMPLES)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (state.isSessionActive) "${state.lockedPowerPercent}% LOCKED" else "0% IDLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isSessionActive) DiabloRed else TextSecondary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val history = state.powerStabilityHistory
            val lineColor = if (state.isSessionActive) DiabloRed else TextSecondary.copy(alpha = 0.4f)
            val glowColor = if (state.isSessionActive) DiabloRedGlow else Color.Transparent

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x20000000))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (history.size < 2) return@Canvas
                val stepX = size.width / (history.size - 1)
                val path = Path()
                val fillPath = Path()

                fillPath.moveTo(0f, size.height)

                history.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value.coerceIn(0, 100) / 100f * (size.height - 12f)) - 6f
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo(size.width, size.height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(glowColor, Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, size.height - 6f),
                    end = Offset(size.width, size.height - 6f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, 6f),
                    end = Offset(size.width, 6f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}
