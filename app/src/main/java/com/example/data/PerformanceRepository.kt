package com.example.data

import com.example.model.PerformanceProfile
import com.example.model.WorkloadFocus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PerformanceRepository(private val dao: PerformanceDao) {

    val associatedGames: Flow<List<AssociatedGameEntity>> = dao.getAllAssociatedGames()

    val recentSessions: Flow<List<SessionHistoryEntity>> = dao.getRecentSessionHistory()

    val userPreferences: Flow<UserPreferencesEntity> = dao.getUserPreferencesFlow().map {
        it ?: UserPreferencesEntity()
    }

    suspend fun getSnapshotPreferences(): UserPreferencesEntity {
        return dao.getUserPreferencesSnapshot() ?: UserPreferencesEntity().also {
            dao.saveUserPreferences(it)
        }
    }

    suspend fun updateSelectedProfile(profile: PerformanceProfile) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(selectedProfileId = profile.id))
    }

    suspend fun updateSelectedWorkloadFocus(focus: WorkloadFocus) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(selectedWorkloadFocusId = focus.id))
    }

    suspend fun updateCustomLogoUri(uri: String?) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(customLogoUri = uri))
    }

    suspend fun updateBatteryWarningSetting(enabled: Boolean) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(warnOnLowBattery = enabled))
    }

    suspend fun updateBootRestoreSetting(enabled: Boolean) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(rememberPreferenceOnBoot = enabled))
    }

    suspend fun updateVivoIqooUnlockSetting(unlocked: Boolean) {
        val current = getSnapshotPreferences()
        dao.saveUserPreferences(current.copy(vivoIqooUnlockedOverride = unlocked))
    }

    suspend fun upsertGameAssociation(
        packageName: String,
        appName: String,
        profile: PerformanceProfile,
        workloadFocus: WorkloadFocus,
        autoActivate: Boolean
    ) {
        dao.upsertAssociatedGame(
            AssociatedGameEntity(
                packageName = packageName,
                appName = appName,
                profileId = profile.id,
                workloadFocusId = workloadFocus.id,
                autoActivateOnLaunch = autoActivate
            )
        )
    }

    suspend fun removeGameAssociation(packageName: String) {
        dao.deleteAssociatedGame(packageName)
    }

    suspend fun markGameLaunched(packageName: String) {
        dao.updateGameLaunchTimestamp(packageName, System.currentTimeMillis())
    }

    suspend fun recordCompletedSession(
        profile: PerformanceProfile,
        workloadFocus: WorkloadFocus,
        gameName: String?,
        startTimestampMs: Long,
        durationSeconds: Long,
        peakTempCelsius: Float,
        experiencedThermalThrottling: Boolean,
        stopReason: String
    ) {
        dao.insertSessionHistory(
            SessionHistoryEntity(
                profileId = profile.id,
                workloadFocusId = workloadFocus.id,
                associatedGameName = gameName,
                startTimestampMs = startTimestampMs,
                durationSeconds = durationSeconds,
                peakBatteryTempCelsius = peakTempCelsius,
                experiencedThermalThrottling = experiencedThermalThrottling,
                stopReason = stopReason
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearSessionHistory()
    }
}
