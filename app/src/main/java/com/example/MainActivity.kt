package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.engine.AndroidPerformanceEngine
import com.example.ui.PerformanceViewModel
import com.example.ui.components.LowBatteryWarningDialog
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.VivoBrandingLogo
import com.example.ui.components.VivoIqooExclusiveLockDialog
import com.example.ui.screens.BrandingSettingsScreen
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameHubScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TelemetryGreen
import com.example.ui.theme.TelemetryRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class VivoNavTab(
    val routeId: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    PROFILES("profiles", "Diablo & Modes", Icons.Filled.Tune, Icons.Outlined.Tune),
    GAMES("game_hub", "Game Hub", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    COMPATIBILITY("compatibility", "Vivo Matrix", Icons.Filled.Memory, Icons.Outlined.Memory),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        val engine = AndroidPerformanceEngine.getInstance(applicationContext)
        engine.setAppForegroundState(true)
        engine.refreshStaticAndDynamicTelemetry()
    }

    override fun onStop() {
        super.onStop()
        AndroidPerformanceEngine.getInstance(applicationContext).setAppForegroundState(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: PerformanceViewModel = viewModel()
                val telemetryState by viewModel.telemetryState.collectAsStateWithLifecycle()

                LaunchedEffect(
                    telemetryState.isSessionActive,
                    telemetryState.sustainedModeRequestedOnWindow,
                    telemetryState.isDiabloModeActive,
                    telemetryState.antiThrottleBoosterEnabled,
                    telemetryState.noTouchPowerLockEnabled,
                    telemetryState.compatibility.sustainedPerformanceSupported
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        telemetryState.compatibility.sustainedPerformanceSupported
                    ) {
                        try {
                            val enableSustainedClamp = telemetryState.sustainedModeRequestedOnWindow &&
                                !telemetryState.antiThrottleBoosterEnabled
                            window?.setSustainedPerformanceMode(enableSustainedClamp)
                        } catch (_: Throwable) {
                        }
                    }

                    try {
                        val win = window
                        if (win != null) {
                            if (telemetryState.isSessionActive) {
                                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            } else {
                                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            }

                            val params = win.attributes
                            if (telemetryState.isSessionActive) {
                                val peakHz = telemetryState.deviceSpecs.displayRefreshRateHz
                                    .coerceAtLeast(if (telemetryState.isDiabloModeActive) 165 else 144)
                                    .toFloat()
                                params.preferredRefreshRate = peakHz
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    @Suppress("DEPRECATION")
                                    val modes = windowManager?.defaultDisplay?.supportedModes
                                    val bestMode = modes?.maxByOrNull { it.refreshRate }
                                    if (bestMode != null) {
                                        params.preferredDisplayModeId = bestMode.modeId
                                    }
                                }
                            } else {
                                params.preferredRefreshRate = 0f
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    params.preferredDisplayModeId = 0
                                }
                            }
                            win.attributes = params
                        }
                    } catch (_: Throwable) {
                    }
                }

                ReSpoofingApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReSpoofingApp(viewModel: PerformanceViewModel) {
    val context = LocalContext.current
    val telemetryState by viewModel.telemetryState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val associatedGames by viewModel.associatedGames.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val showLowBatteryDialog by viewModel.showLowBatteryWarningDialog.collectAsStateWithLifecycle()
    val showVivoIqooLockDialog by viewModel.showVivoIqooLockDialog.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.uiBannerMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(VivoNavTab.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = currentTab != VivoNavTab.DASHBOARD) {
        currentTab = VivoNavTab.DASHBOARD
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.requestStartPerformanceMode()
    }

    val startActionWithNotificationCheck = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.requestStartPerformanceMode()
            }
        } else {
            viewModel.requestStartPerformanceMode()
        }
    }

    val openCustomLogoPicker = {}

    LaunchedEffect(bannerMessage) {
        val msg = bannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBannerMessage()
        }
    }

    if (showVivoIqooLockDialog) {
        VivoIqooExclusiveLockDialog(
            detectedManufacturer = telemetryState.compatibility.manufacturer,
            detectedModel = telemetryState.compatibility.deviceModel,
            onEnableVivoIqooTestMode = { viewModel.enableVivoIqooEmulatorModeAndStart() },
            onDismiss = { viewModel.dismissVivoIqooLockDialog() }
        )
    }

    if (showLowBatteryDialog) {
        LowBatteryWarningDialog(
            batteryPercent = telemetryState.batteryStatus.levelPercent,
            onConfirm = { viewModel.confirmLowBatteryStart() },
            onDismiss = { viewModel.dismissLowBatteryWarning() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 640.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = ObsidianBg,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                VivoBrandingLogo(
                                    customLogoUri = preferences.customLogoUri,
                                    size = 36.dp,
                                    isSessionActive = telemetryState.isSessionActive
                                )
                                Column {
                                    Text(
                                        text = "RE Spoofing",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "By Raunak Exploits • Diablo 97%–100% Lock",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                            }
                        },
                        actions = {
                            StatusPillBadge(
                                text = when {
                                    telemetryState.isSessionActive -> "${telemetryState.lockedPowerPercent.coerceIn(97, 100)}% LOCKED"
                                    !telemetryState.compatibility.isVivoOrIqoo -> "VIVO LOCK"
                                    else -> "STANDBY"
                                },
                                color = when {
                                    telemetryState.isSessionActive -> TelemetryGreen
                                    !telemetryState.compatibility.isVivoOrIqoo -> TelemetryRed
                                    else -> TextSecondary
                                },
                                testTag = "top_bar_status_pill"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.refreshTelemetry() },
                                modifier = Modifier.testTag("refresh_telemetry_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh Telemetry",
                                    tint = ElectricCyan
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = CarbonSurface,
                            titleContentColor = TextPrimary
                        )
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = CarbonSurface,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .border(width = 1.dp, color = CarbonBorder)
                                .testTag("bottom_navigation_bar")
                        ) {
                            VivoNavTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ObsidianBg,
                                        selectedTextColor = Color(0xFFFFB300),
                                        indicatorColor = Color(0xFFFFB300),
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.routeId}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = CarbonSurface,
                            modifier = Modifier
                                .fillMaxHeight()
                                .border(width = 1.dp, color = CarbonBorder)
                                .testTag("side_navigation_rail")
                        ) {
                            VivoNavTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ObsidianBg,
                                        selectedTextColor = Color(0xFFFFB300),
                                        indicatorColor = Color(0xFFFFB300),
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.routeId}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentTab) {
                            VivoNavTab.DASHBOARD -> DashboardScreen(
                                state = telemetryState,
                                customLogoUri = preferences.customLogoUri,
                                onStartClicked = startActionWithNotificationCheck,
                                onStopClicked = { viewModel.stopPerformanceMode() },
                                onQuickProfileSelect = { viewModel.selectProfile(it) },
                                onOpenLogoPicker = openCustomLogoPicker,
                                onToggleVivoIqooSimulation = { viewModel.toggleVivoIqooEmulatorSimulation(it) },
                                onToggleNoTouchPowerLock = { viewModel.toggleNoTouchPowerLock(it) },
                                onToggleAntiThrottleBooster = { viewModel.toggleAntiThrottleBooster(it) },
                                onToggleVivoGameCenterPulse = { viewModel.toggleVivoGameCenterInstantPulse(it) },
                                onPurgeBackgroundAppsNow = { viewModel.purgeBackgroundAppsNow() }
                            )
                            VivoNavTab.PROFILES -> ProfilesScreen(
                                state = telemetryState,
                                recentSessions = recentSessions,
                                onSelectProfile = { viewModel.selectProfile(it) },
                                onSelectWorkloadFocus = { viewModel.selectWorkloadFocus(it) },
                                onClearHistory = { viewModel.clearSessionHistory() }
                            )
                            VivoNavTab.GAMES -> GameHubScreen(
                                associatedGames = associatedGames,
                                installedApps = installedApps,
                                onAssociateGame = { pkg, name, profile, focus ->
                                    viewModel.associateGameWithProfile(pkg, name, profile, focus)
                                },
                                onRemoveGame = { viewModel.removeAssociatedGame(it) },
                                onLaunchGameWithProfile = { viewModel.launchAssociatedGame(it) }
                            )
                            VivoNavTab.COMPATIBILITY -> CompatibilityScreen(
                                compatibility = telemetryState.compatibility
                            )
                            VivoNavTab.SETTINGS -> BrandingSettingsScreen(
                                preferences = preferences,
                                compatibility = telemetryState.compatibility,
                                isSessionActive = telemetryState.isSessionActive,
                                onPickCustomLogo = openCustomLogoPicker,
                                onResetDefaultLogo = { viewModel.updateCustomBrandingLogo(null) },
                                onToggleLowBatteryWarning = { viewModel.setWarnOnLowBattery(it) },
                                onToggleBootPreferenceRestore = { viewModel.setRememberPreferenceOnBoot(it) },
                                onToggleVivoIqooSimulation = { viewModel.toggleVivoIqooEmulatorSimulation(it) },
                                onToggleAutoStartMaxPower = { viewModel.setAutoStartMaxPowerOnLaunch(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
