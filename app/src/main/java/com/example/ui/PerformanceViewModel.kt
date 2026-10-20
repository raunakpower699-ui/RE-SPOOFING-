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
import com.example.model.UniversalDeviceTier
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

    private val engine = AndroidPerformanceEngine.getInstance(application)
    private val repository: PerformanceRepository

    val telemetryState: StateFlow<PerformanceTelemetryState> = engine.telemetryState

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _uiBannerMessage = MutableStateFlow<String?>(null)
    val uiBannerMessage: StateFlow<String?> = _uiBannerMessage.asStateFlow()

    val associatedGames: StateFlow<List<AssociatedGameEntity>>
    val recentSessions: StateFlow<List<SessionHistoryEntity>>
    val userPreferences: StateFlow<UserPreferencesEntity>

    init {
        val db = AppDatabase.getInstance(application)
        repository = PerformanceRepository(db.performanceDao())

        associatedGames = repository.associatedGames.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        recentSessions = repository.recentSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        userPreferences = repository.userPreferences.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesEntity()
        )

        engine.onSessionCompletedCallback = { profile, focus, gameName, startMs, durSec, peakTemp, throttled, reason ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordSession(
                    profileId = profile.id,
                    focusId = focus.id,
                    gameName = gameName,
                    startEpochMs = startMs,
                    durationSeconds = durSec,
                    peakTempCelsius = peakTemp,
                    throttled = throttled,
                    reason = reason
                )
            }
        }

        loadInstalledApplications()
    }

    fun selectProfile(profile: PerformanceProfile) {
        engine.setProfile(profile)
    }

    fun selectUniversalTier(tier: UniversalDeviceTier) {
        engine.setUniversalTier(tier)
        _uiBannerMessage.value = "Device Profile: ${tier.title} applied."
    }

    fun cleanAndOptimizeRam() {
        val freedMb = engine.optimizeAndCleanRam()
        _uiBannerMessage.value = "⚡ Makkhan Speed: Freed ~$freedMb MB RAM cache! All devices run silky smooth."
    }

    fun selectWorkloadFocus(focus: WorkloadFocus) {
        engine.setWorkloadFocus(focus)
    }

    fun startPerformanceMode(associatedGamePackage: String? = null, associatedGameName: String? = null) {
        val currentProfile = engine.telemetryState.value.selectedProfile
        val success = engine.startPerformanceSession(
            profile = currentProfile,
            associatedGamePackage = associatedGamePackage,
            associatedGameName = associatedGameName
        )
        if (success) {
            try {
                PerformanceForegroundService.startService(getApplication())
            } catch (_: Throwable) {
            }
            _uiBannerMessage.value = "🔥 ${currentProfile.title} Active • Maximum Full Power Lock Engaged!"
        } else {
            _uiBannerMessage.value = "Unable to start performance session."
        }
    }

    fun stopPerformanceMode() {
        engine.stopPerformanceSession("User Stopped")
        try {
            PerformanceForegroundService.stopService(getApplication())
        } catch (_: Throwable) {
        }
        _uiBannerMessage.value = "Performance session stopped. Returned to normal idle."
    }

    fun dismissBanner() {
        _uiBannerMessage.value = null
    }

    fun addGameAssociation(
        packageName: String,
        appName: String,
        profile: PerformanceProfile,
        focus: WorkloadFocus
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.associateGame(packageName, appName, profile.id, focus.id)
        }
    }

    fun removeGameAssociation(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeGame(packageName)
        }
    }

    fun launchGameWithProfile(game: AssociatedGameEntity) {
        val profile = PerformanceProfile.fromId(game.assignedProfileId)
        val focus = WorkloadFocus.fromId(game.assignedFocusId)
        engine.setProfile(profile)
        engine.setWorkloadFocus(focus)
        startPerformanceMode(game.packageName, game.appName)

        try {
            val pm = getApplication<Application>().packageManager
            val launchIntent = pm.getLaunchIntentForPackage(game.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(launchIntent)
            } else {
                _uiBannerMessage.value = "App ${game.appName} locked at 100% duty, but launch intent not found."
            }
        } catch (_: Throwable) {
            _uiBannerMessage.value = "Performance locked at 100%. Failed to open ${game.appName} directly."
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun updateBatteryWarning(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(batteryProtectionWarningEnabled = enabled))
        }
    }

    fun updateBootRestore(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(autoRestoreOnBoot = enabled))
        }
    }

    fun updateAutoStartMaxPower(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.value
            repository.updatePreferences(current.copy(autoStartMaxPowerOnLaunch = enabled))
        }
    }

    private fun loadInstalledApplications() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledApplications(0)
                }

                val apps = packages
                    .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("benchmark") || it.packageName.contains("antutu") || it.packageName.contains("geekbench") || it.packageName.contains("game") }
                    .map {
                        InstalledAppItem(
                            packageName = it.packageName,
                            appName = pm.getApplicationLabel(it).toString()
                        )
                    }
                    .sortedBy { it.appName }

                _installedApps.value = apps
            } catch (_: Throwable) {
                _installedApps.value = emptyList()
            }
        }
    }
}
