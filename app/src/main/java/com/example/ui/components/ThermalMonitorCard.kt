package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PerformanceTelemetryState
import com.example.ui.theme.DiabloOrange
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LiquidGlassGradient
import com.example.ui.theme.TelemetryGreen
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
            shape = RoundedCornerShape(16.dp),
            color = DiabloRed.copy(alpha = 0.22f),
            border = androidx.compose.foundation.BorderStroke(1.6.dp, DiabloRed.copy(alpha = pulseAlpha)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("thermal_throttling_alert")
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(DiabloRed.copy(alpha = 0.28f), DiabloOrange.copy(alpha = 0.14f))
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = "Thermal Throttling Alert",
                    tint = DiabloRed,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = thermal.alertHeadline ?: "THERMAL THROTTLING DETECTED",
                        style = MaterialTheme.typography.titleSmall,
                        color = DiabloRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = thermal.fpsDropCauseExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
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

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_thermal_monitor_card")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = "Live Telemetry",
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE TELEMETRY",
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

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
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactStatBox(
                    label = "CPU",
                    value = "${state.realMeasuredThreadDutyPercent}%",
                    color = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
                CompactStatBox(
                    label = "GPU",
                    value = "${state.gpuLockedDutyPercent}%",
                    color = DiabloRed,
                    modifier = Modifier.weight(1f)
                )
                CompactStatBox(
                    label = "FPS",
                    value = "${state.liveMeasuredFps}",
                    color = TelemetryGreen,
                    modifier = Modifier.weight(1f)
                )
                CompactStatBox(
                    label = "TEMP",
                    value = "${thermal.cpuTempCelsius.toInt()}°C",
                    color = tempColor(thermal.cpuTempCelsius),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactStatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = GlassSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
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
