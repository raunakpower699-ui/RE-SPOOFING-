package com.example.ui.screens

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ThermostatAuto
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SessionHistoryEntity
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.WorkloadFocus
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryAmber
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilesScreen(
    state: PerformanceTelemetryState,
    recentSessions: List<SessionHistoryEntity>,
    onSelectProfile: (PerformanceProfile) -> Unit,
    onSelectWorkloadFocus: (WorkloadFocus) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("profiles_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PERFORMANCE & DIABLO PROFILES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select how RE Spoofing requests Android PerformanceHintManager, Game Mode, Sustained Performance, or ROG-style Diablo Mode Overdrive.",
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

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = "INTELLIGENT CPU / GPU WORKLOAD FOCUS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prioritize only the subsystems required by your workload. Avoids forcing unnecessary GPU activity for CPU tasks or unnecessary CPU hints for rendering tasks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        items(WorkloadFocus.entries, key = { it.id }) { focus ->
            val isSelected = state.selectedWorkloadFocus == focus
            WorkloadFocusCard(
                focus = focus,
                isSelected = isSelected,
                onSelect = { onSelectWorkloadFocus(focus) }
            )
        }

        // Session History Section
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
                        contentDescription = "Session History",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECENT PERFORMANCE SESSIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
                    )
                }
                if (recentSessions.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Clear Session History",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        if (recentSessions.isEmpty()) {
            item {
                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No completed sessions recorded yet. Press START on the Dashboard to initiate a performance optimization session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentSessions, key = { it.id }) { session ->
                SessionHistoryRow(session = session)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfileOptionCard(
    profile: PerformanceProfile,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val isDiablo = profile == PerformanceProfile.DIABLO_MODE
    val borderColor = when {
        isSelected && isDiablo -> Color(0xFFFF1744)
        isSelected -> ElectricCyan
        isDiablo -> Color(0xFFFF1744).copy(alpha = 0.55f)
        else -> CarbonBorder
    }
    val containerColor = when {
        isSelected && isDiablo -> Color(0xFF1F080E)
        isSelected -> CarbonSurfaceElevated
        else -> CarbonSurface
    }
    val icon = when (profile) {
        PerformanceProfile.BALANCED -> Icons.Filled.Speed
        PerformanceProfile.PERFORMANCE -> Icons.Filled.Bolt
        PerformanceProfile.GAMING -> Icons.Filled.SportsEsports
        PerformanceProfile.DIABLO_MODE -> Icons.Filled.LocalFireDepartment
        PerformanceProfile.SUSTAINED_PERFORMANCE -> Icons.Filled.ThermostatAuto
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("profile_card_${profile.id.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = profile.title,
                        tint = when {
                            isDiablo -> Color(0xFFFF1744)
                            isSelected -> ElectricCyan
                            else -> CobaltTurbo
                        },
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = profile.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isDiablo) Color(0xFFFFB300) else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (profile == PerformanceProfile.PERFORMANCE) {
                                StatusPillBadge(text = "DEFAULT", color = CobaltTurbo)
                            }
                            if (isDiablo) {
                                StatusPillBadge(text = "ROG EXTREME", color = Color(0xFFFF1744))
                            }
                        }
                        Text(
                            text = profile.subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                isDiablo -> Color(0xFFFF1744)
                                isSelected -> ElectricCyan
                                else -> TextSecondary
                            }
                        )
                    }
                }
                Icon(
                    imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = when {
                        isSelected && isDiablo -> Color(0xFFFF1744)
                        isSelected -> ElectricCyan
                        else -> TextMuted
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = profile.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun WorkloadFocusCard(
    focus: WorkloadFocus,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) CobaltTurbo else CarbonBorder
    val containerColor = if (isSelected) CarbonSurfaceElevated else CarbonSurface
    val icon = when (focus) {
        WorkloadFocus.COMBINED_MAX -> Icons.Filled.Bolt
        WorkloadFocus.CPU_PRIMARY -> Icons.Filled.Memory
        WorkloadFocus.GPU_PRIMARY -> Icons.Filled.DeveloperBoard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .testTag("workload_focus_${focus.id.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = focus.title,
                tint = if (isSelected) ElectricCyan else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = focus.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = focus.summary,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = focus.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) ElectricCyan else TextMuted
            )
        }
    }
}

@Composable
private fun SessionHistoryRow(session: SessionHistoryEntity) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(session.startTimestampMs))
    Surface(
        color = CarbonSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${session.profileId} • ${session.workloadFocusId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                StatusPillBadge(
                    text = "${session.durationSeconds}s",
                    color = if (session.experiencedThermalThrottling) TelemetryAmber else TelemetryGreen
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildString {
                    append("$dateStr • Peak Temp: ${String.format(Locale.US, "%.1f°C", session.peakBatteryTempCelsius)}")
                    if (!session.associatedGameName.isNullOrBlank()) {
                        append(" • Game: ${session.associatedGameName}")
                    }
                    if (session.experiencedThermalThrottling) {
                        append(" • Thermal Limited")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
