package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.RefreshRateTarget
import com.example.ui.components.RealTimeThermalMonitorCard
import com.example.ui.components.RefreshRateLockControlCard
import com.example.ui.components.ThermalThrottlingAlertBanner
import com.example.ui.theme.CobaltDeep
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ThermalThrottlingAlertBanner(
                state = state,
                onDismiss = onDismissThermalAlert
            )
        }

        item {
            CompactDeviceHeader(state = state)
        }

        item {
            RefreshRateLockControlCard(
                state = state,
                onSelectRefreshRate = onSelectRefreshRate
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactModeRowCard(
                    title = "🔥 DIABLO MODE",
                    badge = "100% MAX",
                    subtitle = "Full CPU/GPU lock • Auto-frees 99% for BGMI & Free Fire",
                    isSelected = state.selectedProfile == PerformanceProfile.DIABLO_MODE,
                    accentColor = DiabloRed,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(PerformanceProfile.DIABLO_MODE)
                    },
                    testTag = "mode_card_diablo"
                )
                CompactModeRowCard(
                    title = "⚖️ BALANCE MODE",
                    badge = "70% STEADY",
                    subtitle = "Smooth gaming frame pacing with balanced thermals",
                    isSelected = state.selectedProfile == PerformanceProfile.BALANCE_MODE,
                    accentColor = ElectricCyan,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(PerformanceProfile.BALANCE_MODE)
                    },
                    testTag = "mode_card_balance"
                )
                CompactModeRowCard(
                    title = "🍃 SAVING MODE",
                    badge = "25% COOL",
                    subtitle = "Low heat while keeping your selected Hz pinned",
                    isSelected = state.selectedProfile == PerformanceProfile.SAVING_MODE,
                    accentColor = TelemetryGreen,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectProfile(PerformanceProfile.SAVING_MODE)
                    },
                    testTag = "mode_card_saving"
                )
            }
        }

        item {
            PrimaryBoostButton(
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

        item {
            RealTimeThermalMonitorCard(
                state = state,
                onToggleFloatingFpsOverlay = onToggleFloatingFpsOverlay
            )
        }
    }
}

@Composable
private fun CompactDeviceHeader(state: PerformanceTelemetryState) {
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

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isActive) activeColor.copy(alpha = 0.75f) else GlassBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.deviceSpecs.phoneDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isActive) {
                        "CPU ${state.realMeasuredThreadDutyPercent}% • GPU ${state.gpuLockedDutyPercent}% • ${state.selectedRefreshRate.label} LOCKED"
                    } else {
                        "${state.deviceSpecs.androidVersionLabel} • Panel Max ${state.deviceSpecs.panelMaxRefreshRateHz}Hz"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) activeColor else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = thermalColor.copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(1.dp, thermalColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = "Temp",
                        tint = thermalColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${state.thermalStatus.cpuTempCelsius.toInt()}°C",
                        style = MaterialTheme.typography.labelSmall,
                        color = thermalColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactModeRowCard(
    title: String,
    badge: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) GlassSurfaceElevated else GlassSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.8.dp else 1.dp,
            if (isSelected) accentColor else GlassBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.18f), Color.Transparent))
                    } else {
                        LiquidGlassGradient
                    }
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = "Select $title",
                tint = if (isSelected) accentColor else TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) accentColor else TextSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryBoostButton(
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
        label = "boostButtonScale"
    )

    val activeColor = when (state.selectedProfile) {
        PerformanceProfile.DIABLO_MODE -> DiabloRed
        PerformanceProfile.BALANCE_MODE -> ElectricCyan
        PerformanceProfile.SAVING_MODE -> TelemetryGreen
    }
    val glowColor = if (isActive) DiabloRedGlow else ElectricCyanGlow

    val bgBrush = if (isActive) {
        Brush.horizontalGradient(listOf(DiabloRedDim, DiabloRed))
    } else {
        Brush.horizontalGradient(listOf(activeColor.copy(alpha = 0.9f), CobaltDeep, activeColor.copy(alpha = 0.9f)))
    }

    val powerLabel = if (state.customCpuDutyOverride != null || state.customGpuDutyOverride != null) {
        val basePct = when (state.selectedProfile) {
            PerformanceProfile.DIABLO_MODE -> 100
            PerformanceProfile.BALANCE_MODE -> 70
            PerformanceProfile.SAVING_MODE -> 25
        }
        val cpuPct = state.customCpuDutyOverride ?: if (state.selectedWorkloadFocus.enableCpuHints) basePct else 0
        val gpuPct = state.customGpuDutyOverride ?: if (state.selectedWorkloadFocus.enableGpuGameHints) basePct else 0
        "CPU $cpuPct% / GPU $gpuPct%"
    } else {
        when (state.selectedProfile) {
            PerformanceProfile.DIABLO_MODE -> "MAX POWER"
            PerformanceProfile.BALANCE_MODE -> "70% BALANCE"
            PerformanceProfile.SAVING_MODE -> "25% SAVER"
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.8.dp, if (isActive) DiabloRed else activeColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = glowColor, spotColor = glowColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (isActive) onStop() else onStart() }
            )
            .testTag(if (isActive) "stop_performance_button" else "start_performance_button")
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush)
                .padding(vertical = 15.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.FlashOn,
                    contentDescription = if (isActive) "Stop Boost" else "Start Boost",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isActive) {
                        "🔥 LOCKED ($powerLabel + ${state.selectedRefreshRate.label})"
                    } else {
                        "⚡ START MAX LOCK (${state.selectedRefreshRate.label})"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = if (isActive) "Tap to stop lock" else "Locks Max Hz + CPU/GPU Boost",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
