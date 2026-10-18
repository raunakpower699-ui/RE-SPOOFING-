package com.example.ui

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
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
    val repository = PerformanceRepository(database.performanceDao())
    val engine = AndroidPerformanceEngine.getInstance(appContext)

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

    private val _uiBannerMessage = MutableStateFlow<String?>(null)
    val uiBannerMessage: StateFlow<String?> = _uiBannerMessage.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    init {
        engine.activateAllMaxHardwareSubsystems()
        loadInstalledApplications()

        engine.onSessionCompletedCallback = { profile, focus, gameName, startEpoch, durationSec, peakTemp, throttled, reason ->
            viewModelScope.launch(Dispatchers.IO) {
                repository.recordCompletedSession(
                    profile = profile,
                    workloadFocus = focus,
                    gameName = gameName,
                    startTimestampMs = startEpoch,
                    durationSeconds = durationSec,
                    peakTempCelsius = peakTemp,
                    experiencedThermalThrottling = throttled,
                    stopReason = reason
                )
            }
        }
    }

    private fun loadInstalledApplications() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = appContext.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                val list = resolveInfos.mapNotNull { ri ->
                    val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
                    if (pkg == appContext.packageName) return@mapNotNull null
                    val name = ri.loadLabel(pm)?.toString() ?: pkg
                    InstalledAppItem(packageName = pkg, appName = name)
                }.distinctBy { it.packageName }
                    .sortedBy { it.appName.lowercase() }
                _installedApps.value = list
            } catch (_: Throwable) {
            }
        }
    }

    fun startPerformanceMode(associatedGame: AssociatedGameEntity? = null) {
        val current = telemetryState.value
        val profile = associatedGame?.let { PerformanceProfile.fromId(it.profileId) } ?: current.selectedProfile
        val focus = associatedGame?.let { WorkloadFocus.fromId(it.workloadFocusId) } ?: current.selectedWorkloadFocus
        val pkg = associatedGame?.packageName ?: current.activeGamePackage
        val name = associatedGame?.appName ?: current.activeGameName

        engine.activateAllMaxHardwareSubsystems()
        val success = engine.startPerformanceSession(
            profile = profile,
            workloadFocus = focus,
            associatedGamePackage = pkg,
            associatedGameName = name
        )

        if (success) {
            PerformanceForegroundService.startServiceSession(
                context = appContext,
                profile = profile,
                workloadFocus = focus,
                gamePackage = pkg,
                gameName = name
            )
            _uiBannerMessage.value = "${profile.title} ENGAGED • 100% Locked Duty Cycle Active"
        } else {
            _uiBannerMessage.value = "Unable to start session on this device."
        }
    }

    fun stopPerformanceMode() {
        PerformanceForegroundService.stopServiceSession(appContext)
        engine.stopPerformanceSession("User stopped session")
        _uiBannerMessage.value = "Session stopped • CPU & GPU returned to normal power"
    }

    fun selectProfile(profile: PerformanceProfile) {
        engine.setProfile(profile)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedProfile(profile)
        }
        if (telemetryState.value.isSessionActive) {
            startPerformanceMode()
        }
    }

    fun selectWorkloadFocus(focus: WorkloadFocus) {
        engine.setWorkloadFocus(focus)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSelectedWorkloadFocus(focus)
        }
        if (telemetryState.value.isSessionActive) {
            startPerformanceMode()
        }
    }

    fun addGameAssociation(
        packageName: String,
        appName: String,
        profile: PerformanceProfile,
        workloadFocus: WorkloadFocus,
        autoActivate: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertGameAssociation(
                packageName = packageName,
                appName = appName,
                profile = profile,
                workloadFocus = workloadFocus,
                autoActivate = autoActivate
            )
        }
    }

    fun removeGameAssociation(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeGameAssociation(packageName)
        }
    }

    fun launchGameWithProfile(game: AssociatedGameEntity) {
        startPerformanceMode(game)
        viewModelScope.launch(Dispatchers.IO) {
            repository.markGameLaunched(game.packageName)
        }
        try {
            val pm = appContext.packageManager
            val intent = pm.getLaunchIntentForPackage(game.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appContext.startActivity(intent)
            }
        } catch (_: Throwable) {
        }
    }

    fun updateBatteryWarning(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBatteryWarningSetting(enabled)
        }
    }

    fun updateBootRestore(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBootRestoreSetting(enabled)
        }
    }

    fun updateAutoStartMaxPower(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateAutoStartMaxPowerOnLaunch(enabled)
        }
    }

    fun updatePlayIntroAnimation(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlayIntroAnimationOnStart(enabled)
        }
    }

    fun updateCustomLogoUri(uri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomLogoUri(uri)
        }
    }

    fun updateCustomIntroVideoUri(uri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomIntroVideoUri(uri)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun dismissBanner() {
        _uiBannerMessage.value = null
    }
}
