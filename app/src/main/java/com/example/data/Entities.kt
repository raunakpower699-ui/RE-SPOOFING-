package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "associated_games")
data class AssociatedGameEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val assignedProfileId: String,
    val assignedFocusId: String,
    val dateAddedEpochMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "session_history")
data class SessionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val focusId: String,
    val associatedGameName: String?,
    val startEpochMs: Long,
    val durationSeconds: Long,
    val peakBatteryTempCelsius: Float,
    val thermalThrottlingDetected: Boolean,
    val terminationReason: String
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val batteryProtectionWarningEnabled: Boolean = true,
    val autoRestoreOnBoot: Boolean = false,
    val autoStartMaxPowerOnLaunch: Boolean = false
)
