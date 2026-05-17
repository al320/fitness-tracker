package com.al32.fitcheck.data.local

import com.al32.fitcheck.data.local.entities.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val workouts: List<WorkoutEntity>,
    val sets: List<SetEntity>,
    val exercises: List<ExerciseEntity>,
    val workoutExercises: List<WorkoutExerciseEntity>,
    val userStats: UserStatsEntity?
)
