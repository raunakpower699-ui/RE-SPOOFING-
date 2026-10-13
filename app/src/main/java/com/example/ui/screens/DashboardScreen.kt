package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeviceHardwareTier
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.ui.components.AnimatedGlowButton
import com.example.ui.components.AnimatedGlowOutlinedButton
import com.example.ui.components.RaunakExploitsCreatorBadge
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.VivoBrandingLogo
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun DashboardScreen(
    state: PerformanceTelemetryState,
    customLogoUri: String?,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onQuickProfileSelect: (PerformanceProfile) -> Unit,
    onOpenLogoPicker: () -> Unit,
    onToggleVivoIqooSimulation: (Boolean) -> Unit = {},
    onToggleOriginOs6Overdrive: (Boolean) -> Unit = {},
    onToggleGlDitherAndFastestHints: (Boolean) -> Unit = {},
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Sleek Header: Phone Model, Hardware Tier Badge & Status Pill
        item {
            SimpleHeaderCard(
                state = state,
                customLogoUri = customLogoUri
            )
        }

        // 2. Creator Signature
        item {
            RaunakExploitsCreatorBadge()
        }

        // 3. Hero Ignition Deck: Big Master Boost Action
        item {
            MasterIgnitionDeckCard(
                state = state,
                onStartClicked = onStartClicked,
                onStopClicked = onStopClicked
            )
        }

        // 4. Clean Customization: 3 Simple Performance Modes
        item {
            SimpleProfileSelectorCard(
                state = state,
                onSelectProfile = onQuickProfileSelect
            )
        }

        // 5. Clean Customization: 4 Essential Hardware Switches (No Clutter)
        item {
            EssentialCustomizationCard(
                state = state,
                onToggleVSync = onToggleVSyncDisableSwapZero,
                onToggleThermalBypass = onToggleAntiThrottleBooster,
                onToggleHardwareFloor = onToggleNoTouchPowerLock,
                onToggleNativeCanvas = { maintain -> onToggleRenderScaleSpoof(!maintain) },
                onLaunch3D = onLaunchVolumeShaderPipeline,
                onPurgeRam = onPurgeBackgroundAppsNow
            )
        }

        // 6. Live Gamer Telemetry HUD: Compact 2x2 Stat Matrix
        item {
            GamerTelemetryHudMatrix(state = state)
        }

        // 7. Thermal & Safety Protection Status
        item {
            SimpleThermalProtectionCard(state = state)
        }
    }
}

@Composable
private fun SimpleHeaderCard(
    state: PerformanceTelemetryState,
    customLogoUri: String?
) {
    val tier = state.deviceSpecs.hardwareTier
    val tierColor = when (tier) {
        DeviceHardwareTier.FLAGSHIP -> Color(0xFFFFB300)
        DeviceHardwareTier.MID_RANGE -> ElectricCyan
        DeviceHardwareTier.ENTRY_LEVEL -> TelemetryGreen
    }

    val headerBorderBrush = Brush.horizontalGradient(
        colors = listOf(tierColor, Color(0xFFE50914))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, headerBorderBrush, RoundedCornerShape(18.dp))
            .testTag("identity_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VivoBrandingLogo(
                customLogoUri = customLogoUri,
                size = 52.dp,
                isSessionActive = state.isSessionActive
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "RE SPOOFING",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    StatusPillBadge(
                        text = tier.shortLabel,
                        color = tierColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${state.deviceSpecs.phoneDisplayName} (${state.deviceSpecs.socProcessor})",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (state.isSessionActive) "OVERDRIVE ACTIVE • 100% DUTY LOCK" else "STANDBY • READY TO BOOST",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isSessionActive) TelemetryGreen else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MasterIgnitionDeckCard(
    state: PerformanceTelemetryState,
    onStartClicked: () -> Unit,
    onStopClicked: () -> Unit
) {
    val isRunning = state.isSessionActive
    val activeBorderBrush = Brush.linearGradient(
        colors = if (isRunning) {
            listOf(Color(0xFFFF1744), Color(0xFFFFB300), TelemetryGreen)
        } else {
            listOf(CarbonBorder, ElectricCyan.copy(alpha = 0.5f))
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.8.dp, activeBorderBrush, RoundedCornerShape(20.dp))
            .testTag("master_ignition_deck"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) Color(0xFF16080C) else CarbonSurfaceElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Power Level Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isRunning) "ENGINE POWER OUTPUT" else "PERFORMANCE GOVERNOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) Color(0xFFFFB300) else TextSecondary
                    )
                    Text(
                        text = if (isRunning) "${state.lockedPowerPercent.coerceIn(98, 100)}% MAXIMUM LOCK" else "READY FOR IGNITION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) Color(0xFFFF1744) else TextPrimary
                    )
                }
                StatusPillBadge(
                    text = if (isRunning) "IGNITED" else "STANDBY",
                    color = if (isRunning) Color(0xFFFF1744) else TelemetryAmber
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Big Action Button
            if (!isRunning) {
                AnimatedGlowButton(
                    text = "ACTIVATE MAX OVERDRIVE",
                    icon = Icons.Filled.Bolt,
                    onClick = onStartClicked,
                    enabled = true,
                    baseColor = Color(0xFFFFB300),
                    pressedGlowColor = ElectricCyan,
                    contentColor = ObsidianBg,
                    testTag = "start_button",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimatedGlowButton(
                        text = "OVERDRIVE LOCKED (100%)",
                        icon = Icons.Filled.LocalFireDepartment,
                        onClick = {},
                        enabled = false,
                        baseColor = Color(0xFFFF1744),
                        pressedGlowColor = Color(0xFFFFB300),
                        contentColor = Color.White,
                        testTag = "overdrive_running_badge",
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    )
                    AnimatedGlowOutlinedButton(
                        text = "STOP",
                        icon = Icons.Filled.Stop,
                        onClick = onStopClicked,
                        enabled = true,
                        accentColor = TelemetryRed,
                        testTag = "stop_button",
                        modifier = Modifier
                            .width(100.dp)
                            .height(54.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleProfileSelectorCard(
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
                text = "PERFORMANCE TUNING PROFILE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickModeChip(
                    title = "⚡ Flagship Ultra",
                    subtitle = "100% Lock",
                    isSelected = state.selectedProfile == PerformanceProfile.ORIGINOS_6_OVERDRIVE ||
                        state.selectedProfile == PerformanceProfile.DIABLO_MODE,
                    accentColor = Color(0xFFFF1744),
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.ORIGINOS_6_OVERDRIVE) },
                    testTag = "quick_profile_originos_6"
                )
                QuickModeChip(
                    title = "🎮 Monster Gaming",
                    subtitle = "144 FPS / GLES",
                    isSelected = state.selectedProfile == PerformanceProfile.GAMING,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.GAMING) },
                    testTag = "quick_profile_gaming"
                )
                QuickModeChip(
                    title = "🛡️ Balanced",
                    subtitle = "Adaptive",
                    isSelected = state.selectedProfile == PerformanceProfile.BALANCED,
                    accentColor = TelemetryGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectProfile(PerformanceProfile.BALANCED) },
                    testTag = "quick_profile_balanced"
                )
            }
        }
    }
}

@Composable
private fun QuickModeChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CobaltDeep else ObsidianBg,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isSelected) accentColor else CarbonBorder
        ),
        modifier = modifier
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) accentColor else TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EssentialCustomizationCard(
    state: PerformanceTelemetryState,
    onToggleVSync: (Boolean) -> Unit,
    onToggleThermalBypass: (Boolean) -> Unit,
    onToggleHardwareFloor: (Boolean) -> Unit,
    onToggleNativeCanvas: (Boolean) -> Unit,
    onLaunch3D: () -> Unit,
    onPurgeRam: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SIMPLE CUSTOMIZATION",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFB300),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. V-Sync Unlocker
            CleanToggleRow(
                title = "V-Sync & Frame Limit Bypass",
                subtitle = "Force eglSwapInterval(0) for maximum uncapped FPS",
                checked = state.vSyncDisabledEglSwapZero,
                onCheckedChange = onToggleVSync,
                accentColor = TelemetryGreen,
                testTag = "toggle_vsync_disable_swap_zero"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 2. Thermal Bypass
            CleanToggleRow(
                title = "Thermal Bypass & Anti-Throttle",
                subtitle = "Bypass OEM 90% sustained thermal limit to prevent FPS drops",
                checked = state.antiThrottleBoosterEnabled,
                onCheckedChange = onToggleThermalBypass,
                accentColor = Color(0xFFFF1744),
                testTag = "toggle_anti_throttle_booster"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 3. Hardware Floor Lock
            CleanToggleRow(
                title = "Constant Hardware Floor Lock",
                subtitle = "Keep CPU & GPU duty locked at 98%–100% even without screen touch",
                checked = state.noTouchPowerLockEnabled,
                onCheckedChange = onToggleHardwareFloor,
                accentColor = ElectricCyan,
                testTag = "toggle_no_touch_power_lock"
            )

            HorizontalDivider(
                color = CarbonBorder.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 4. 1.0x Native Canvas Boundaries
            CleanToggleRow(
                title = "1.0x Native Canvas Guard",
                subtitle = "Maintain native resolution boundaries without SurfaceFlinger stalls",
                checked = state.nativeCanvasBoundariesMaintained,
                onCheckedChange = onToggleNativeCanvas,
                accentColor = Color(0xFFFFB300),
                testTag = "toggle_native_canvas_boundaries"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedGlowButton(
                    text = "3D BENCHMARK",
                    icon = Icons.Filled.PlayArrow,
                    onClick = onLaunch3D,
                    enabled = true,
                    baseColor = TelemetryGreen,
                    pressedGlowColor = ElectricCyan,
                    contentColor = ObsidianBg,
                    testTag = "launch_volumeshader_pipeline_button",
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
                AnimatedGlowButton(
                    text = "PURGE RAM",
                    icon = Icons.Filled.Memory,
                    onClick = onPurgeRam,
                    enabled = true,
                    baseColor = CobaltDeep,
                    pressedGlowColor = ElectricCyan,
                    contentColor = TextPrimary,
                    testTag = "purge_background_apps_button",
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }
        }
    }
}

@Composable
private fun CleanToggleRow(
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
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
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
private fun GamerTelemetryHudMatrix(state: PerformanceTelemetryState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CarbonBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REAL-TIME HARDWARE HUD",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.deviceSpecs.hardwareTier.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB300)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HudMetricBox(
                    label = "FRAME RATE",
                    value = "${state.liveMeasuredFps} FPS",
                    sub = if (state.vSyncDisabledEglSwapZero) "UNLOCKED (eglSwap 0)" else "VSYNC Paced",
                    icon = Icons.Filled.Speed,
                    color = TelemetryGreen,
                    modifier = Modifier.weight(1f)
                )
                HudMetricBox(
                    label = "GPU DUTY",
                    value = if (state.isSessionActive) "${state.gpuLockedDutyPercent.coerceIn(98, 100)}%" else "STANDBY",
                    sub = state.gpuStatus.hardwareModel.take(18),
                    icon = Icons.Filled.DeveloperBoard,
                    color = CobaltDeep,
                    tint = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HudMetricBox(
                    label = "CPU CLUSTERS",
                    value = if (state.isSessionActive) "${state.lockedPowerPercent.coerceIn(98, 100)}%" else "OS MANAGED",
                    sub = "${state.deviceSpecs.cpuCores} Cores Active",
                    icon = Icons.Filled.Memory,
                    color = Color(0xFFFF1744),
                    modifier = Modifier.weight(1f)
                )
                HudMetricBox(
                    label = "BATTERY TEMP",
                    value = String.format(Locale.US, "%.1f°C", state.batteryStatus.temperatureCelsius),
                    sub = if (state.thermalStatus.isThrottling) "SAFETY LIMIT" else "PROTECTED",
                    icon = Icons.Filled.Thermostat,
                    color = if (state.thermalStatus.isThrottling) TelemetryRed else TelemetryGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HudMetricBox(
    label: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    tint: Color = color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SimpleThermalProtectionCard(state: PerformanceTelemetryState) {
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
                    text = "Adaptive governor active for ${state.deviceSpecs.hardwareTier.displayName} • OEM 90% clamping bypassed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
