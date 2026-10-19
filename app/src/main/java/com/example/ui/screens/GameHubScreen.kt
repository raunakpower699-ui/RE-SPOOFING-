package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Security
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
import com.example.data.AssociatedGameEntity
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
    onAssociateGame: (packageName: String, appName: String, profile: PerformanceProfile, focus: WorkloadFocus) -> Unit,
    onRemoveGame: (packageName: String) -> Unit,
    onLaunchGameWithProfile: (AssociatedGameEntity) -> Unit,
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "GAME & BENCHMARK LAUNCH HUB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Associate any game or benchmark with Diablo Mode. Launching from Game Hub locks 100% hardware duty and opens the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Compliance Banner
        item {
            Surface(
                color = CarbonSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TelemetryGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "Safe",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Anti-Cheat Safe: Uses standard Android Performance & Game Mode APIs. Zero APK modification or game hooking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Add Game Association Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CarbonSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ASSOCIATE GAME / APP",
                        style = MaterialTheme.typography.labelLarge,
                        color = ElectricCyan
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
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) ElectricCyan else CarbonBorder
                                ),
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

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customGameName,
                            onValueChange = { customGameName = it },
                            label = { Text("App / Game Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("game_title_input")
                        )
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = { customPackageName = it },
                            label = { Text("Package (e.g. com.tencent.ig)") },
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
                                        customPackageName,
                                        customGameName,
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
                            Text("Associate App", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showInstalledPicker = !showInstalledPicker },
                            modifier = Modifier.weight(1f).testTag("toggle_installed_apps_button")
                        ) {
                            Text(
                                text = if (showInstalledPicker) "Hide Apps" else "Pick Installed (${installedApps.size})",
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // Installed Apps Picker
        if (showInstalledPicker) {
            item {
                Text(
                    text = "AVAILABLE INSTALLED APPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(installedApps.take(15), key = { it.packageName }) { app ->
                val alreadyAdded = associatedPackageSet.contains(app.packageName)
                Surface(
                    color = CarbonSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
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

        // Associated Apps List
        item {
            Text(
                text = "CONFIGURED APPS (${associatedGames.size})",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        if (associatedGames.isEmpty()) {
            item {
                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gamepad,
                            contentDescription = "No Games",
                            tint = TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No apps associated yet. Add your favorite game or benchmark above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(associatedGames, key = { it.packageName }) { game ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                                text = game.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Profile: ${game.assignedProfileId}",
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

                        Spacer(modifier = Modifier.width(6.dp))

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
    }
}
