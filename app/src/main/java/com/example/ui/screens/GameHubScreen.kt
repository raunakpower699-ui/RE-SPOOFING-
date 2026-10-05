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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
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
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.CobaltTurbo
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Game Launch Support (Section 14):
 * Allows the user to select games/apps installed on the device and associate them with
 * Vivo Spoofing's supported performance/game-mode configuration.
 * Strictly zero code injection, zero APK modification, and zero anti-cheat hooking.
 */
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
    var selectedProfile by remember { mutableStateOf(PerformanceProfile.GAMING) }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "GAME LAUNCH HUB",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Associate installed games with a dedicated Performance Profile. Launching from Game Hub starts the Foreground Performance Service and opens the game via standard Android intents.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // Anti-Cheat & Security Compliance Banner
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
                        contentDescription = "Legitimate Launch Guarantee",
                        tint = TelemetryGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Anti-Cheat Safe: Vivo Spoofing never injects code into games, never modifies game APKs, and never hooks protected processes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Default Profile Configuration for New Game Association
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
                        text = "ASSOCIATION PROFILE PRESET",
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

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "WORKLOAD FOCUS PRESET",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkloadFocus.entries.forEach { focus ->
                            val selected = selectedFocus == focus
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) CarbonSurfaceElevated else ObsidianBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) CobaltTurbo else CarbonBorder
                                ),
                                modifier = Modifier.clickable { selectedFocus = focus }
                            ) {
                                Text(
                                    text = focus.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) ElectricCyan else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Add Custom or Installed App
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customGameName,
                            onValueChange = { customGameName = it },
                            label = { Text("Game / App Title") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("game_title_input")
                        )
                        OutlinedTextField(
                            value = customPackageName,
                            onValueChange = { customPackageName = it },
                            label = { Text("Package Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("game_package_input")
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
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_custom_game_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Game")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Associate Game", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showInstalledPicker = !showInstalledPicker },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_installed_apps_button")
                        ) {
                            Text(
                                text = if (showInstalledPicker) "Hide Installed Apps" else "Pick Installed App (${installedApps.size})",
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // Installed Device Apps Picker
        if (showInstalledPicker) {
            item {
                Text(
                    text = "INSTALLED DEVICE APPLICATIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = ElectricCyan
                )
            }
            items(installedApps.take(25), key = { "installed_${it.packageName}" }) { app ->
                val alreadyAssociated = associatedPackageSet.contains(app.packageName)
                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(12.dp),
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
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = {
                                onAssociateGame(
                                    app.packageName,
                                    app.appName,
                                    selectedProfile,
                                    selectedFocus
                                )
                            },
                            enabled = !alreadyAssociated,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CobaltTurbo,
                                contentColor = TextPrimary
                            ),
                            modifier = Modifier.testTag("associate_installed_${app.packageName}")
                        ) {
                            Text(if (alreadyAssociated) "Added" else "Add")
                        }
                    }
                }
            }
        }

        // Associated Games List
        item {
            Text(
                text = "ASSOCIATED GAMES (${associatedGames.size})",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricCyan
            )
        }

        if (associatedGames.isEmpty()) {
            item {
                Surface(
                    color = CarbonSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsEsports,
                            contentDescription = null,
                            tint = CobaltTurbo,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No games associated yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pick an installed application above or enter a game package to bind it to Vivo Spoofing's GAMING or PERFORMANCE profile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(associatedGames, key = { it.packageName }) { game ->
                AssociatedGameCard(
                    game = game,
                    onLaunch = { onLaunchGameWithProfile(game) },
                    onRemove = { onRemoveGame(game.packageName) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AssociatedGameCard(
    game: AssociatedGameEntity,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CobaltTurbo.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag("associated_game_${game.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.appName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = game.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.testTag("remove_game_${game.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove associated game",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPillBadge(text = "Profile: ${game.profileId}", color = ElectricCyan)
                StatusPillBadge(text = "Focus: ${game.workloadFocusId}", color = CobaltTurbo)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianBg
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_game_${game.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Filled.RocketLaunch,
                    contentDescription = "Launch with Performance Mode"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START PERFORMANCE MODE & LAUNCH",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
