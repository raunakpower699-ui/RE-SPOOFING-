package com.example.ui

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AssociatedGameEntity
import com.example.data.PerformanceRepository
import com.example.data.SessionHistoryEntity
import com.example.data.UserPreferencesEntity
import com.example.engine.AndroidPerformanceEngine
import com.example.model.InstalledAppItem
import com.example.model.PerformanceProfile
import com.example.model.PerformanceTelemetryState
import com.example.model.WorkloadFocus
import com.example.service.PerformanceForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val repository = PerformanceRepository(database.performanceDao())
    private val engine = AndroidPerformanceEngine.getInstance(appContext)

    private val isRobolectric: Boolean by lazy {
        Build.FINGERPRINT.contains("robolectric", ignoreCase = true)
    }

    val telemetryState: StateFlow<PerformanceTelemetryState> = engine.telemetryState

    val userPreferences: StateFlow<UserPreferencesEntity> = repository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferencesEntity()
    )

    val associatedGames: StateFlow<List<AssociatedGameEntity>> = repository.associatedGames.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentSessions: StateFlow<List<SessionHistoryEntity>> = repository.recentSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _showLowBatteryWarningDialog = MutableStateFlow(false)
    val showLowBatteryWarningDialog: StateFlow<Boolean> = _showLowBatteryWarningDialog.asStateFlow()

    private val _showVivoIqooLockDialog = MutableStateFlow(false)
    val showVivoIqooLockDialog: StateFlow<Boolean> = _showVivoIqooLockDialog.asStateFlow()

    private var pendingLaunchGame: AssociatedGameEntity? = null

    private val _uiBannerMessage = MutableStateFlow<String?>(null)
    val uiBannerMessage: StateFlow<String?> = _uiBannerMessage.asStateFlow()

    init {
        engine.onSessionCompletedCallback = { profile, focus, gameName, startMs, durationSec, peakTempC, throttled, reason ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordCompletedSession(
                    profile = profile,
                    workloadFocus = focus,
                    gameName = gameName,
                    startTimestampMs = startMs,
                    durationSeconds = durationSec,
                    peakTempCelsius = peakTempC,
                    experiencedThermalThrottling = throttled,
                    stopReason = reason
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val savedPrefs = repository.getSnapshotPreferences()
            val profile = PerformanceProfile.fromId(savedPrefs.selectedProfileId)
            val focus = WorkloadFocus.fromId(savedPrefs.selectedWorkloadFocusId)
            if (savedPrefs.vivoIqooUnlockedOverride) {
                engine.setVivoIqooEmulatorSimulation(true)
            }
            engine.setSelectedProfile(profile)
            engine.setSelectedWorkloadFocus(focus)

            if (!isRobolectric) {
                if (savedPrefs.autoStartMaxPowerOnLaunch && engine.isDeviceAuthorizedForVivoIqoo()) {
                    PerformanceForegroundService.startServiceSession(
                        context = appContext,
                        profile = profile,
                        workloadFocus = focus,
                        gamePackage = null,
                        gameName = null
                    )
                }
            }
            loadInstalledLaunchableApps()
        }
    }

    fun setAutoStartMaxPowerOnLaunch(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAutoStartMaxPowerOnLaunch(enabled)
            _uiBannerMessage.value = if (enabled) {
                "Auto-Start 97%–100% CPU/GPU Max Power on APK Open ENABLED"
            } else {
                "Auto-Start on APK Open disabled"
            }
        }
    }

    fun refreshTelemetry() {
        engine.refreshStaticAndDynamicTelemetry()
        _uiBannerMessage.value = "Live CPU & GPU hardware telemetry refreshed!"
    }

    fun activateAllMaxHardwareSubsystemsNow() {
        engine.activateAllMaxHardwareSubsystems()
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedProfile(PerformanceProfile.ORIGINOS_6_OVERDRIVE)
            repository.updateSelectedWorkloadFocus(WorkloadFocus.COMBINED_MAX)
        }
        val currentTelemetry = telemetryState.value
        if (currentTelemetry.compatibility.isVivoOrIqoo) {
            PerformanceForegroundService.startServiceSession(
                context = appContext,
                profile = PerformanceProfile.ORIGINOS_6_OVERDRIVE,
                workloadFocus = WorkloadFocus.COMBINED_MAX,
                gamePackage = currentTelemetry.activeGamePackage ?: "com.volumeshader",
                gameName = currentTelemetry.activeGameName ?: "EXTREME Mandelbulb 3D Shader (com.volumeshader)"
            )
            _uiBannerMessage.value = "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100%"
        } else {
            _uiBannerMessage.value = "Extreme Render Overdrive armed — Unlock Vivo/iQOO mode to ignite!"
        }
    }

    fun toggleOriginOs6Overdrive(enabled: Boolean) {
        engine.setOriginOs6OverdriveEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100%"
        } else {
            "OriginOS 6 Extreme Render Overdrive set to standby"
        }
    }

    fun toggleRenderScaleSpoof(enabled: Boolean) {
        engine.setRenderScaleSpoofEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "FORCE_RENDER_SCALE_SPOOF: 0.7x Shader Resolution (-30%) -> 1080p Display Spoof ACTIVE"
        } else {
            "Render Scale restored to 1.0x native shader resolution"
        }
    }

    fun toggleVSyncDisableEglSwapZero(enabled: Boolean) {
        engine.setVSyncDisableEglSwapZero(enabled)
        _uiBannerMessage.value = if (enabled) {
            "V_SYNC_DISABLE: Forced eglSwapInterval(0) — UNLOCKED_MAXIMUM_PHYSICAL_LIMIT"
        } else {
            "V-Sync restored to standard eglSwapInterval(1)"
        }
    }

    fun launchVolumeShaderBenchmarkPipeline() {
        val currentTelemetry = telemetryState.value
        if (!currentTelemetry.compatibility.isVivoOrIqoo) {
            _showVivoIqooLockDialog.value = true
            return
        }
        engine.activateAllMaxHardwareSubsystems()
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedProfile(PerformanceProfile.ORIGINOS_6_OVERDRIVE)
            repository.updateSelectedWorkloadFocus(WorkloadFocus.COMBINED_MAX)
            repository.upsertGameAssociation(
                packageName = "com.volumeshader",
                appName = "EXTREME Mandelbulb 3D Shader (com.volumeshader)",
                profile = PerformanceProfile.ORIGINOS_6_OVERDRIVE,
                workloadFocus = WorkloadFocus.COMBINED_MAX,
                autoActivate = true
            )
        }
        PerformanceForegroundService.startServiceSession(
            context = appContext,
            profile = PerformanceProfile.ORIGINOS_6_OVERDRIVE,
            workloadFocus = WorkloadFocus.COMBINED_MAX,
            gamePackage = "com.volumeshader",
            gameName = "EXTREME Mandelbulb 3D Shader (com.volumeshader)"
        )
        try {
            val pm = appContext.packageManager
            val launchIntent = pm.getLaunchIntentForPackage("com.volumeshader")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(launchIntent)
                _uiBannerMessage.value = "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100% | Launching com.volumeshader!"
            } else {
                _uiBannerMessage.value = "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100% (0.7x Mandelbulb 3D Active)"
            }
        } catch (_: Throwable) {
            _uiBannerMessage.value = "RENDER_SCALING_ACTIVE | THERMAL_BYPASS_ENGAGED | GPU_DUTY: 100%"
        }
    }

    fun requestStartPerformanceMode(forceOverrideBatteryWarning: Boolean = false) {
        val currentTelemetry = telemetryState.value

        if (!currentTelemetry.compatibility.isVivoOrIqoo) {
            pendingLaunchGame = null
            _showVivoIqooLockDialog.value = true
            return
        }

        val prefs = userPreferences.value

        if (!forceOverrideBatteryWarning && prefs.warnOnLowBattery && currentTelemetry.batteryStatus.isLowBattery) {
            pendingLaunchGame = null
            _showLowBatteryWarningDialog.value = true
            return
        }

        _showLowBatteryWarningDialog.value = false
        PerformanceForegroundService.startServiceSession(
            context = appContext,
            profile = currentTelemetry.selectedProfile,
            workloadFocus = currentTelemetry.selectedWorkloadFocus,
            gamePackage = null,
            gameName = null
        )
        _uiBannerMessage.value = "97%–100% Constant CPU/GPU Max Lock ACTIVE (Runs non-stop until exited from Notification Panel)"
    }

    fun enableVivoIqooEmulatorModeAndStart() {
        _showVivoIqooLockDialog.value = false
        engine.setVivoIqooEmulatorSimulation(true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVivoIqooUnlockSetting(true)
        }
        val game = pendingLaunchGame
        pendingLaunchGame = null
        if (game != null) {
            launchAssociatedGame(game)
        } else {
            requestStartPerformanceMode()
        }
    }

    fun toggleVivoIqooEmulatorSimulation(enabled: Boolean) {
        engine.setVivoIqooEmulatorSimulation(enabled)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVivoIqooUnlockSetting(enabled)
        }
        _uiBannerMessage.value = if (enabled) {
            "Vivo / iQOO Mode Unlocked — Ready to START!"
        } else {
            "Strict Vivo / iQOO Hardware Lock active."
        }
    }

    fun dismissVivoIqooLockDialog() {
        _showVivoIqooLockDialog.value = false
        pendingLaunchGame = null
    }

    fun confirmLowBatteryStart() {
        _showLowBatteryWarningDialog.value = false
        val game = pendingLaunchGame
        pendingLaunchGame = null
        if (game != null) {
            executeGameLaunchWithProfile(game)
        } else {
            requestStartPerformanceMode(forceOverrideBatteryWarning = true)
        }
    }

    fun dismissLowBatteryWarning() {
        _showLowBatteryWarningDialog.value = false
        pendingLaunchGame = null
    }

    fun stopPerformanceMode() {
        PerformanceForegroundService.stopServiceSession(appContext, "User pressed STOP")
        _uiBannerMessage.value = "Performance session stopped & hardware locks released."
    }

    fun toggleNoTouchPowerLock(enabled: Boolean) {
        engine.setNoTouchPowerLockEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "97%–100% Constant CPU & OpenGL GPU Max Lock ENABLED (Zero Drops)"
        } else {
            "CPU/GPU Lock set to standard OS demand"
        }
    }

    fun toggleAntiThrottleBooster(enabled: Boolean) {
        engine.setAntiThrottleBoosterEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "5–10 Min 97%–100% Anti-Throttle Thermal Booster ENABLED"
        } else {
            "Thermal Booster set to standard OS thermal curve"
        }
    }

    fun toggleVivoGameCenterInstantPulse(enabled: Boolean) {
        engine.setVivoGameCenterInstantPulseEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "Vivo Game Center Instant-Max 5x ADPF Pulse ENABLED"
        } else {
            "Vivo Game Center Instant-Max Pulse paused"
        }
    }

    fun toggleLowLatencyAudioDspLock(enabled: Boolean) {
        engine.setLowLatencyAudioDspLockEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "Low-Latency Game Audio DSP FastPath (PERFORMANCE_MODE_LOW_LATENCY) ENABLED"
        } else {
            "Low-Latency Audio DSP FastPath paused"
        }
    }

    fun toggleMemoryBandwidthPrefetch(enabled: Boolean) {
        engine.setMemoryBandwidthPrefetchEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "LPDDR Memory Controller 64B Cache-Line Prefetch Lock ENABLED"
        } else {
            "LPDDR Memory Controller Prefetch paused"
        }
    }

    fun toggleMinimalPostProcessingDisplay(enabled: Boolean) {
        engine.setMinimalPostProcessingDisplayEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "Display Minimal Post-Processing & Peak VSYNC Lock ENABLED"
        } else {
            "Display Minimal Post-Processing set to standard"
        }
    }

    fun toggleTouchSensorBoost(enabled: Boolean) {
        engine.setTouchSensorBoostEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "High-Rate Game IMU Sensor (SENSOR_DELAY_GAME) & Unbuffered Touch Lock ENABLED"
        } else {
            "High-Rate Game IMU Sensor Lock paused"
        }
    }

    fun toggleStorageIoBoost(enabled: Boolean) {
        engine.setStorageIoBoostEnabled(enabled)
        _uiBannerMessage.value = if (enabled) {
            "UFS 3.1/4.0 Direct 16KB FileChannel I/O Keep-Alive ENABLED"
        } else {
            "UFS Direct FileChannel I/O Keep-Alive paused"
        }
    }

    fun updateLiveChoreographerMetrics(fps: Int, frameTimeMs: Float) {
        engine.updateLiveChoreographerFrameMetrics(fps, frameTimeMs)
    }

    fun purgeBackgroundAppsNow() {
        viewModelScope.launch(Dispatchers.IO) {
            val (killedCount, freedMb) = engine.purgeBackgroundProcessesAndBoostRam()
            _uiBannerMessage.value = "Purged $killedCount background apps via ActivityManager (${freedMb} MB freed)!"
        }
    }

    fun selectProfile(profile: PerformanceProfile) {
        engine.setSelectedProfile(profile)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedProfile(profile)
        }
    }

    fun selectWorkloadFocus(focus: WorkloadFocus) {
        engine.setSelectedWorkloadFocus(focus)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedWorkloadFocus(focus)
        }
    }

    fun associateGameWithProfile(
        packageName: String,
        appName: String,
        profile: PerformanceProfile,
        workloadFocus: WorkloadFocus,
        autoActivate: Boolean = true
    ) {
        if (packageName.isBlank() || appName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertGameAssociation(
                packageName = packageName.trim(),
                appName = appName.trim(),
                profile = profile,
                workloadFocus = workloadFocus,
                autoActivate = autoActivate
            )
            _uiBannerMessage.value = "Associated $appName with ${profile.title} mode."
        }
    }

    fun removeAssociatedGame(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeGameAssociation(packageName)
        }
    }

    fun launchAssociatedGame(game: AssociatedGameEntity) {
        val currentTelemetry = telemetryState.value

        if (!currentTelemetry.compatibility.isVivoOrIqoo) {
            pendingLaunchGame = game
            _showVivoIqooLockDialog.value = true
            return
        }

        val prefs = userPreferences.value

        if (prefs.warnOnLowBattery && currentTelemetry.batteryStatus.isLowBattery) {
            pendingLaunchGame = game
            _showLowBatteryWarningDialog.value = true
            return
        }

        executeGameLaunchWithProfile(game)
    }

    private fun executeGameLaunchWithProfile(game: AssociatedGameEntity) {
        val profile = PerformanceProfile.fromId(game.profileId)
        val focus = WorkloadFocus.fromId(game.workloadFocusId)

        engine.setSelectedProfile(profile)
        engine.setSelectedWorkloadFocus(focus)

        if (game.autoActivateOnLaunch) {
            PerformanceForegroundService.startServiceSession(
                context = appContext,
                profile = profile,
                workloadFocus = focus,
                gamePackage = game.packageName,
                gameName = game.appName
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.markGameLaunched(game.packageName)
        }

        try {
            val pm = appContext.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(game.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(launchIntent)
                _uiBannerMessage.value = "RE Spoofing active for ${game.appName} — Launching app."
            } else {
                _uiBannerMessage.value = "RE Spoofing [${profile.title}] activated for ${game.appName}."
            }
        } catch (_: Throwable) {
            _uiBannerMessage.value = "RE Spoofing [${profile.title}] activated for ${game.appName}."
        }
    }

    fun clearBannerMessage() {
        _uiBannerMessage.value = null
    }

    fun updateCustomBrandingLogo(uriString: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomLogoUri(uriString)
            _uiBannerMessage.value = if (uriString != null) {
                "Custom RE Spoofing branding logo applied."
            } else {
                "Restored default RE Spoofing emblem logo."
            }
        }
    }

    fun setWarnOnLowBattery(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBatteryWarningSetting(enabled)
        }
    }

    fun setRememberPreferenceOnBoot(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBootRestoreSetting(enabled)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun loadInstalledLaunchableApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = appContext.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
                val ownPkg = appContext.packageName
                val items = resolveInfos
                    .mapNotNull { info ->
                        val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                        if (pkg == ownPkg) return@mapNotNull null
                        val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                        val flags = info.activityInfo?.applicationInfo?.flags ?: 0
                        val isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        InstalledAppItem(
                            packageName = pkg,
                            appName = label,
                            isSystemApp = isSystem
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedWith(compareBy<InstalledAppItem> { it.isSystemApp }.thenBy { it.appName.lowercase() })

                _installedApps.value = items
            } catch (_: Throwable) {
                _installedApps.value = emptyList()
            }
        }
    }
}
