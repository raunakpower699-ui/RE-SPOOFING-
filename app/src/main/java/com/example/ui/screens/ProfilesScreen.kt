package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
    onSelectProfile: (PerformanceProfile) -> Unit,
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
            EngineLockModeCard(
                selectedMode = state.engineWorkloadMode,
                gameHeadroomPercent = state.gameCpuHeadroomPercent,
                onSelectEngineWorkloadMode = onSelectEngineWorkloadMode
            )
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INTELLIGENT WORKLOAD FOCUS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CobaltDeep,
                        border = BorderStroke(1.dp, CobaltTurbo)
                    ) {
                        Text(
                            text = if (state.isSessionActive) {
                                "CPU ${state.realMeasuredThreadDutyPercent}% • GPU ${state.gpuLockedDutyPercent}%"
                            } else {
                                "TARGET ${effectiveCpuTarget}%/${effectiveGpuTarget}%"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quick presets: focus CPU Multi-Core only, GPU only, or Combined Max.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(WorkloadFocus.entries, key = { it.id }) { focus ->
            val isSelected = state.selectedWorkloadFocus == focus &&
                state.customCpuDutyOverride == null &&
                state.customGpuDutyOverride == null
            WorkloadFocusCard(
                focus = focus,
                isSelected = isSelected,
                onSelect = { onSelectWorkloadFocus(focus) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Column {
                Text(
                    text = "PERFORMANCE TUNING MODES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Base clock intensity tier for CPU/GPU governors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(PerformanceProfile.entries, key = { it.id }) { profile ->
            val isSelected = state.selectedProfile == profile
            ProfileOptionCard(
                profile = profile,
                isSelected = isSelected,
                onSelect = { onSelectProfile(profile) }
            )
        }

        if (recentSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "History",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

            items(recentSessions, key = { it.id }) { session ->
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
                    text = if (state.isSessionActive) "ENGINE LOCKED & ACTIVE" else "READY TO ENGAGE",
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
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Direct CPU GPU Control",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DIRECT CPU / GPU POWER CONTROL",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = if (isSessionActive) "LIVE" else "ARMED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSessionActive) TelemetryGreen else ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set independent CPU & GPU compute power. Keep both at 100% for full max lock, or 0% to isolate one side.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Memory,
                        contentDescription = "CPU Power",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CPU POWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$cpuTargetPercent%",
                    style = MaterialTheme.typography.labelLarge,
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
                    val isSelected = isStepSelected(cpuTargetPercent, pct)
                    PowerStepPill(
                        label = if (pct == 0) "0% OFF" else "$pct%",
                        isSelected = isSelected,
                        accentColor = ElectricCyan,
                        tag = "cpu_power_$pct",
                        onClick = { onSelectCpuPowerTarget(pct) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "GPU Power",
                        tint = DiabloRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPU POWER",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$gpuTargetPercent%",
                    style = MaterialTheme.typography.labelLarge,
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
                    val isSelected = isStepSelected(gpuTargetPercent, pct)
                    PowerStepPill(
                        label = if (pct == 0) "0% OFF" else "$pct%",
                        isSelected = isSelected,
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EngineLockModeCard(
    selectedMode: EngineWorkloadMode,
    gameHeadroomPercent: Int,
    onSelectEngineWorkloadMode: (EngineWorkloadMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = BorderStroke(1.2.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("engine_lock_mode_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.SportsEsports,
                        contentDescription = "Engine Lock Mode",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENGINE LOCK BEHAVIOR",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Headroom $gameHeadroomPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TelemetryGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Anti-Drop keeps 144/165Hz & CPU/GPU governors awake with 98% free game headroom. Unclamped Max drives full foreground hardware load.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EngineWorkloadMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val accent = if (mode == EngineWorkloadMode.GAME_ANTI_DROP) TelemetryGreen else DiabloRed
                    val shortTitle = if (mode == EngineWorkloadMode.GAME_ANTI_DROP) "Game Anti-Drop" else "Unclamped Max"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CobaltDeep else ObsidianBg,
                        border = BorderStroke(1.4.dp, if (isSelected) accent else CarbonBorder),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectEngineWorkloadMode(mode) }
                            .testTag("engine_mode_${mode.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
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
private fun ProfileOptionCard(
    profile: PerformanceProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val accentColor = when (profile) {
        PerformanceProfile.DIABLO_MODE -> DiabloRed
        PerformanceProfile.BALANCE_MODE -> ElectricCyan
        PerformanceProfile.SAVING_MODE -> TelemetryGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                1.5.dp,
                if (isSelected) accentColor else CarbonBorder,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CobaltDeep else CarbonSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = accentColor,
                    unselectedColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun WorkloadFocusCard(
    focus: WorkloadFocus,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("focus_card_${focus.id}")
            .border(
                1.5.dp,
                if (isSelected) CobaltTurbo else CarbonBorder,
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CobaltDeep else CarbonSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = CobaltTurbo,
                    unselectedColor = TextSecondary
                )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = focus.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = focus.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = CobaltTurbo
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = focus.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
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
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.associatedGameName ?: "Hardware Lock Session",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.durationSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profile: ${session.profileId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Peak: ${session.peakBatteryTempCelsius}°C",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (session.peakBatteryTempCelsius > 44f) DiabloRed else TelemetryGreen
                )
            }
        }
    }
}
