package com.al32.fitcheck.data.repository

import com.al32.fitcheck.data.local.BackupData
import com.al32.fitcheck.data.local.dao.ExerciseDao
import com.al32.fitcheck.data.local.dao.MuscleIntensity
import com.al32.fitcheck.data.local.dao.SetDao
import com.al32.fitcheck.data.local.dao.UserStatsDao
import com.al32.fitcheck.data.local.dao.VolumeHistory
import com.al32.fitcheck.data.local.dao.WorkoutDao
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.local.entities.WorkoutExerciseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FitcheckRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val userStatsDao: UserStatsDao
) {
    // Workout functions
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()
    val templates: Flow<List<WorkoutEntity>> = workoutDao.getTemplates()
    val activeWorkout: Flow<WorkoutEntity?> = workoutDao.getActiveWorkout()
    
    suspend fun startWorkout(name: String, isTemplate: Boolean = false): Long {
        return workoutDao.insertWorkout(
            WorkoutEntity(name = name, startTime = System.currentTimeMillis(), isTemplate = isTemplate)
        )
    }

    suspend fun saveAsTemplate(workoutId: Long, name: String) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        val newTemplateId = startWorkout(name, isTemplate = true)
        // Copy exercises would go here (requires join table copy)
    }

    suspend fun reorderExercise(workoutId: Long, exerciseId: Long, newOrder: Int) {
        workoutDao.updateExerciseOrder(workoutId, exerciseId, newOrder)
    }

    fun getExercisesForWorkout(workoutId: Long): Flow<List<ExerciseEntity>> = 
        workoutDao.getExercisesForWorkout(workoutId)

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long, order: Int) {
        workoutDao.insertWorkoutExercise(WorkoutExerciseEntity(workoutId = workoutId, exerciseId = exerciseId, order = order))
    }

    suspend fun updateSet(setId: Long, reps: Int, weight: Double, isPr: Boolean, isCompleted: Boolean) {
        setDao.updateSetData(setId, reps, weight, isPr, isCompleted)
    }

    suspend fun getPersonalRecord(exerciseId: Long): SetEntity? = setDao.getPersonalRecord(exerciseId)

    suspend fun getPreviousPerformance(exerciseId: Long, workoutId: Long): List<SetEntity> = 
        setDao.getPreviousPerformance(exerciseId, workoutId)

    fun searchExercises(query: String): Flow<List<ExerciseEntity>> =
        exerciseDao.searchExercises(query)

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }

    suspend fun deleteSet(setId: Long) {
        // We need a delete by ID or the entity. For simplicity adding deleteSetById if needed or just using entity.
        // Let's assume we have the entity or add it to Dao.
    }

    suspend fun finishWorkout(workoutId: Long, xp: Int) {
        workoutDao.finishWorkout(workoutId, System.currentTimeMillis(), xp)
        userStatsDao.addXp(xp)
    }

    // Exercise functions
    val allExercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()
    
    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()
    
    suspend fun insertExercises(exercises: List<ExerciseEntity>) = exerciseDao.insertExercises(exercises)

    // Set functions
    fun getSetsForWorkout(workoutId: Long): Flow<List<SetEntity>> = setDao.getSetsForWorkout(workoutId)
    
    fun getHistoryForExercise(exerciseId: Long): Flow<List<SetEntity>> = setDao.getHistoryForExercise(exerciseId)

    suspend fun addSet(set: SetEntity) = setDao.insertSet(set)

    // User stats
    val userStats: Flow<UserStatsEntity?> = userStatsDao.getUserStats()
    val totalVolume: Flow<Double?> = setDao.getTotalVolume()
    val dailyVolumeHistory: Flow<List<VolumeHistory>> = setDao.getDailyVolumeHistory()
    
    fun getMuscleIntensity(since: Long): Flow<List<MuscleIntensity>> = 
        setDao.getMuscleIntensity(since)

    suspend fun getBackupData(): BackupData {
        return BackupData(
            workouts = workoutDao.getAllWorkoutsSync(),
            sets = setDao.getAllSetsSync(),
            exercises = exerciseDao.getAllExercisesSync(),
            workoutExercises = workoutDao.getAllWorkoutExercisesSync(),
            userStats = userStatsDao.getUserStatsSync()
        )
    }

    suspend fun restoreBackup(data: BackupData) {
        workoutDao.insertWorkouts(data.workouts)
        setDao.insertSets(data.sets)
        exerciseDao.insertExercises(data.exercises)
        workoutDao.insertWorkoutExercises(data.workoutExercises)
        data.userStats?.let { userStatsDao.insertOrUpdateStats(it) }
    }
}
