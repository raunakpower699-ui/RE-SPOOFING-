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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("profiles_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PERFORMANCE TUNING MODES",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose how RE Spoofing configures hardware compute locks and governors.",
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
                    text = "INTELLIGENT WORKLOAD FOCUS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select whether compute prioritization targets CPU, GPU, or Combined simultaneously.",
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

        if (recentSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
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
