package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDao {

    @Query("SELECT * FROM associated_games ORDER BY appName ASC")
    fun getAllAssociatedGames(): Flow<List<AssociatedGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssociatedGame(game: AssociatedGameEntity)

    @Query("DELETE FROM associated_games WHERE packageName = :packageName")
    suspend fun deleteAssociatedGame(packageName: String)

    @Query("UPDATE associated_games SET lastLaunchedTimestamp = :timestamp WHERE packageName = :packageName")
    suspend fun updateGameLaunchTimestamp(packageName: String, timestamp: Long)

    @Query("SELECT * FROM session_history ORDER BY startTimestampMs DESC LIMIT 30")
    fun getRecentSessionHistory(): Flow<List<SessionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionHistory(session: SessionHistoryEntity)

    @Query("DELETE FROM session_history")
    suspend fun clearSessionHistory()

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferencesSnapshot(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(prefs: UserPreferencesEntity)
}
