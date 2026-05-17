package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SetDao {
    @Query("SELECT * FROM sets WHERE workoutId = :workoutId")
    fun getSetsForWorkout(workoutId: Long): Flow<List<SetEntity>>

    @Query("UPDATE sets SET reps = :reps, weight = :weight, isPr = :isPr, isCompleted = :isCompleted WHERE id = :setId")
    suspend fun updateSetData(setId: Long, reps: Int, weight: Double, isPr: Boolean, isCompleted: Boolean)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId AND isCompleted = 1 ORDER BY weight DESC, reps DESC LIMIT 1")
    suspend fun getPersonalRecord(exerciseId: Long): SetEntity?

    @Query("""
        SELECT * FROM sets 
        WHERE exerciseId = :exerciseId AND workoutId != :currentWorkoutId AND isCompleted = 1 
        ORDER BY timestamp DESC LIMIT :limit
    """)
    suspend fun getPreviousPerformance(exerciseId: Long, currentWorkoutId: Long, limit: Int = 5): List<SetEntity>

    @Query("SELECT * FROM sets")
    suspend fun getAllSetsSync(): List<SetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<SetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity): Long

    @Update
    suspend fun updateSet(set: SetEntity)

    @Delete
    suspend fun deleteSet(set: SetEntity)

    @Query("SELECT * FROM sets WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    fun getHistoryForExercise(exerciseId: Long): Flow<List<SetEntity>>

    @Query("""
        SELECT date(timestamp/1000, 'unixepoch') as day, SUM(weight * reps) as volume 
        FROM sets 
        WHERE isCompleted = 1 
        GROUP BY day 
        ORDER BY day DESC 
        LIMIT 30
    """)
    fun getDailyVolumeHistory(): Flow<List<VolumeHistory>>

    @Query("""
        SELECT e.muscleGroup, SUM(s.weight * s.reps) as intensity
        FROM sets s
        JOIN exercises e ON s.exerciseId = e.id
        WHERE s.isCompleted = 1 AND s.timestamp > :since
        GROUP BY e.muscleGroup
    """)
    fun getMuscleIntensity(since: Long): Flow<List<MuscleIntensity>>

    @Query("SELECT SUM(weight * reps) FROM sets WHERE isCompleted = 1")
    fun getTotalVolume(): Flow<Double?>
}

data class VolumeHistory(val day: String, val volume: Double)
data class MuscleIntensity(val muscleGroup: String, val intensity: Double)
