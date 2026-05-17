package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.local.entities.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE isTemplate = 0 ORDER BY startTime DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("""
        SELECT e.* FROM exercises e
        JOIN workout_exercises we ON e.id = we.exerciseId
        WHERE we.workoutId = :workoutId
        ORDER BY we.`order` ASC
    """)
    fun getExercisesForWorkout(workoutId: Long): Flow<List<ExerciseEntity>>

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity)

    @Query("SELECT * FROM workouts WHERE isTemplate = 1")
    fun getTemplates(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE endTime IS NULL AND isTemplate = 0 LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("UPDATE workout_exercises SET `order` = :newOrder WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun updateExerciseOrder(workoutId: Long, exerciseId: Long, newOrder: Int)

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun removeExerciseFromWorkout(workoutId: Long, exerciseId: Long)

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsSync(): List<WorkoutEntity>

    @Query("SELECT * FROM workout_exercises")
    suspend fun getAllWorkoutExercisesSync(): List<WorkoutExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercises(workoutExercises: List<WorkoutExerciseEntity>)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("UPDATE workouts SET endTime = :endTime, totalXpGained = :xp WHERE id = :id")
    suspend fun finishWorkout(id: Long, endTime: Long, xp: Int)
}
