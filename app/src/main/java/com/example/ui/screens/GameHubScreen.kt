package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssociatedGameEntity
import com.example.data.UserPreferencesEntity
import com.example.model.InstalledAppItem
import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameHubScreen(
    associatedGames: List<AssociatedGameEntity>,
    installedApps: List<InstalledAppItem>,
    preferences: UserPreferencesEntity,
    onAssociateGame: (packageName: String, appName: String, profile: PerformanceProfile, focus: WorkloadFocus) -> Unit,
    onRemoveGame: (packageName: String) -> Unit,
    onLaunchGameWithProfile: (AssociatedGameEntity) -> Unit,
    onToggleBatteryWarning: (Boolean) -> Unit,
    onToggleBootRestore: (Boolean) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    onToggleFloatingFpsOverlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedProfile by remember { mutableStateOf(PerformanceProfile.DIABLO_MODE) }
    var selectedFocus by remember { mutableStateOf(WorkloadFocus.COMBINED_MAX) }
    var customGameName by rememberSaveable { mutableStateOf("") }
    var customPackageName by rememberSaveable { mutableStateOf("") }
    var showInstalledPicker by rememberSaveable { mutableStateOf(false) }

    val associatedPackageSet = remember(associatedGames) {
        associatedGames.map { it.packageName }.toSet()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .testTag("game_hub_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CarbonBorder),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ADD GAME (BGMI / FREE FIRE / APPS)",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PerformanceProfile.entries.forEach { profile ->
                            val selected = selectedProfile == profile
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) CobaltDeep else ObsidianBg,
                                border = BorderStroke(1.dp, if (selected) ElectricCyan else CarbonBorder),
                                modifier = Modifier.clickable { selectedProfile = profile }
                            ) {
                                Text(
                                    text = profile.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) TextPrimary else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkloadFocus.entries.forEach { focus ->
                            val selected = selectedFocus == focus
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) CobaltDeep else ObsidianBg,
                                border = BorderStroke(1.dp, if (selected) TelemetryGreen else CarbonBorder),
                                modifier = Modifier.clickable { selectedFocus = focus }
                            ) {
                                Text(
                                    text = focus.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) TelemetryGreen else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customGameName,
                            onValueChange = { customGameName = it },
                            label = { Text("Game Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("game_title_input")
                        )
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = { customPackageName = it },
                            label = { Text("Package ID") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("game_package_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (customGameName.isNotBlank() && customPackageName.isNotBlank()) {
                                    onAssociateGame(
                                        customPackageName.trim(),
                                        customGameName.trim(),
                                        selectedProfile,
                                        selectedFocus
                                    )
                                    customGameName = ""
                                    customPackageName = ""
                                }
                            },
                            enabled = customGameName.isNotBlank() && customPackageName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier.weight(1f).testTag("add_custom_game_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Game", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showInstalledPicker = !showInstalledPicker },
                            modifier = Modifier.weight(1f).testTag("toggle_installed_apps_button")
                        ) {
                            Text(
                                text = if (showInstalledPicker) "Hide Apps" else "Pick Apps (${installedApps.size})",
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        if (showInstalledPicker) {
            items(installedApps.take(15), key = { it.packageName }) { app ->
                val alreadyAdded = associatedPackageSet.contains(app.packageName)
                Surface(
                    color = CarbonSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (alreadyAdded) {
                            Text(
                                text = "Added",
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetryGreen
                            )
                        } else {
                            Button(
                                onClick = {
                                    onAssociateGame(
                                        app.packageName,
                                        app.appName,
                                        selectedProfile,
                                        selectedFocus
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CobaltDeep)
                            ) {
                                Text("+ Link", color = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }

        if (associatedGames.isEmpty()) {
            item {
                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gamepad,
                            contentDescription = "No Games",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tap Pick Apps above to link BGMI or Free Fire for 1-tap Max FPS launch.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(associatedGames, key = { it.packageName }) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.appName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${game.assignedProfileId} • ${game.assignedFocusId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DiabloRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { onLaunchGameWithProfile(game) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DiabloRed,
                                contentColor = TextPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Launch")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Launch")
                        }

                        IconButton(onClick = { onRemoveGame(game.packageName) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remove",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "QUICK SETTINGS",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan,
                fontWeight = FontWeight.ExtraBold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CarbonBorder),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    CompactSettingRow(
                        title = "Floating FPS Overlay",
                        subtitle = "Show real-time FPS HUD over games",
                        checked = preferences.floatingFpsOverlayEnabled,
                        onCheckedChange = onToggleFloatingFpsOverlay,
                        testTag = "floating_fps_overlay_switch"
                    )
                    CompactSettingRow(
                        title = "Auto-Start Diablo on Launch",
                        subtitle = "Engage Max Lock immediately when app opens",
                        checked = preferences.autoStartMaxPowerOnLaunch,
                        onCheckedChange = onToggleAutoStart
                    )
                    CompactSettingRow(
                        title = "Auto-Restore on Boot",
                        subtitle = "Re-enable lock automatically after device restart",
                        checked = preferences.autoRestoreOnBoot,
                        onCheckedChange = onToggleBootRestore
                    )
                    CompactSettingRow(
                        title = "Thermal Warning Banner",
                        subtitle = "Warn if CPU/GPU cross safe temperature limit",
                        checked = preferences.batteryProtectionWarningEnabled,
                        onCheckedChange = onToggleBatteryWarning
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = ElectricCyan
            )
        )
    }
}
