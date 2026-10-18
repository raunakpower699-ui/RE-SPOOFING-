package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.PerformanceViewModel
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameHubScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RESpoofingTheme
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, "tab_dashboard"),
    PROFILES("Modes", Icons.Filled.Speed, "tab_profiles"),
    GAME_HUB("Game Hub", Icons.Filled.Gamepad, "tab_game_hub"),
    SETTINGS("Settings", Icons.Filled.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PerformanceViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted handled gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            RESpoofingTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: PerformanceViewModel) {
    val telemetryState by viewModel.telemetryState.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()
    val associatedGames by viewModel.associatedGames.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val bannerMessage by viewModel.uiBannerMessage.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(AppNavTab.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RE SPOOFING",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (telemetryState.isSessionActive) DiabloRed else CarbonBorder
                        ) {
                            Text(
                                text = if (telemetryState.isSessionActive) "100% LOCKED" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CarbonSurface,
                modifier = Modifier.border(1.dp, CarbonBorder)
            ) {
                AppNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) ElectricCyan else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) ElectricCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CobaltDeep
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppNavTab.DASHBOARD -> {
                    DashboardScreen(
                        state = telemetryState,
                        onStartPerformanceMode = { viewModel.startPerformanceMode() },
                        onStopPerformanceMode = { viewModel.stopPerformanceMode() },
                        onSelectProfile = { viewModel.selectProfile(it) }
                    )
                }
                AppNavTab.PROFILES -> {
                    ProfilesScreen(
                        state = telemetryState,
                        recentSessions = recentSessions,
                        onSelectProfile = { viewModel.selectProfile(it) },
                        onSelectWorkloadFocus = { viewModel.selectWorkloadFocus(it) },
                        onClearHistory = { viewModel.clearSessionHistory() }
                    )
                }
                AppNavTab.GAME_HUB -> {
                    GameHubScreen(
                        associatedGames = associatedGames,
                        installedApps = installedApps,
                        onAssociateGame = { pkg, name, prof, focus ->
                            viewModel.addGameAssociation(pkg, name, prof, focus)
                        },
                        onRemoveGame = { viewModel.removeGameAssociation(it) },
                        onLaunchGameWithProfile = { viewModel.launchGameWithProfile(it) }
                    )
                }
                AppNavTab.SETTINGS -> {
                    SettingsScreen(
                        preferences = preferences,
                        onToggleBatteryWarning = { viewModel.updateBatteryWarning(it) },
                        onToggleBootRestore = { viewModel.updateBootRestore(it) },
                        onToggleAutoStart = { viewModel.updateAutoStartMaxPower(it) }
                    )
                }
            }

            // Top Banner Message Overlay
            AnimatedVisibility(
                visible = bannerMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                bannerMessage?.let { msg ->
                    Surface(
                        color = CarbonSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, ElectricCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissBanner() }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
