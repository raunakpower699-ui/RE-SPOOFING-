package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceDao {

    @Query("SELECT * FROM associated_games ORDER BY dateAddedEpochMs DESC")
    fun getAllAssociatedGames(): Flow<List<AssociatedGameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGame(game: AssociatedGameEntity)

    @Query("DELETE FROM associated_games WHERE packageName = :packageName")
    suspend fun deleteGame(packageName: String)

    @Query("SELECT * FROM session_history ORDER BY startEpochMs DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<SessionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionHistoryEntity)

    @Query("DELETE FROM session_history")
    suspend fun clearSessionHistory()

    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPreferences(prefs: UserPreferencesEntity)
}
