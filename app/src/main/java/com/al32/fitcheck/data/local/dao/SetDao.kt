package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.SetEntry
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM set_entries WHERE exerciseEntryId = :exerciseEntryId")
    fun getSetsForEntry(exerciseEntryId: String): Flow<List<SetEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntry)

    @Update
    suspend fun updateSet(set: SetEntry)

    @Delete
    suspend fun deleteSet(set: SetEntry)

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries e ON s.exerciseEntryId = e.id
        WHERE e.exerciseId = :exerciseId AND s.isCompleted = 1
        ORDER BY s.completedAt DESC
    """)
    fun getHistoryForExercise(exerciseId: String): Flow<List<SetEntry>>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries e ON s.exerciseEntryId = e.id
        WHERE e.exerciseId = :exerciseId AND s.isCompleted = 1
        ORDER BY s.completedAt DESC LIMIT 1
    """)
    suspend fun getPersonalRecord(exerciseId: String): SetEntry?

    @Query("""
        SELECT s.weight, s.reps, s.completedAt, ex.primaryMuscles, ex.secondaryMuscles, ex.movementPattern
        FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        JOIN exercises ex ON ee.exerciseId = ex.id
        WHERE s.isCompleted = 1 AND s.completedAt > :since
    """)
    fun getCompletedSetsWithPhysiology(since: Long): Flow<List<SetWithPhysiology>>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries e ON s.exerciseEntryId = e.id
        WHERE e.exerciseId = :exerciseId AND s.isCompleted = 1
        AND e.sessionId != :currentSessionId
        ORDER BY s.completedAt DESC LIMIT :limit
    """)
    fun getPreviousPerformance(exerciseId: String, currentSessionId: String, limit: Int = 5): Flow<List<SetEntry>>

    @Query("SELECT * FROM set_entries WHERE exerciseEntryId = :exerciseEntryId")
    fun getSetsForEntrySync(exerciseEntryId: String): Flow<List<SetEntry>>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        WHERE ee.sessionId = :sessionId
    """)
    fun getSetsForSession(sessionId: String): Flow<List<SetEntry>>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        WHERE ee.exerciseId = :exerciseId
    """)
    fun getSetsForExercise(exerciseId: String): Flow<List<SetEntry>>

    @Query("""
        SELECT ee.exerciseId as exerciseId, MAX(s.weight * (1 + s.reps / 30.0)) as bestE1RM
        FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        WHERE s.isCompleted = 1 AND s.completedAt > :since
        GROUP BY ee.exerciseId
    """)
    fun getBestE1RMPerExercise(since: Long): Flow<List<ExerciseBest1RM>>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        WHERE ee.sessionId = :sessionId AND s.isCompleted = 1
    """)
    suspend fun getCompletedSetsForSessionSync(sessionId: String): List<SetEntry>

    @Query("""
        SELECT s.* FROM set_entries s
        JOIN exercise_entries ee ON s.exerciseEntryId = ee.id
        WHERE ee.exerciseId = :exerciseId AND s.isCompleted = 1
    """)
    suspend fun getCompletedSetsForExerciseSync(exerciseId: String): List<SetEntry>
    @Query("SELECT * FROM set_entries WHERE exerciseEntryId = :exerciseEntryId")
    suspend fun getSetsForEntrySyncNow(exerciseEntryId: String): List<SetEntry>
}

data class SetWithPhysiology(
    val weight: Float,
    val reps: Int,
    val completedAt: Long,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val movementPattern: MovementPattern
)

data class ExerciseBest1RM(val exerciseId: String, val bestE1RM: Float)
