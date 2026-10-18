package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "associated_games")
data class AssociatedGameEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val profileId: String,
    val workloadFocusId: String,
    val autoActivateOnLaunch: Boolean = true,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastLaunchedTimestamp: Long = 0L
)

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: String,
    val workloadFocusId: String,
    val associatedGameName: String?,
    val startTimestampMs: Long,
    val durationSeconds: Long,
    val peakBatteryTempCelsius: Float,
    val experiencedThermalThrottling: Boolean,
    val stopReason: String
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val selectedProfileId: String = "DIABLO_MODE",
    val selectedWorkloadFocusId: String = "COMBINED_MAX",
    val warnOnLowBattery: Boolean = true,
    val rememberPreferenceOnBoot: Boolean = true,
    val vivoIqooUnlockedOverride: Boolean = true,
    val customLogoUri: String? = null,
    val customIntroVideoUri: String? = null,
    val playIntroAnimationOnStart: Boolean = true,
    val autoStartMaxPowerOnLaunch: Boolean = false
)
