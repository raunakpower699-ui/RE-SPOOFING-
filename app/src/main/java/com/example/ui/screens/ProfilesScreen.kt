package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionHistoryEntity
import com.example.model.EngineWorkloadMode
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.WorkloadFocus
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfilesScreen(
    state: PerformanceTelemetryState,
    recentSessions: List<SessionHistoryEntity>,
    onSelectWorkloadFocus: (WorkloadFocus) -> Unit,
    onSelectCpuPowerTarget: (Int) -> Unit,
    onSelectGpuPowerTarget: (Int) -> Unit,
    onSelectEngineWorkloadMode: (EngineWorkloadMode) -> Unit,
    onStartPerformanceMode: () -> Unit,
    onStopPerformanceMode: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val basePct = when (state.selectedProfile) {
        PerformanceProfile.DIABLO_MODE -> 100
        PerformanceProfile.BALANCE_MODE -> 70
        PerformanceProfile.SAVING_MODE -> 25
    }
    val effectiveCpuTarget = state.customCpuDutyOverride
        ?: if (state.selectedWorkloadFocus.enableCpuHints) basePct else 0
    val effectiveGpuTarget = state.customGpuDutyOverride
        ?: if (state.selectedWorkloadFocus.enableGpuGameHints) basePct else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("profiles_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LiveControlHeaderCard(
                state = state,
                effectiveCpuTarget = effectiveCpuTarget,
                effectiveGpuTarget = effectiveGpuTarget,
                onStartPerformanceMode = onStartPerformanceMode,
                onStopPerformanceMode = onStopPerformanceMode
            )
        }

        item {
            DirectCpuGpuControlCard(
                cpuTargetPercent = effectiveCpuTarget,
                gpuTargetPercent = effectiveGpuTarget,
                isSessionActive = state.isSessionActive,
                onSelectCpuPowerTarget = onSelectCpuPowerTarget,
                onSelectGpuPowerTarget = onSelectGpuPowerTarget
            )
        }

        item {
            QuickFocusStrip(
                selectedFocus = state.selectedWorkloadFocus,
                hasCustomOverride = state.customCpuDutyOverride != null || state.customGpuDutyOverride != null,
                onSelectWorkloadFocus = onSelectWorkloadFocus
            )
        }

        item {
            EngineLockModeCard(
                selectedMode = state.engineWorkloadMode,
                onSelectEngineWorkloadMode = onSelectEngineWorkloadMode
            )
        }

        if (recentSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "History",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECENT SESSIONS (${recentSessions.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = ElectricCyan
                        )
                    }
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear History",
                            tint = TextSecondary
                        )
                    }
                }
            }

            items(recentSessions.take(8), key = { it.id }) { session ->
                SessionHistoryCard(session = session)
            }
        }
    }
}

@Composable
private fun LiveControlHeaderCard(
    state: PerformanceTelemetryState,
    effectiveCpuTarget: Int,
    effectiveGpuTarget: Int,
    onStartPerformanceMode: () -> Unit,
    onStopPerformanceMode: () -> Unit
) {
    val borderColor = if (state.isSessionActive) DiabloRed else ElectricCyan.copy(alpha = 0.6f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (state.isSessionActive) CobaltDeep else CarbonSurface,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (state.isSessionActive) "CPU & GPU ACTIVE" else "CPU & GPU STANDBY",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.isSessionActive) DiabloRed else ElectricCyan,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                val cpuShown = if (state.isSessionActive) state.realMeasuredThreadDutyPercent else effectiveCpuTarget
                val gpuShown = if (state.isSessionActive) state.gpuLockedDutyPercent else effectiveGpuTarget
                val hzShown = if (state.isSessionActive) state.lockedRefreshRateHz else state.selectedRefreshRate.hz
                Text(
                    text = "CPU $cpuShown% • GPU $gpuShown% • ${hzShown}Hz",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { if (state.isSessionActive) onStopPerformanceMode() else onStartPerformanceMode() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isSessionActive) DiabloRed else ElectricCyan,
                    contentColor = if (state.isSessionActive) TextPrimary else ObsidianBg
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("modes_start_stop_button")
            ) {
                Icon(
                    imageVector = if (state.isSessionActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isSessionActive) "Stop Engine" else "Start Engine",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (state.isSessionActive) "STOP" else "START",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun DirectCpuGpuControlCard(
    cpuTargetPercent: Int,
    gpuTargetPercent: Int,
    isSessionActive: Boolean,
    onSelectCpuPowerTarget: (Int) -> Unit,
    onSelectGpuPowerTarget: (Int) -> Unit
) {
    val dutySteps = listOf(0, 50, 75, 100)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("direct_cpu_gpu_control_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Memory,
                        contentDescription = "CPU Power",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CPU POWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "$cpuTargetPercent% • ${if (isSessionActive) "LIVE" else "READY"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dutySteps.forEach { pct ->
                    PowerStepPill(
                        label = if (pct == 0) "OFF" else "$pct%",
                        isSelected = isStepSelected(cpuTargetPercent, pct),
                        accentColor = ElectricCyan,
                        tag = "cpu_power_$pct",
                        onClick = { onSelectCpuPowerTarget(pct) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "GPU Power",
                        tint = DiabloRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPU POWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "$gpuTargetPercent% • ${if (isSessionActive) "LIVE" else "READY"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DiabloRed,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dutySteps.forEach { pct ->
                    PowerStepPill(
                        label = if (pct == 0) "OFF" else "$pct%",
                        isSelected = isStepSelected(gpuTargetPercent, pct),
                        accentColor = DiabloRed,
                        tag = "gpu_power_$pct",
                        onClick = { onSelectGpuPowerTarget(pct) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFocusStrip(
    selectedFocus: WorkloadFocus,
    hasCustomOverride: Boolean,
    onSelectWorkloadFocus: (WorkloadFocus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkloadFocus.entries.forEach { focus ->
            val isSelected = selectedFocus == focus && !hasCustomOverride
            val shortLabel = when (focus) {
                WorkloadFocus.COMBINED_MAX -> "Both Max"
                WorkloadFocus.CPU_FOCUS -> "CPU Only"
                WorkloadFocus.GPU_FOCUS -> "GPU Only"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) CobaltDeep else CarbonSurface,
                border = BorderStroke(1.4.dp, if (isSelected) CobaltTurbo else CarbonBorder),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectWorkloadFocus(focus) }
                    .testTag("focus_card_${focus.id}")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 11.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = shortLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) ElectricCyan else TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun isStepSelected(actualPercent: Int, stepPercent: Int): Boolean {
    return when (stepPercent) {
        0 -> actualPercent == 0
        50 -> actualPercent in 1..60
        75 -> actualPercent in 61..89
        100 -> actualPercent >= 90
        else -> actualPercent == stepPercent
    }
}

@Composable
private fun PowerStepPill(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CobaltDeep else ObsidianBg,
        border = BorderStroke(1.4.dp, if (isSelected) accentColor else CarbonBorder),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) accentColor else TextSecondary,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EngineLockModeCard(
    selectedMode: EngineWorkloadMode,
    onSelectEngineWorkloadMode: (EngineWorkloadMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("engine_lock_mode_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "GAME BOOST BEHAVIOR",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedMode.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(EngineWorkloadMode.BENCHMARK_STRESS, EngineWorkloadMode.GAME_ANTI_DROP)
                modes.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val accent = if (mode == EngineWorkloadMode.BENCHMARK_STRESS) DiabloRed else TelemetryGreen
                    val shortTitle = if (mode == EngineWorkloadMode.BENCHMARK_STRESS) "Max + Auto Game" else "Zero-Load Hz"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CobaltDeep else ObsidianBg,
                        border = BorderStroke(1.4.dp, if (isSelected) accent else CarbonBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectEngineWorkloadMode(mode) }
                            .testTag("engine_mode_${mode.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = shortTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) accent else TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: SessionHistoryEntity) {
    Surface(
        color = CarbonSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.associatedGameName ?: "Hardware Lock Session",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.profileId} • Peak ${session.peakBatteryTempCelsius}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "${session.durationSeconds}s",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
