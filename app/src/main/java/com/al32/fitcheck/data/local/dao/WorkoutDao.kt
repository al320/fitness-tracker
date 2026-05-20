package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.local.entities.ExerciseEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSession?>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): WorkoutSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession)

    @Upsert
    suspend fun upsertSession(session: WorkoutSession)

    @Update
    suspend fun updateSession(session: WorkoutSession)

    @Delete
    suspend fun deleteSession(session: WorkoutSession)

    // Exercise Entries
    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getEntriesForSession(sessionId: String): Flow<List<ExerciseEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntry(entry: ExerciseEntry)

    @Delete
    suspend fun deleteEntry(entry: ExerciseEntry)

    @Query("UPDATE exercise_entries SET orderIndex = :newOrder WHERE id = :entryId")
    suspend fun updateEntryOrder(entryId: String, newOrder: Int)

    @Query("SELECT * FROM exercise_entries WHERE sessionId = :sessionId")
    suspend fun getEntriesForSessionSyncNow(sessionId: String): List<ExerciseEntry>
}
