package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedDim
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Status Card
        item {
            HeroHardwareStatusCard(state = state)
        }

        // The 3 Performance Modes: Diablo, Balance, Saving
        item {
            PrimaryThreeModeSelectorCard(
                state = state,
                onSelectProfile = { profile ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectProfile(profile)
                }
            )
        }

        // Primary START / STOP Action Button
        item {
            PrimaryExecutionButtonCard(
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

        // Live Telemetry Gauges (CPU, GPU, Thermals)
        item {
            HardwareTelemetryGaugesCard(state = state)
        }

        // Real-Time 100% Stability Graph
        item {
            LivePowerStabilityGraphCard(state = state)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroHardwareStatusCard(state: PerformanceTelemetryState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.2.dp,
                if (isActive) activeColor.copy(alpha = pulseAlpha) else CarbonBorder,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isActive) activeColor else TextSecondary.copy(alpha = 0.4f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "${state.selectedProfile.title.uppercase()} ACTIVE" else "STANDBY • READY",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isActive) activeColor else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isActive) activeColor.copy(alpha = 0.15f) else ObsidianBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) activeColor else CarbonBorder
                    )
                ) {
                    Text(
                        text = if (isActive) "${state.lockedPowerPercent}% LOCKED" else "0% IDLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) activeColor else TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.deviceSpecs.phoneDisplayName,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isActive) {
                    "CPU & GPU locked at ${state.lockedPowerPercent}% duty • Zero throttling active"
                } else {
                    "Select Diablo, Balance, or Saving Mode then press START"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (isActive && state.sessionElapsedSeconds > 0L) {
                Spacer(modifier = Modifier.height(10.dp))
                val min = state.sessionElapsedSeconds / 60
                val sec = state.sessionElapsedSeconds % 60
                Text(
                    text = "Session Duration: ${String.format("%02d:%02d", min, sec)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = activeColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PrimaryThreeModeSelectorCard(
    state: PerformanceTelemetryState,
    onSelectProfile: (PerformanceProfile) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PERFORMANCE TUNING MODES",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeSelectionChip(
                    title = "🔥 Diablo Mode",
                    subtitle = "100% Lock",
                    isSelected = state.selectedProfile == PerformanceProfile.DIABLO_MODE,
                    accentColor = DiabloRed,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.DIABLO_MODE) },
                    testTag = "mode_chip_diablo"
                )
                ModeSelectionChip(
                    title = "⚖️ Balance Mode",
                    subtitle = "70% Target",
                    isSelected = state.selectedProfile == PerformanceProfile.BALANCE_MODE,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.BALANCE_MODE) },
                    testTag = "mode_chip_balance"
                )
                ModeSelectionChip(
                    title = "🍃 Saving Mode",
                    subtitle = "25% Target",
                    isSelected = state.selectedProfile == PerformanceProfile.SAVING_MODE,
                    accentColor = TelemetryGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.SAVING_MODE) },
                    testTag = "mode_chip_saving"
                )
            }
        }
    }
}

@Composable
private fun ModeSelectionChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CobaltDeep else ObsidianBg,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) accentColor else CarbonBorder
        ),
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) accentColor else TextSecondary.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PrimaryExecutionButtonCard(
    state: PerformanceTelemetryState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isActive = state.isSessionActive
    val buttonColor by animateColorAsState(
        targetValue = if (isActive) DiabloRedDim else when (state.selectedProfile) {
            PerformanceProfile.DIABLO_MODE -> DiabloRed
            PerformanceProfile.BALANCE_MODE -> ElectricCyan
            PerformanceProfile.SAVING_MODE -> TelemetryGreen
        },
        label = "btnColor"
    )

    Button(
        onClick = { if (isActive) onStop() else onStart() },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag(if (isActive) "stop_performance_button" else "start_performance_button"),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = if (isActive || state.selectedProfile == PerformanceProfile.DIABLO_MODE) TextPrimary else ObsidianBg
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Icon(
            imageVector = if (isActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
            contentDescription = if (isActive) "Stop Performance" else "Start Performance",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (isActive) "STOP SESSION (RETURN TO NORMAL IDLE)" else "START ${state.selectedProfile.title.uppercase()} (LOCK ${state.selectedProfile.subtitle})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun HardwareTelemetryGaugesCard(state: PerformanceTelemetryState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REAL HARDWARE TELEMETRY GAUGES",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryMetricBox(
                    label = "CPU POWER LOCK",
                    value = if (state.isSessionActive) "${state.lockedPowerPercent}%" else "NORMAL IDLE",
                    subtext = "${state.deviceSpecs.cpuCoreConfiguration} Active",
                    color = if (state.isSessionActive) DiabloRed else TextSecondary,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricBox(
                    label = "GPU FLOOR LOCK",
                    value = if (state.isSessionActive) "${state.gpuLockedDutyPercent}%" else "SYSTEM IDLE",
                    subtext = "eglSwapInterval(0)",
                    color = if (state.isSessionActive) ElectricCyan else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryMetricBox(
                    label = "BATTERY TEMP",
                    value = "${state.batteryStatus.temperatureCelsius}°C",
                    subtext = "Battery: ${state.batteryStatus.levelPercent}%",
                    color = if (state.batteryStatus.temperatureCelsius > 44f) DiabloOrange else TelemetryGreen,
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricBox(
                    label = "THERMAL PROTECTION",
                    value = if (state.isSessionActive) "BYPASS ACTIVE" else "NOMINAL",
                    subtext = "Zero Governor Throttle",
                    color = if (state.isSessionActive) TelemetryGreen else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricBox(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
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
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
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
private fun LivePowerStabilityGraphCard(state: PerformanceTelemetryState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTINUOUS DUTY STABILITY (24 SAMPLES)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (state.isSessionActive) "${state.lockedPowerPercent}% LOCKED" else "0% IDLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isSessionActive) DiabloRed else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val history = state.powerStabilityHistory
            val lineColor = if (state.isSessionActive) DiabloRed else TextSecondary.copy(alpha = 0.4f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(ObsidianBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                if (history.size < 2) return@Canvas
                val stepX = size.width / (history.size - 1)
                val path = Path()

                history.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value.coerceIn(0, 100) / 100f * (size.height - 12f)) - 6f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Grid floor guide line
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(0f, size.height - 6f),
                    end = Offset(size.width, size.height - 6f),
                    strokeWidth = 1.dp.toPx()
                )
                // 100% ceiling guide line
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
