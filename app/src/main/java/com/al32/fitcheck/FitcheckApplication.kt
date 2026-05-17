package com.al32.fitcheck

import android.app.Application
import com.al32.fitcheck.data.local.AppDatabase
import com.al32.fitcheck.data.repository.FitcheckRepository

import com.al32.fitcheck.data.local.entities.ExerciseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitcheckApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        FitcheckRepository(
            database.workoutDao(),
            database.exerciseDao(),
            database.setDao(),
            database.userStatsDao()
        ) 
    }

    override fun onCreate() {
        super.onCreate()
        preseedExercises()
    }

    private fun preseedExercises() {
        applicationScope.launch {
            if (repository.getExerciseCount() == 0) {
                repository.insertExercises(listOf(
                    ExerciseEntity(name = "Bench Press", muscleGroup = "Chest", baseXP = 20),
                    ExerciseEntity(name = "Squat", muscleGroup = "Legs", baseXP = 25),
                    ExerciseEntity(name = "Deadlift", muscleGroup = "Back", baseXP = 30),
                    ExerciseEntity(name = "Shoulder Press", muscleGroup = "Shoulders", baseXP = 15),
                    ExerciseEntity(name = "Pull Up", muscleGroup = "Back", baseXP = 15)
                ))
            }
        }
    }
}
