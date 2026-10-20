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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.PerformanceViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameHubScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CobaltDeep
import com.example.ui.theme.DiabloRed
import com.example.ui.theme.DiabloRedGlow
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanGlow
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LiquidGlassGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.RESpoofingTheme
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
    ) { /* handled */ }

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
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, ElectricCyan, CircleShape)
                                .shadow(6.dp, CircleShape, ambientColor = ElectricCyan, spotColor = ElectricCyan)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.raunak_logo),
                                contentDescription = "Raunak Exploits",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "RAUNAK EXPLOITS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (telemetryState.isSessionActive) DiabloRed else GlassBorder
                        ) {
                            Text(
                                text = if (telemetryState.isSessionActive) "100% LOCKED" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
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
            LiquidGlassBottomNavigationDock(
                selectedTab = selectedTab,
                isSessionActive = telemetryState.isSessionActive,
                onTabSelected = { selectedTab = it }
            )
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
                        onSelectProfile = { viewModel.selectProfile(it) },
                        onSelectUniversalTier = { viewModel.selectUniversalTier(it) },
                        onCleanAndOptimizeRam = { viewModel.cleanAndOptimizeRam() }
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
                        color = GlassSurface,
                        shape = RoundedCornerShape(14.dp),
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

@Composable
private fun LiquidGlassBottomNavigationDock(
    selectedTab: AppNavTab,
    isSessionActive: Boolean,
    onTabSelected: (AppNavTab) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val accentColor = if (isSessionActive) DiabloRed else ElectricCyan
    val glowColor = if (isSessionActive) DiabloRedGlow else ElectricCyanGlow

    Surface(
        color = GlassSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.5f),
                    GlassBorder,
                    Color.Transparent
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), ambientColor = glowColor, spotColor = glowColor)
    ) {
        Row(
            modifier = Modifier
                .background(LiquidGlassGradient)
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppNavTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.92f else if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "tabScale"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) CobaltDeep else Color.Transparent,
                    border = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.3f))
                            )
                        )
                    } else null,
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTabSelected(tab)
                            }
                        )
                        .testTag(tab.tag)
                        .then(
                            if (isSelected) Modifier.shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = glowColor, spotColor = glowColor) else Modifier
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) accentColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
