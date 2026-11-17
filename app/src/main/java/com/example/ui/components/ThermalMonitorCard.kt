package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceTelemetryState
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LiquidGlassGradient
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryGreenGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ThermalThrottlingAlertBanner(
    state: PerformanceTelemetryState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val thermal = state.thermalStatus
    val visible = thermal.isThrottling && !state.thermalAlertDismissed

    val infiniteTransition = rememberInfiniteTransition(label = "thermalAlertPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DiabloRed.copy(alpha = 0.22f),
            border = androidx.compose.foundation.BorderStroke(1.8.dp, DiabloRed.copy(alpha = pulseAlpha)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(18.dp), ambientColor = DiabloRedGlow, spotColor = DiabloRedGlow)
                .testTag("thermal_throttling_alert")
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(DiabloRed.copy(alpha = 0.28f), DiabloOrange.copy(alpha = 0.14f))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = "Thermal Throttling Alert",
                    tint = DiabloRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = thermal.alertHeadline ?: "THERMAL THROTTLING DETECTED",
                        style = MaterialTheme.typography.titleSmall,
                        color = DiabloRed,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = thermal.fpsDropCauseExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 17.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss Thermal Alert",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RealTimeThermalMonitorCard(
    state: PerformanceTelemetryState,
    onToggleFloatingFpsOverlay: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val thermal = state.thermalStatus
    val statusColor = when {
        thermal.isThrottling -> DiabloRed
        thermal.cpuTempCelsius >= 60f || thermal.gpuTempCelsius >= 58f -> DiabloOrange
        else -> TelemetryGreen
    }
    val glowColor = when {
        thermal.isThrottling -> DiabloRedGlow
        else -> TelemetryGreenGlow
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            Brush.linearGradient(listOf(statusColor.copy(alpha = 0.65f), GlassBorder))
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = glowColor, spotColor = glowColor)
            .testTag("realtime_thermal_monitor_card")
    ) {
        Column(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = "CPU GPU Thermal Monitor",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REAL-TIME CPU / GPU THERMAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hudEnabled = state.floatingFpsOverlayEnabled
                    val hudColor = if (hudEnabled) ElectricCyan else TextSecondary
                    Surface(
                        onClick = {
                            if (!hudEnabled && !state.refreshRatePermissions.canDrawOverlays) {
                                try {
                                    val intent = android.content.Intent(
                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (_: Throwable) {
                                }
                            }
                            onToggleFloatingFpsOverlay()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = hudColor.copy(alpha = 0.14f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, hudColor.copy(alpha = 0.6f)),
                        modifier = Modifier.testTag("floating_fps_hud_toggle")
                    ) {
                        Text(
                            text = if (hudEnabled) "FPS HUD ON" else "FPS HUD OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = hudColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColor.copy(alpha = 0.16f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = if (thermal.isThrottling) "THROTTLING" else "STABLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThermalMetricBox(
                    label = "CPU TEMP",
                    value = "${thermal.cpuTempCelsius}°C",
                    subtext = if (thermal.cpuTempCelsius >= 72f) "Hot • Clock Limit" else "Prime/Gold Cool",
                    color = tempColor(thermal.cpuTempCelsius),
                    progress = (thermal.cpuTempCelsius / 95f).coerceIn(0.1f, 1f),
                    modifier = Modifier.weight(1f)
                )

                ThermalMetricBox(
                    label = "GPU TEMP",
                    value = "${thermal.gpuTempCelsius}°C",
                    subtext = if (thermal.gpuTempCelsius >= 70f) "Hot • FPS Drop Risk" else "Shader Headroom OK",
                    color = tempColor(thermal.gpuTempCelsius),
                    progress = (thermal.gpuTempCelsius / 95f).coerceIn(0.1f, 1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThermalMetricBox(
                    label = "THERMAL HEADROOM",
                    value = "${thermal.thermalHeadroomPercent}%",
                    subtext = thermal.sensorSourceLabel,
                    color = if (thermal.thermalHeadroomPercent < 25) DiabloRed else ElectricCyan,
                    progress = (thermal.thermalHeadroomPercent / 100f).coerceIn(0.05f, 1f),
                    modifier = Modifier.weight(1f)
                )

                ThermalMetricBox(
                    label = "GAMING STABILITY",
                    value = "${state.liveMeasuredFps} FPS • ${state.fpsStabilityPercent}%",
                    subtext = "Min ${state.minMeasuredFps} • Drops ${state.droppedFramesCount}",
                    color = if (state.droppedFramesCount > 3 || state.fpsStabilityPercent < 90) DiabloOrange else TelemetryGreen,
                    progress = (state.fpsStabilityPercent / 100f).coerceIn(0.1f, 1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual CPU/GPU Thermal Trend Graph (24 live samples)
            DualThermalTrendGraph(
                cpuHistory = state.cpuTempHistory,
                gpuHistory = state.gpuTempHistory
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.10f),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = thermal.fpsDropCauseExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun ThermalMetricBox(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "thermalProgress"
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
                        .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color)))
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.78f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DualThermalTrendGraph(
    cpuHistory: List<Float>,
    gpuHistory: List<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "THERMAL TIMELINE (24 SAMPLES)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "● CPU",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiabloOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = "● GPU",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x22000000))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (cpuHistory.size < 2 || gpuHistory.size < 2) return@Canvas
            val minT = 25f
            val maxT = 95f
            val stepX = size.width / (cpuHistory.size - 1)

            fun buildPath(values: List<Float>): Path {
                val path = Path()
                values.forEachIndexed { idx, temp ->
                    val norm = ((temp - minT) / (maxT - minT)).coerceIn(0f, 1f)
                    val x = idx * stepX
                    val y = size.height - (norm * (size.height - 8f)) - 4f
                    if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                return path
            }

            // 72°C throttling threshold guide line
            val warnNorm = ((72f - minT) / (maxT - minT)).coerceIn(0f, 1f)
            val warnY = size.height - (warnNorm * (size.height - 8f)) - 4f
            drawLine(
                color = DiabloRed.copy(alpha = 0.35f),
                start = Offset(0f, warnY),
                end = Offset(size.width, warnY),
                strokeWidth = 1.dp.toPx()
            )

            drawPath(
                path = buildPath(gpuHistory),
                color = ElectricCyan,
                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = buildPath(cpuHistory),
                color = DiabloOrange,
                style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

private fun tempColor(tempCelsius: Float): Color {
    return when {
        tempCelsius >= 72f -> DiabloRed
        tempCelsius >= 58f -> DiabloOrange
        else -> TelemetryGreen
    }
}
