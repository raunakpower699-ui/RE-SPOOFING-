package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
                customLogoUri = customLogoUri,
                onOpenLogoPicker = onOpenLogoPicker
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

        // 4. Hero Performance Mode Control Deck (START / STOP + Status + Quick Diablo Mode)
        item {
            PerformanceControlHeroCard(
                state = state,
                onStartClicked = onStartClicked,
                onStopClicked = onStopClicked,
                onQuickProfileSelect = onQuickProfileSelect
            )
        }

        // 5. ROG-Style DIABLO MODE Overdrive HUD Card (shown when DIABLO_MODE is selected or active)
        item {
            AnimatedVisibility(visible = state.selectedProfile == PerformanceProfile.DIABLO_MODE) {
                DiabloModeOverdriveHudCard(state = state)
            }
        }

        // 6. Live Phone Information Card (Phone Name, Android Version, SoC, RAM, Storage, Display)
        item {
            DeviceLiveSpecsCard(
                specs = state.deviceSpecs,
                battery = state.batteryStatus
            )
        }

        // 7. Thermal Protection & Battery Warning Banners
        item {
            ThermalAndBatteryProtectionBanner(state = state)
        }

        // 8. Four Primary Status Cards (CPU, GPU, Thermal, Foreground Service & Battery)
        item {
            Text(
                text = "REAL-TIME VIVO / iQOO HARDWARE & API TELEMETRY",
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
                title = "CPU — Performance Request",
                subtitleLabel = "PerformanceHintManager & Scheduler",
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
                title = "GPU — Performance Request",
                subtitleLabel = "Vivo / iQOO Game Mode & Graphics APIs",
                stateBadge = "Performance request: ${state.gpuStatus.requestState}",
                stateColor = gpuColor,
                icon = Icons.Filled.DeveloperBoard,
                primaryMetric = "SoC / Graphics: ${state.gpuStatus.hardwareModel} • Mode: ${state.compatibility.currentGameModeLabel}",
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
                title = "Thermal — Protected",
                subtitleLabel = stringResource(id = R.string.thermal_protection_active),
                stateBadge = "Thermal: ${state.thermalStatus.level.displayLabel}",
                stateColor = thermalColor,
                icon = Icons.Filled.Thermostat,
                primaryMetric = String.format(
                    Locale.US,
                    "Battery Temp: %.1f°C • Headroom: %s • %s",
                    state.thermalStatus.batteryTempCelsius,
                    headroomStr,
                    state.thermalStatus.rawAndroidThermalName
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
                "Service — Running"
            } else {
                "Service — Inactive"
            }

            TelemetryStatusCard(
                title = "Service & Battery Status",
                subtitleLabel = "Foreground Utility & Power Telemetry",
                stateBadge = serviceBadge,
                stateColor = serviceColor,
                icon = if (state.batteryStatus.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                primaryMetric = "Battery: ${state.batteryStatus.levelPercent}% (${state.batteryStatus.powerSourceLabel}) • ${state.batteryStatus.voltageMv} mV",
                secondaryDetail = if (state.foregroundServiceRunning && state.isSessionActive) {
                    "Persistent notification active (RE Spoofing — Performance Mode Active). Battery Saver: ${if (state.batteryStatus.isSystemBatterySaverOn) "ON" else "OFF"}"
                } else {
                    "Foreground service stopped. Zero background CPU/GPU consumption. Battery Saver: ${if (state.batteryStatus.isSystemBatterySaverOn) "ON" else "OFF"}"
                },
                testTag = "service_battery_status_card"
            )
        }

        // 9. Active Optimization Indicators
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

        // 10. Live Engine Event Log
        item {
            EngineEventLogCard(logs = state.eventLogs)
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
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
                            text = "DIABLO MODE — ROG EXTREME ENGINE",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hardcore Competitive Gaming • Tuned by Raunak Exploits",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
                StatusPillBadge(
                    text = if (state.isSessionActive) "IGNITED" else "ARMED",
                    color = if (state.isSessionActive) Color(0xFFFF1744) else Color(0xFFFFB300)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFFF1744).copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(10.dp))

            DiabloTelemetryRow("Frame Pacing Target", "6.0 ms (165Hz-Class Hint Session)")
            DiabloTelemetryRow("Android GameState", "MODE_GAMEPLAY_UNINTERRUPTIBLE")
            DiabloTelemetryRow("CPU Power Cap", "DISABLED (PreferPowerEfficiency = false)")
            DiabloTelemetryRow("Window Sustained + Peak Hz", if (state.isSessionActive) "ACTIVE (Peak Refresh Requested)" else "Standby until START")
            DiabloTelemetryRow("OS Thermal Safeguard", "ACTIVE (${state.thermalStatus.level.displayLabel})")
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
    customLogoUri: String?,
    onOpenLogoPicker: () -> Unit
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
                    size = 66.dp,
                    isSessionActive = state.isSessionActive,
                    modifier = Modifier.clickable { onOpenLogoPicker() }
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
                            text = "VIVO / iQOO ONLY",
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
                                "VIVO / iQOO DEVICE VERIFIED"
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
    val isDiabloSelected = state.selectedProfile == PerformanceProfile.DIABLO_MODE
    val activeBorderColor by animateColorAsState(
        targetValue = when {
            state.isSessionActive && isDiabloSelected -> Color(0xFFFF1744)
            state.isSessionActive -> ElectricCyan
            !isVivoIqooVerified -> TelemetryRed.copy(alpha = 0.7f)
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
            Image(
                painter = painterResource(id = R.drawable.img_telemetry_banner),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(275.dp),
                contentScale = ContentScale.Crop,
                alpha = if (state.isSessionActive) 0.28f else 0.14f
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBg.copy(alpha = 0.55f),
                                CarbonSurfaceElevated.copy(alpha = 0.94f),
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
                                state.isSessionActive && isDiabloSelected -> "DIABLO MODE ACTIVE"
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

                    Text(
                        text = when {
                            state.isSessionActive && isDiabloSelected -> "DIABLO MODE — IGNITED"
                            state.isSessionActive -> "RE SPOOFING — ACTIVE"
                            isDiabloSelected -> "DIABLO MODE READY"
                            else -> stringResource(id = R.string.main_purpose)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (isDiabloSelected) Color(0xFFFFB300) else TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when {
                            state.isSessionActive && isDiabloSelected ->
                                "ROG-Style Extreme Uninterruptible Overdrive (6.0ms / Peak Refresh Requested)"
                            state.isSessionActive -> stringResource(id = R.string.status_subtitle_active)
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
                            ?: "Android & Vivo/iQOO thermal safeguards remain strictly enabled. OS retains final authority over clocks and hardware safety.",
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
