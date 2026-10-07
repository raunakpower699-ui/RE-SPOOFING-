package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ActiveOptimizationIndicator
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.ThermalStatusLevel
import com.example.ui.components.AnimatedGlowButton
import com.example.ui.components.AnimatedGlowOutlinedButton
import com.example.ui.components.DeviceLiveSpecsCard
import com.example.ui.components.RaunakExploitsCreatorBadge
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.TelemetryStatusCard
import com.example.ui.components.VivoBrandingLogo
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: PerformanceTelemetryState,
    customLogoUri: String?,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onQuickProfileSelect: (PerformanceProfile) -> Unit,
    onOpenLogoPicker: () -> Unit,
    onToggleVivoIqooSimulation: (Boolean) -> Unit,
    onToggleOriginOs6Overdrive: (Boolean) -> Unit = {},
    onToggleTargetWindowHookState: (Boolean) -> Unit = {},
    onToggleAutoRestoreOnMinimize: (Boolean) -> Unit = {},
    onToggleRenderScaleSpoof: (Boolean) -> Unit = {},
    onToggleVSyncDisableSwapZero: (Boolean) -> Unit = {},
    onLaunchVolumeShaderPipeline: () -> Unit = {},
    onToggleNoTouchPowerLock: (Boolean) -> Unit = {},
    onToggleAntiThrottleBooster: (Boolean) -> Unit = {},
    onToggleVivoGameCenterPulse: (Boolean) -> Unit = {},
    onToggleLowLatencyAudioDspLock: (Boolean) -> Unit = {},
    onToggleMemoryBandwidthPrefetch: (Boolean) -> Unit = {},
    onToggleMinimalPostProcessingDisplay: (Boolean) -> Unit = {},
    onToggleTouchSensorBoost: (Boolean) -> Unit = {},
    onToggleStorageIoBoost: (Boolean) -> Unit = {},
    onActivateAllMaxHardwareNow: () -> Unit = {},
    onPurgeBackgroundAppsNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. RE Spoofing Identity & Vivo/iQOO Exclusive Branding Header
        item {
            IdentityHeaderCard(
                state = state,
                customLogoUri = customLogoUri
            )
        }

        // 2. Created by Raunak Exploits Signature Badge
        item {
            RaunakExploitsCreatorBadge()
        }

        // 3. Vivo & iQOO Hardware Gate Status Card
        item {
            VivoIqooHardwareGateBanner(
                state = state,
                onToggleVivoIqooSimulation = onToggleVivoIqooSimulation
            )
        }

        // 4. Hero Performance Mode Control Deck (START / STOP + Status + Quick OriginOS 6 / Diablo Mode)
        item {
            PerformanceControlHeroCard(
                state = state,
                onStartClicked = onStartClicked,
                onStopClicked = onStopClicked,
                onQuickProfileSelect = onQuickProfileSelect
            )
        }

        // 5. [SYSTEM DIRECTIVE: DYNAMIC_PER_APP_SCALE_OVERDRIVE] (0.5x App-Only Scale & 1.0x Auto-Restore)
        item {
            OriginOs6OverdriveDirectiveCard(
                state = state,
                onToggleOriginOs6Overdrive = onToggleOriginOs6Overdrive,
                onToggleTargetWindowHookState = onToggleTargetWindowHookState,
                onToggleAutoRestoreOnMinimize = onToggleAutoRestoreOnMinimize,
                onToggleRenderScaleSpoof = onToggleRenderScaleSpoof,
                onToggleVSyncDisableSwapZero = onToggleVSyncDisableSwapZero,
                onLaunchVolumeShaderPipeline = onLaunchVolumeShaderPipeline
            )
        }

        // 6. 97%–100% Constant CPU, OpenGL ES 3.0/2.0 GPU, LPDDR5X, Audio DSP, Sensor & UFS Max Lock Card
        item {
            ConstantPowerLockAndThermalBoosterCard(
                state = state,
                onToggleNoTouchPowerLock = onToggleNoTouchPowerLock,
                onToggleAntiThrottleBooster = onToggleAntiThrottleBooster,
                onToggleVivoGameCenterPulse = onToggleVivoGameCenterPulse,
                onToggleLowLatencyAudioDspLock = onToggleLowLatencyAudioDspLock,
                onToggleMemoryBandwidthPrefetch = onToggleMemoryBandwidthPrefetch,
                onToggleMinimalPostProcessingDisplay = onToggleMinimalPostProcessingDisplay,
                onToggleTouchSensorBoost = onToggleTouchSensorBoost,
                onToggleStorageIoBoost = onToggleStorageIoBoost,
                onActivateAllMaxHardwareNow = onActivateAllMaxHardwareNow,
                onPurgeBackgroundAppsNow = onPurgeBackgroundAppsNow
            )
        }

        // 7. DIABLO MODE & ORIGINOS 6 Overdrive HUD Card
        item {
            AnimatedVisibility(
                visible = state.selectedProfile == PerformanceProfile.DIABLO_MODE ||
                    state.selectedProfile == PerformanceProfile.ORIGINOS_6_OVERDRIVE
            ) {
                DiabloModeOverdriveHudCard(state = state)
            }
        }

        // 7. Live Phone Information Card (Phone Name, Android Version, SoC, RAM, Storage, Display)
        item {
            DeviceLiveSpecsCard(
                specs = state.deviceSpecs,
                battery = state.batteryStatus
            )
        }

        // 8. Thermal Protection & Battery Warning Banners
        item {
            ThermalAndBatteryProtectionBanner(state = state)
        }

        // 9. Four Primary Status Cards (CPU, GPU, Thermal, Foreground Service & Battery)
        item {
            Text(
                text = "REAL-TIME CPU, GPU, LPDDR, AUDIO, SENSOR & UFS TELEMETRY",
                style = MaterialTheme.typography.labelLarge,
                color = if (state.selectedProfile == PerformanceProfile.DIABLO_MODE) Color(0xFFFF1744) else ElectricCyan,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            val cpuColor = when {
                !state.compatibility.isVivoOrIqoo -> TelemetryRed
                state.thermalStatus.isThrottling && state.isSessionActive -> TelemetryAmber
                state.isSessionActive && state.selectedProfile == PerformanceProfile.DIABLO_MODE -> Color(0xFFFF1744)
                state.isSessionActive && state.cpuStatus.requestState.startsWith("ACTIVE") -> ElectricCyan
                else -> TextSecondary
            }

            TelemetryStatusCard(
                title = "CPU — 97%–100% Constant Max Lock",
                subtitleLabel = "Multi-Core FPU/Matrix + ARMv8 CRC32C ALU + Direct LPDDR Stride + 5x ADPF",
                stateBadge = "Performance request: ${state.cpuStatus.requestState}",
                stateColor = cpuColor,
                icon = Icons.Filled.Memory,
                primaryMetric = "${state.cpuStatus.readableFrequenciesSummary} • ${state.cpuStatus.architecture}",
                secondaryDetail = state.cpuStatus.statusDetail,
                testTag = "cpu_status_card"
            )
        }

        item {
            val gpuColor = when {
                !state.compatibility.isVivoOrIqoo -> TelemetryRed
                state.thermalStatus.isThrottling && state.isSessionActive -> TelemetryAmber
                state.isSessionActive && state.selectedProfile == PerformanceProfile.DIABLO_MODE -> Color(0xFFFFB300)
                state.isSessionActive && state.gpuStatus.requestState.startsWith("ACTIVE") -> CobaltTurbo
                else -> TextSecondary
            }

            TelemetryStatusCard(
                title = "GPU — OpenGL ES 2.0 97%–100% Max Lock",
                subtitleLabel = "EGL14 Pbuffer ALU + Texture2D VRAM Fragment Shaders & Peak VSYNC Lock",
                stateBadge = "Performance request: ${state.gpuStatus.requestState}",
                stateColor = gpuColor,
                icon = Icons.Filled.DeveloperBoard,
                primaryMetric = "SoC / Graphics: ${state.gpuStatus.hardwareModel} • GPU Duty: ${if (state.isSessionActive) "${state.gpuLockedDutyPercent.coerceIn(97, 100)}%" else "0%"}",
                secondaryDetail = state.gpuStatus.statusDetail,
                testTag = "gpu_status_card"
            )
        }

        item {
            val thermalColor = when (state.thermalStatus.level) {
                ThermalStatusLevel.NORMAL -> TelemetryGreen
                ThermalStatusLevel.WARM -> TelemetryAmber
                ThermalStatusLevel.THROTTLING -> TelemetryRed
            }
            val headroomStr = state.thermalStatus.thermalHeadroom?.let {
                String.format(Locale.US, "%.2f", it)
            } ?: "OS Managed"

            TelemetryStatusCard(
                title = "Thermal — Protected & Boosted",
                subtitleLabel = stringResource(id = R.string.thermal_protection_active),
                stateBadge = "Thermal: ${state.thermalStatus.level.displayLabel}",
                stateColor = thermalColor,
                icon = Icons.Filled.Thermostat,
                primaryMetric = String.format(
                    Locale.US,
                    "Battery Temp: %.1f°C • Sustained Target: %d%% • Headroom: %s",
                    state.thermalStatus.batteryTempCelsius,
                    state.thermalStatus.sustainedPowerScorePercent,
                    headroomStr
                ),
                secondaryDetail = state.thermalStatus.warningBannerText
                    ?: state.thermalStatus.level.statusSummary,
                testTag = "thermal_status_card"
            )
        }

        item {
            val serviceColor = if (state.foregroundServiceRunning && state.isSessionActive) {
                TelemetryGreen
            } else {
                TextSecondary
            }
            val serviceBadge = if (state.foregroundServiceRunning && state.isSessionActive) {
                "Service — Non-Stop Running"
            } else {
                "Service — Inactive"
            }

            TelemetryStatusCard(
                title = "Non-Stop Service & Battery Status",
                subtitleLabel = "Runs Unlimited Until Notification Panel Exit",
                stateBadge = serviceBadge,
                stateColor = serviceColor,
                icon = if (state.batteryStatus.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                primaryMetric = "Battery: ${state.batteryStatus.levelPercent}% (${state.batteryStatus.powerSourceLabel}) • ${state.batteryStatus.voltageMv} mV",
                secondaryDetail = if (state.foregroundServiceRunning && state.isSessionActive) {
                    "START_STICKY + stopWithTask=false + 5s Notification Heartbeat + CPU PARTIAL_WAKE_LOCK active. Runs non-stop until you press EXIT / STOP in Notification Panel."
                } else {
                    "Foreground service stopped. Zero background CPU/GPU consumption."
                },
                testTag = "service_battery_status_card"
            )
        }

        // 10. Active Optimization Indicators
        item {
            Text(
                text = "ACTIVE OPTIMIZATION INDICATORS",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(state.activeIndicators, key = { it.id }) { indicator ->
            OptimizationIndicatorRow(indicator = indicator)
        }

        // 11. Live Engine Event Log
        item {
            EngineEventLogCard(logs = state.eventLogs)
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Dedicated 97%–100% Constant CPU, OpenGL ES 2.0 GPU, LPDDR5X, Audio DSP, IMU Sensor & UFS Max Lock Card.
 * Eliminates all 0%, 18%, 70%, 73%, and 80% drops!
 */
@Composable
private fun ConstantPowerLockAndThermalBoosterCard(
    state: PerformanceTelemetryState,
    onToggleNoTouchPowerLock: (Boolean) -> Unit,
    onToggleAntiThrottleBooster: (Boolean) -> Unit,
    onToggleVivoGameCenterPulse: (Boolean) -> Unit,
    onToggleLowLatencyAudioDspLock: (Boolean) -> Unit,
    onToggleMemoryBandwidthPrefetch: (Boolean) -> Unit,
    onToggleMinimalPostProcessingDisplay: (Boolean) -> Unit,
    onToggleTouchSensorBoost: (Boolean) -> Unit,
    onToggleStorageIoBoost: (Boolean) -> Unit,
    onActivateAllMaxHardwareNow: () -> Unit,
    onPurgeBackgroundAppsNow: () -> Unit
) {
    val isLockedActive = state.isSessionActive && state.noTouchPowerLockEnabled
    val isDiablo = state.selectedProfile == PerformanceProfile.DIABLO_MODE
    val borderBrush = Brush.horizontalGradient(
        colors = if (isLockedActive) {
            listOf(TelemetryGreen, ElectricCyan, Color(0xFFFFB300))
        } else {
            listOf(ElectricCyan.copy(alpha = 0.55f), Color(0xFFFFB300).copy(alpha = 0.55f))
        }
    )

    var scanPhase by remember { mutableFloatStateOf(0f) }
    var liveFpsEstimate by remember { mutableIntStateOf(state.deviceSpecs.displayRefreshRateHz) }

    LaunchedEffect(state.isSessionActive, state.noTouchPowerLockEnabled, state.vivoGameCenterInstantPulseEnabled) {
        if (state.isSessionActive && (state.noTouchPowerLockEnabled || state.vivoGameCenterInstantPulseEnabled)) {
            while (isActive) {
                scanPhase = (scanPhase + 0.04f) % 1.0f
                liveFpsEstimate = state.liveMeasuredFps.coerceAtLeast(state.deviceSpecs.displayRefreshRateHz)
                delay(50L)
            }
        } else {
            scanPhase = 0f
        }
    }

    val cpuLockedVal = if (state.isSessionActive && state.noTouchPowerLockEnabled) {
        state.lockedPowerPercent.coerceIn(97, 100)
    } else {
        state.lockedPowerPercent
    }
    val gpuLockedVal = if (state.isSessionActive && state.noTouchPowerLockEnabled) {
        state.gpuLockedDutyPercent.coerceIn(97, 100)
    } else {
        state.gpuLockedDutyPercent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.6.dp, borderBrush, RoundedCornerShape(18.dp))
            .testTag("constant_power_lock_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = "97%–100% Constant Power Lock",
                        tint = if (isLockedActive) TelemetryGreen else Color(0xFFFFB300),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "97%–100% FULL HARDWARE MAX LOCK",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "CPU (FPU+CRC32C) • GLES20 GPU • LPDDR5X • Audio DSP • IMU • UFS I/O",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLockedActive) TelemetryGreen else ElectricCyan
                        )
                    }
                }

                StatusPillBadge(
                    text = if (state.isSessionActive) {
                        "$cpuLockedVal% LOCKED"
                    } else {
                        "97%–100% ARMED"
                    },
                    color = if (state.isSessionActive) TelemetryGreen else Color(0xFFFFB300),
                    testTag = "locked_power_percent_badge"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hardware-Accelerated 97%-100% Stability Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianBg)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                val history = state.powerStabilityHistory
                val primaryGraphColor = when {
                    !state.isSessionActive -> ElectricCyan.copy(alpha = 0.45f)
                    isDiablo -> Color(0xFFFF1744)
                    else -> TelemetryGreen
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    if (state.isSessionActive && (state.noTouchPowerLockEnabled || state.vivoGameCenterInstantPulseEnabled)) {
                        val radiusBase = (w.coerceAtLeast(100f) * 0.45f)
                        val cx = w * scanPhase
                        val cy = h * 0.5f
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryGraphColor.copy(alpha = 0.18f),
                                    ElectricCyan.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                center = Offset(cx, cy),
                                radius = radiusBase
                            ),
                            topLeft = Offset.Zero,
                            size = Size(w, h)
                        )
                    }

                    val count = history.size.coerceAtLeast(2)
                    val barGap = 4.dp.toPx()
                    val totalGap = barGap * (count - 1)
                    val barWidth = ((w - totalGap) / count).coerceAtLeast(3f)

                    val floorY = h * (1f - 0.86f)
                    drawLine(
                        color = Color(0xFFFFB300).copy(alpha = 0.65f),
                        start = Offset(0f, floorY),
                        end = Offset(w, floorY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val linePath = Path()
                    history.forEachIndexed { idx, pct ->
                        val normalized = if (state.isSessionActive && pct > 0) {
                            ((pct - 50).coerceIn(42, 50) / 50f).coerceIn(0.86f, 0.99f)
                        } else {
                            0.20f
                        }
                        val barHeight = h * normalized
                        val x = idx * (barWidth + barGap)
                        val y = h - barHeight

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryGraphColor.copy(alpha = if (state.isSessionActive) 0.58f else 0.22f),
                                    primaryGraphColor.copy(alpha = 0.08f)
                                ),
                                startY = y,
                                endY = h
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )

                        val centerX = x + barWidth / 2f
                        if (idx == 0) {
                            linePath.moveTo(centerX, y)
                        } else {
                            linePath.lineTo(centerX, y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = primaryGraphColor,
                        style = Stroke(width = 2.4.dp.toPx())
                    )

                    if (state.isSessionActive) {
                        val scanX = w * scanPhase
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ElectricCyan,
                                    Color.White,
                                    ElectricCyan,
                                    Color.Transparent
                                )
                            ),
                            start = Offset(scanX, 0f),
                            end = Offset(scanX, h),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.isSessionActive) {
                            "CPU: $cpuLockedVal% LOCKED • GPU: $gpuLockedVal% LOCKED"
                        } else {
                            "STANDBY — PRESS START FOR 97%–100% LOCK"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.isSessionActive) Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.isSessionActive) {
                            "GLES20 + $liveFpsEstimate FPS"
                        } else {
                            "PURGED: ${state.purgedBackgroundAppsCount} APPS"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // One-Tap Ignite All Max Hardware Subsystems Button
            AnimatedGlowButton(
                text = "IGNITE ALL MAX HARDWARE SUBSYSTEMS (100% REAL)",
                icon = Icons.Filled.LocalFireDepartment,
                onClick = onActivateAllMaxHardwareNow,
                enabled = true,
                baseColor = Color(0xFFFF1744),
                pressedGlowColor = Color(0xFFFFB300),
                contentColor = Color.White,
                testTag = "ignite_all_max_hardware_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedGlowOutlinedButton(
                text = if (state.purgedBackgroundAppsCount > 0) {
                    "KILL BACKGROUND APPS & BOOST RAM (${state.purgedBackgroundAppsCount} Purged)"
                } else {
                    "KILL BACKGROUND APPS & BOOST RAM NOW"
                },
                icon = Icons.Filled.Bolt,
                onClick = onPurgeBackgroundAppsNow,
                accentColor = ElectricCyan,
                testTag = "purge_background_apps_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            BoosterToggleRow(
                title = "97%–100% Constant CPU (FPU + CRC32C) & OpenGL ES 2.0 GPU Lock",
                subtitle = "Runs continuous Matrix/FPU + ARMv8 CRC32C ALU worker threads + OpenGL ES 2.0 EGL14 Pbuffer shaders so CPU & GPU never drop below 97%",
                checked = state.noTouchPowerLockEnabled,
                onCheckedChange = onToggleNoTouchPowerLock,
                accentColor = TelemetryGreen,
                testTag = "toggle_no_touch_power_lock"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "LPDDR5 / 5X Memory Controller 64B Cache-Line Prefetch Lock",
                subtitle = if (state.isSessionActive && state.memoryBandwidthPrefetchEnabled) {
                    "ACTIVE: ${state.memoryBandwidthMbPerSec} MB/s across native 256KB off-heap Direct ByteBuffer (prevents RAM bus downclocking)"
                } else {
                    "Strides 64-byte cache lines across a 256KB native Direct ByteBuffer to keep the SoC LPDDR memory bus locked at max speed"
                },
                checked = state.memoryBandwidthPrefetchEnabled,
                onCheckedChange = onToggleMemoryBandwidthPrefetch,
                accentColor = ElectricCyan,
                testTag = "toggle_memory_bandwidth_prefetch"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "Low-Latency Game Audio DSP Fast-Mixer Lock",
                subtitle = "Holds AudioTrack in PERFORMANCE_MODE_LOW_LATENCY + USAGE_GAME (${state.audioHardwareSampleRateHz}Hz / ${state.audioFastMixerBufferFrames} frames) so the SoC Audio DSP never sleeps",
                checked = state.lowLatencyAudioDspLockEnabled,
                onCheckedChange = onToggleLowLatencyAudioDspLock,
                accentColor = TelemetryGreen,
                testTag = "toggle_low_latency_audio_dsp"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "High-Rate Game IMU Sensor & Unbuffered Touch Pipeline Lock",
                subtitle = if (state.isSessionActive && state.touchSensorBoostEnabled) {
                    "ACTIVE: ${state.sensorSamplingHz}Hz SENSOR_DELAY_GAME (${state.sensorHardwareName.ifBlank { "Hardware IMU" }}) + Unbuffered Input Dispatch"
                } else {
                    "Locks SensorManager in SENSOR_DELAY_GAME mode and enables Window requestUnbufferedDispatch for zero touch/gyro batching lag"
                },
                checked = state.touchSensorBoostEnabled,
                onCheckedChange = onToggleTouchSensorBoost,
                accentColor = Color(0xFFFFB300),
                testTag = "toggle_touch_sensor_boost"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "UFS 3.1 / 4.0 Direct 16KB FileChannel Storage I/O Keep-Alive",
                subtitle = if (state.isSessionActive && state.storageIoBoostEnabled) {
                    "ACTIVE: ${state.storageThroughputMbPerSec} MB/s page-aligned Direct ByteBuffer FileChannel keep-alive (prevents UFS link sleep)"
                } else {
                    "Executes non-blocking 16KB page-aligned Direct ByteBuffer FileChannel cycles so UFS storage never enters power-save link sleep"
                },
                checked = state.storageIoBoostEnabled,
                onCheckedChange = onToggleStorageIoBoost,
                accentColor = ElectricCyan,
                testTag = "toggle_storage_io_boost"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "Display Minimal Post-Processing & Wide Gamut Peak VSYNC Lock",
                subtitle = "Calls Window.setPreferMinimalPostProcessing(true), Wide Color Gamut, and locks peak hardware refresh rate (${state.deviceSpecs.displayRefreshRateHz}Hz)",
                checked = state.minimalPostProcessingDisplayEnabled,
                onCheckedChange = onToggleMinimalPostProcessingDisplay,
                accentColor = TelemetryGreen,
                testTag = "toggle_minimal_post_processing_display"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "5–10 Min 97%–100% Thermal & Benchmark Booster",
                subtitle = "Disables 90% OEM Sustained Cap and kills background apps via ActivityManager for steady 97%–100% sustained output",
                checked = state.antiThrottleBoosterEnabled,
                onCheckedChange = onToggleAntiThrottleBooster,
                accentColor = Color(0xFFFFB300),
                testTag = "toggle_anti_throttle_booster"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "Vivo Game Center Instant-Max Pulse (5x ADPF Overdrive)",
                subtitle = "Pulses 5x ADPF overdrive across Main UI PID + OpenGL GPU TID + Worker TIDs so Vivo Game Center shows max power immediately",
                checked = state.vivoGameCenterInstantPulseEnabled,
                onCheckedChange = onToggleVivoGameCenterPulse,
                accentColor = ElectricCyan,
                testTag = "toggle_vivo_game_center_pulse"
            )
        }
    }
}

@Composable
private fun BoosterToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(!checked)
            }
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianBg,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = ObsidianBg
            )
        )
    }
}

@Composable
private fun OriginOs6OverdriveDirectiveCard(
    state: PerformanceTelemetryState,
    onToggleOriginOs6Overdrive: (Boolean) -> Unit,
    onToggleTargetWindowHookState: (Boolean) -> Unit,
    onToggleAutoRestoreOnMinimize: (Boolean) -> Unit,
    onToggleRenderScaleSpoof: (Boolean) -> Unit,
    onToggleVSyncDisableSwapZero: (Boolean) -> Unit,
    onLaunchVolumeShaderPipeline: () -> Unit
) {
    val isOriginActive = state.originOs6OverdriveEnabled
    val isHookActive = state.isTargetWindowHookActive
    val borderBrush = Brush.linearGradient(
        colors = if (isOriginActive && isHookActive) {
            listOf(TelemetryGreen, ElectricCyan, Color(0xFFFFB300))
        } else {
            listOf(Color(0xFFFFB300), ElectricCyan.copy(alpha = 0.7f))
        }
    )
    val cpuDuty = if (state.isSessionActive) state.lockedPowerPercent.coerceIn(98, 100) else 100
    val gpuDuty = if (state.isSessionActive) state.gpuLockedDutyPercent.coerceIn(98, 100) else 100
    val gflops = if (state.isSessionActive) state.gpuFlopOverdriveGflops.coerceAtLeast(1420) else 1420

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.8.dp, borderBrush, RoundedCornerShape(18.dp))
            .testTag("originos6_overdrive_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF071612))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeveloperBoard,
                        contentDescription = "Dynamic Per-App Scale Overdrive",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DYNAMIC_PER_APP_SCALE_OVERDRIVE",
                            style = MaterialTheme.typography.titleMedium,
                            color = TelemetryGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OriginOS 6 (Vivo T4X) • Isolated Native Surface Scaler & Kernel Governor",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
                StatusPillBadge(
                    text = if (isHookActive && state.renderScaleSpoofEnabled) "0.5x APP_ONLY" else "1.0x NATIVE",
                    color = if (isHookActive && state.renderScaleSpoofEnabled) TelemetryGreen else Color(0xFFFFB300),
                    testTag = "originos6_fps_badge"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Output Status Terminal Banner
            Surface(
                color = ObsidianBg,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryGreen.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("originos6_output_status_badge")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = state.originOsOutputStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = TelemetryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.secondaryDirectiveStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "TARGET: ${state.targetPipelineProcess} • GLOBAL DPI: ${state.globalDisplayDpiLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB300)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            DiabloTelemetryRow(
                "1. TARGET_WINDOW_HOOK",
                if (isHookActive) {
                    "ACTIVE (App-Only • Global DPI Untouched)"
                } else {
                    "MINIMIZED / HOME (Global DPI Untouched)"
                }
            )
            DiabloTelemetryRow(
                "2. DYNAMIC_CANVAS_DOWNSCALE",
                state.internalShaderResolutionLabel
            )
            DiabloTelemetryRow(
                "3. AUTO_RESTORE_PROTOCOL",
                if (!isHookActive && state.autoRestoreOnMinimizeEnabled) {
                    "ENGAGED -> Restored 1.0x (1080p Native)"
                } else if (state.autoRestoreOnMinimizeEnabled) {
                    "ARMED (Instant 1.0x 1080p on Minimize/Home)"
                } else {
                    "Disabled"
                }
            )
            DiabloTelemetryRow(
                "4. PERF_GOVERNOR_LOCK",
                if (isHookActive) {
                    "LOCKED: GPU $gpuDuty% / CPU $cpuDuty% ($gflops GFLOP/s)"
                } else {
                    "STEPPED DOWN (Target Minimized)"
                }
            )
            DiabloTelemetryRow(
                "5. V_SYNC_BYPASS",
                if (state.vSyncDisabledEglSwapZero && isHookActive) {
                    "eglSwapInterval = 0 (Zero Frame Cap)"
                } else {
                    "eglSwapInterval = 1 (Native VSYNC)"
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedGlowButton(
                text = "LAUNCH TARGET (0.5x MANDELBULB 3D PIPELINE)",
                icon = Icons.Filled.PlayArrow,
                onClick = onLaunchVolumeShaderPipeline,
                enabled = true,
                baseColor = TelemetryGreen,
                pressedGlowColor = ElectricCyan,
                contentColor = ObsidianBg,
                testTag = "launch_volumeshader_pipeline_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            BoosterToggleRow(
                title = "1. TARGET_WINDOW_HOOK (Foreground Target Window Active)",
                subtitle = "ON = Target app in foreground (0.5x canvas scale + 100% CPU/GPU lock). OFF = Simulates Home Button / Minimize -> Immediately triggers 1.0x (1080p native) Auto-Restore. Global Display DPI is NEVER modified.",
                checked = state.isTargetWindowHookActive,
                onCheckedChange = onToggleTargetWindowHookState,
                accentColor = TelemetryGreen,
                testTag = "toggle_target_window_hook"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "2. DYNAMIC_CANVAS_DOWNSCALE (0.5x App-Only Viewport)",
                subtitle = "Forces internal WebGL/Vulkan/OpenGL viewport canvas to 0.5x resolution scale (${state.internalShaderResolutionLabel}) during active target session",
                checked = state.renderScaleSpoofEnabled,
                onCheckedChange = onToggleRenderScaleSpoof,
                accentColor = TelemetryGreen,
                testTag = "toggle_render_scale_spoof"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "3. AUTO_RESTORE_PROTOCOL (1.0x Native on Minimize / Home)",
                subtitle = "Immediately restores Viewport Canvas to 1.0x (1080p native) as soon as target app is minimized, closed, or Home Button is pressed",
                checked = state.autoRestoreOnMinimizeEnabled,
                onCheckedChange = onToggleAutoRestoreOnMinimize,
                accentColor = Color(0xFFFFB300),
                testTag = "toggle_auto_restore_on_minimize"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "5. V_SYNC_BYPASS (Force eglSwapInterval = 0 • Zero Stutter)",
                subtitle = "Forces EGL14.eglSwapInterval(eglDisplay, 0) for target surface to eliminate frame-rate capping and stuttering",
                checked = state.vSyncDisabledEglSwapZero,
                onCheckedChange = onToggleVSyncDisableSwapZero,
                accentColor = ElectricCyan,
                testTag = "toggle_vsync_disable_swap_zero"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            BoosterToggleRow(
                title = "OriginOS 6 (Vivo T4X) Isolated Surface Scaler & Kernel Governor",
                subtitle = "Master switch for TARGET_WINDOW_HOOK + 0.5x Dynamic Canvas + 1.0x Auto-Restore + 100% Governor Lock + V-Sync Bypass",
                checked = state.originOs6OverdriveEnabled,
                onCheckedChange = onToggleOriginOs6Overdrive,
                accentColor = TelemetryGreen,
                testTag = "toggle_originos6_overdrive"
            )
        }
    }
}

@Composable
private fun DiabloModeOverdriveHudCard(state: PerformanceTelemetryState) {
    val diabloBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFF1744), Color(0xFFFFB300))
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, diabloBrush, RoundedCornerShape(18.dp))
            .testTag("diablo_mode_hud_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A080D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Diablo Mode",
                        tint = Color(0xFFFF1744),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DIABLO MODE — FULL HARDWARE MAX LOCK",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "97%–100% Unclamped CPU/GPU/LPDDR/Audio/IMU/UFS • By Raunak Exploits",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
                StatusPillBadge(
                    text = if (state.isSessionActive) "IGNITED (${state.lockedPowerPercent.coerceIn(97, 100)}%)" else "ARMED",
                    color = if (state.isSessionActive) Color(0xFFFF1744) else Color(0xFFFFB300)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFFF1744).copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(10.dp))

            DiabloTelemetryRow("Prime-Core ADPF Target", "1.6 ms (5x Overdrive on UI + GL + CPU TIDs)")
            DiabloTelemetryRow(
                "CPU FPU + ARMv8 CRC32C Lock",
                if (state.noTouchPowerLockEnabled) {
                    val alu = if (state.isSessionActive) "${state.crc32AluOpsPerSecMillions}M ALU ops/s" else "Armed"
                    "LOCKED 97%–100% ($alu)"
                } else {
                    "Standard"
                }
            )
            DiabloTelemetryRow(
                "OpenGL ES 2.0 GPU Lock",
                if (state.noTouchPowerLockEnabled) {
                    "ACTIVE (${state.gpuLockedDutyPercent.coerceIn(97, 100)}% GLES20 • ${state.glRendererName.ifBlank { "Hardware GPU" }})"
                } else {
                    "Standby"
                }
            )
            DiabloTelemetryRow(
                "LPDDR5X Memory Bus Stride",
                if (state.memoryBandwidthPrefetchEnabled) {
                    if (state.isSessionActive) "ACTIVE (${state.memoryBandwidthMbPerSec} MB/s • 64B Cache Stride)" else "ARMED (256KB Direct)"
                } else {
                    "Paused"
                }
            )
            DiabloTelemetryRow(
                "Audio DSP Fast-Mixer Lock",
                if (state.lowLatencyAudioDspLockEnabled) {
                    "LOW LATENCY (${state.audioHardwareSampleRateHz}Hz / ${state.audioFastMixerBufferFrames} Frames)"
                } else {
                    "Paused"
                }
            )
            DiabloTelemetryRow(
                "High-Rate Game IMU & Touch",
                if (state.touchSensorBoostEnabled) {
                    val hz = if (state.isSessionActive) "${state.sensorSamplingHz}Hz" else "SENSOR_DELAY_GAME"
                    "ACTIVE ($hz • Unbuffered Input)"
                } else {
                    "Standard"
                }
            )
            DiabloTelemetryRow(
                "UFS 3.1 / 4.0 Direct Storage I/O",
                if (state.storageIoBoostEnabled) {
                    if (state.isSessionActive) "ACTIVE (${state.storageThroughputMbPerSec} MB/s • 16KB Direct)" else "ARMED (16KB Direct)"
                } else {
                    "Standard"
                }
            )
            DiabloTelemetryRow(
                "Display VSYNC & Network Link",
                "${state.liveMeasuredFps} FPS (${String.format(Locale.US, "%.1f", state.liveFrameTimeMs)}ms) • Net: ${state.networkDownstreamMbps} Mbps"
            )
            DiabloTelemetryRow("Non-Stop Background Guard", "ACTIVE (Until Notification Exit)")
        }
    }
}

@Composable
private fun DiabloTelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFFFB300),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IdentityHeaderCard(
    state: PerformanceTelemetryState,
    customLogoUri: String?
) {
    val headerBorderBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFB300), Color(0xFFE50914))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, headerBorderBrush, RoundedCornerShape(18.dp))
            .testTag("identity_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                VivoBrandingLogo(
                    customLogoUri = customLogoUri,
                    size = 60.dp,
                    isSessionActive = state.isSessionActive
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RE SPOOFING",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        StatusPillBadge(
                            text = "DIABLO MODE × VIVO",
                            color = Color(0xFFFFB300),
                            testTag = "vivo_iqoo_exclusive_badge"
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.main_purpose),
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "By Raunak Exploits • ${state.deviceSpecs.phoneDisplayName} (${state.deviceSpecs.androidVersionLabel})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFB300)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CarbonBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.secondary_description),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun VivoIqooHardwareGateBanner(
    state: PerformanceTelemetryState,
    onToggleVivoIqooSimulation: (Boolean) -> Unit
) {
    val compat = state.compatibility
    val isAllowed = compat.isVivoOrIqoo
    val borderColor = if (isAllowed) TelemetryGreen else TelemetryRed

    Surface(
        color = CarbonSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vivo_iqoo_hardware_gate_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isAllowed) Icons.Filled.VerifiedUser else Icons.Filled.Lock,
                        contentDescription = "Vivo & iQOO Hardware Gate",
                        tint = borderColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isAllowed) {
                                "VIVO / iQOO DIABLO MODE ENGINE VERIFIED"
                            } else {
                                "LOCKED: VIVO & iQOO PHONES ONLY"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = borderColor
                        )
                        Text(
                            text = if (isAllowed) {
                                "Active Environment: ${compat.vivoOsInfo}"
                            } else {
                                "RE Spoofing sirf Vivo aur iQOO smartphones mein work karta hai. Current OEM: ${compat.manufacturer} (${compat.deviceModel})"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                if (!compat.isRealVivoOrIqooHardware) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedGlowOutlinedButton(
                        text = if (compat.isVivoIqooSimulationActive) "Lock OEM" else "Unlock Vivo Mode",
                        onClick = { onToggleVivoIqooSimulation(!compat.isVivoIqooSimulationActive) },
                        accentColor = if (compat.isVivoIqooSimulationActive) TelemetryAmber else ElectricCyan,
                        testTag = "toggle_vivo_iqoo_mode_button"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PerformanceControlHeroCard(
    state: PerformanceTelemetryState,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onQuickProfileSelect: (PerformanceProfile) -> Unit
) {
    val isVivoIqooVerified = state.compatibility.isVivoOrIqoo
    val isOriginSelected = state.selectedProfile == PerformanceProfile.ORIGINOS_6_OVERDRIVE
    val isDiabloSelected = state.selectedProfile == PerformanceProfile.DIABLO_MODE || isOriginSelected
    val activeBorderColor by animateColorAsState(
        targetValue = when {
            state.isSessionActive && isOriginSelected -> TelemetryGreen
            state.isSessionActive && isDiabloSelected -> Color(0xFFFF1744)
            state.isSessionActive -> ElectricCyan
            !isVivoIqooVerified -> TelemetryRed.copy(alpha = 0.7f)
            isOriginSelected -> TelemetryGreen
            isDiabloSelected -> Color(0xFFFFB300)
            else -> CarbonBorder
        },
        animationSpec = tween(250),
        label = "hero_border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.8.dp, activeBorderColor, RoundedCornerShape(20.dp))
            .testTag("hero_control_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurfaceElevated)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBg.copy(alpha = 0.75f),
                                CarbonSurfaceElevated
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPillBadge(
                            text = when {
                                state.isSessionActive && isDiabloSelected -> "DIABLO MODE ACTIVE (NON-STOP)"
                                state.isSessionActive -> stringResource(id = R.string.status_active)
                                !isVivoIqooVerified -> stringResource(id = R.string.status_blocked_oem)
                                else -> stringResource(id = R.string.status_inactive)
                            },
                            color = when {
                                state.isSessionActive && isDiabloSelected -> Color(0xFFFF1744)
                                state.isSessionActive -> TelemetryGreen
                                !isVivoIqooVerified -> TelemetryRed
                                else -> TextSecondary
                            },
                            testTag = "performance_mode_status_badge"
                        )

                        if (state.isSessionActive) {
                            Text(
                                text = formatElapsedSeconds(state.sessionElapsedSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isDiabloSelected) Color(0xFFFFB300) else ElectricCyan,
                                modifier = Modifier.testTag("session_elapsed_timer")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val lockedPct = state.lockedPowerPercent.coerceIn(97, 100)
                    val gpuPct = state.gpuLockedDutyPercent.coerceIn(97, 100)
                    Text(
                        text = when {
                            state.isSessionActive && isOriginSelected -> "ORIGINOS 6 — 144 FPS ($lockedPct% / $gpuPct%)"
                            state.isSessionActive && isDiabloSelected -> "DIABLO — CPU $lockedPct% / GPU $gpuPct%"
                            state.isSessionActive -> "LOCKED — CPU $lockedPct% / GPU $gpuPct%"
                            isOriginSelected -> "ORIGINOS 6 • 144 FPS READY"
                            isDiabloSelected -> "DIABLO MODE READY"
                            else -> stringResource(id = R.string.main_purpose)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = when {
                            isOriginSelected -> TelemetryGreen
                            isDiabloSelected -> Color(0xFFFFB300)
                            else -> TextPrimary
                        },
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            state.isSessionActive ->
                                "97%–100% Constant CPU (FPU/CRC32C) + OpenGL ES 2.0 GPU + LPDDR5X + Audio DSP + IMU + UFS Max Lock • Runs non-stop until exited from Notification Panel"
                            !isVivoIqooVerified -> "Only Vivo and iQOO devices are permitted to run RE Spoofing Performance Mode."
                            else -> stringResource(id = R.string.status_subtitle_inactive)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            state.isSessionActive && isDiabloSelected -> Color(0xFFFF1744)
                            state.isSessionActive -> ElectricCyan
                            !isVivoIqooVerified -> TelemetryAmber
                            else -> TextSecondary
                        }
                    )

                    if (!state.activeGameName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusPillBadge(
                            text = "Target Game: ${state.activeGameName}",
                            color = CobaltTurbo
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "OPERATING MODE: ${state.selectedProfile.title} • FOCUS: ${state.selectedWorkloadFocus.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PerformanceProfile.entries.forEach { profile ->
                            AnimatedProfileChip(
                                profile = profile,
                                isSelected = state.selectedProfile == profile,
                                onClick = { onQuickProfileSelect(profile) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedGlowButton(
                            text = if (isDiabloSelected) "START DIABLO" else "START",
                            icon = when {
                                !isVivoIqooVerified -> Icons.Filled.Lock
                                isDiabloSelected -> Icons.Filled.LocalFireDepartment
                                else -> Icons.Filled.PlayArrow
                            },
                            onClick = onStartClicked,
                            enabled = !state.isSessionActive,
                            baseColor = when {
                                !isVivoIqooVerified -> TelemetryRed
                                isDiabloSelected -> Color(0xFFFF1744)
                                else -> Color(0xFFFFB300)
                            },
                            pressedGlowColor = if (isDiabloSelected) Color(0xFFFFB300) else ElectricCyan,
                            contentColor = if (!isVivoIqooVerified || isDiabloSelected) Color.White else ObsidianBg,
                            testTag = "start_button",
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )

                        AnimatedGlowOutlinedButton(
                            text = "STOP",
                            icon = Icons.Filled.Stop,
                            onClick = onStopClicked,
                            enabled = state.isSessionActive,
                            accentColor = TelemetryRed,
                            testTag = "stop_button",
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedProfileChip(
    profile: PerformanceProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val isDiabloChip = profile == PerformanceProfile.DIABLO_MODE

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    val chipBg by animateColorAsState(
        targetValue = when {
            isPressed -> ElectricCyan.copy(alpha = 0.28f)
            isSelected && isDiabloChip -> Color(0xFF4A0714)
            isSelected -> CobaltDeep
            else -> ObsidianBg.copy(alpha = 0.7f)
        },
        animationSpec = tween(150),
        label = "chip_bg"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            when {
                isPressed -> Color.White
                isSelected && isDiabloChip -> Color(0xFFFF1744)
                isSelected -> ElectricCyan
                isDiabloChip -> Color(0xFFFFB300).copy(alpha = 0.6f)
                else -> CarbonBorder
            }
        ),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag("quick_profile_${profile.id.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (isDiabloChip) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFFFB300) else Color(0xFFFF1744),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = profile.title,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isSelected && isDiabloChip -> Color(0xFFFFB300)
                    isSelected -> TextPrimary
                    isDiabloChip -> Color(0xFFFF1744)
                    else -> TextSecondary
                }
            )
        }
    }
}

@Composable
private fun ThermalAndBatteryProtectionBanner(state: PerformanceTelemetryState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            color = CarbonSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (state.thermalStatus.isThrottling) TelemetryRed else TelemetryGreen.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("thermal_protection_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "Thermal Protection Active",
                    tint = if (state.thermalStatus.isThrottling) TelemetryRed else TelemetryGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.thermal_protection_active),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (state.thermalStatus.isThrottling) TelemetryRed else TelemetryGreen
                    )
                    Text(
                        text = state.thermalStatus.warningBannerText
                            ?: "5–10 Min 97%–100% Anti-Throttle Booster active (OEM 90% Sustained Cap disabled; Prime/Gold ADPF + OpenGL ES 2.0 GPU boost locked).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.thermalStatus.isThrottling) TextPrimary else TextSecondary
                    )
                }
            }
        }

        AnimatedVisibility(visible = state.batteryStatus.isLowBattery) {
            Surface(
                color = TelemetryAmber.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryAmber),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("low_battery_warning_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Battery Warning",
                        tint = TelemetryAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(id = R.string.battery_low_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizationIndicatorRow(indicator: ActiveOptimizationIndicator) {
    val dotColor = when {
        indicator.id == "diablo_overdrive" && indicator.isActive -> Color(0xFFFF1744)
        indicator.isActive && !indicator.isFallbackOrLimited -> TelemetryGreen
        indicator.isActive && indicator.isFallbackOrLimited -> ElectricCyan
        else -> TextMuted
    }

    Surface(
        color = CarbonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("indicator_${indicator.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    indicator.id == "diablo_overdrive" -> Icons.Filled.LocalFireDepartment
                    indicator.isActive -> Icons.Filled.CheckCircle
                    else -> Icons.Filled.Info
                },
                contentDescription = indicator.title,
                tint = dotColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = indicator.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = indicator.stateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = dotColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = indicator.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EngineEventLogCard(logs: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .testTag("engine_event_log_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = "Session Telemetry Log",
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RE SPOOFING TELEMETRY LOG • RAUNAK EXPLOITS",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            logs.take(6).forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

private fun formatElapsedSeconds(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
}
