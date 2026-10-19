package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PerformanceRepository(private val dao: PerformanceDao) {

    val associatedGames: Flow<List<AssociatedGameEntity>> = dao.getAllAssociatedGames()
    val recentSessions: Flow<List<SessionHistoryEntity>> = dao.getRecentSessions()
    val userPreferences: Flow<UserPreferencesEntity> = dao.getUserPreferences().map { it ?: UserPreferencesEntity() }

    suspend fun associateGame(packageName: String, appName: String, profileId: String, focusId: String) {
        dao.insertOrUpdateGame(
            AssociatedGameEntity(
                packageName = packageName,
                appName = appName,
                assignedProfileId = profileId,
                assignedFocusId = focusId
            )
        )
    }

    suspend fun removeGame(packageName: String) {
        dao.deleteGame(packageName)
    }

    suspend fun recordSession(
        profileId: String,
        focusId: String,
        gameName: String?,
        startEpochMs: Long,
        durationSeconds: Long,
        peakTempCelsius: Float,
        throttled: Boolean,
        reason: String
    ) {
        dao.insertSession(
            SessionHistoryEntity(
                profileId = profileId,
                focusId = focusId,
                associatedGameName = gameName,
                startEpochMs = startEpochMs,
                durationSeconds = durationSeconds,
                peakBatteryTempCelsius = peakTempCelsius,
                thermalThrottlingDetected = throttled,
                terminationReason = reason
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearSessionHistory()
    }

    suspend fun updatePreferences(prefs: UserPreferencesEntity) {
        dao.saveUserPreferences(prefs)
    }
}
