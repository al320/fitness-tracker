package com.al32.fitcheck.domain.physiology

import com.al32.fitcheck.data.local.entities.Exercise

object PhysiologyProvider {
    val initialExercises = listOf(
        Exercise(
            id = "barbell_bench_press",
            name = "Barbell Bench Press",
            movementPattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleGroup.CHEST_LOWER, MuscleGroup.CHEST_UPPER),
            secondaryMuscles = listOf(MuscleGroup.FRONT_DELT, MuscleGroup.TRICEPS),
            cnsLoad = CNSLoad.MEDIUM
        ),
        Exercise(
            id = "barbell_squat",
            name = "Barbell Squat",
            movementPattern = MovementPattern.SQUAT,
            primaryMuscles = listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES),
            secondaryMuscles = listOf(MuscleGroup.LOWER_BACK, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            cnsLoad = CNSLoad.HIGH
        ),
        Exercise(
            id = "deadlift",
            name = "Deadlift",
            movementPattern = MovementPattern.HINGE,
            primaryMuscles = listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.LOWER_BACK),
            secondaryMuscles = listOf(MuscleGroup.UPPER_BACK, MuscleGroup.LATS, MuscleGroup.FOREARMS),
            cnsLoad = CNSLoad.HIGH
        ),
        Exercise(
            id = "pull_up",
            name = "Pull Up",
            movementPattern = MovementPattern.PULL,
            primaryMuscles = listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK),
            secondaryMuscles = listOf(MuscleGroup.BICEPS, MuscleGroup.REAR_DELT, MuscleGroup.FOREARMS),
            cnsLoad = CNSLoad.MEDIUM
        ),
        Exercise(
            id = "overhead_press",
            name = "Overhead Press",
            movementPattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleGroup.FRONT_DELT, MuscleGroup.SIDE_DELT),
            secondaryMuscles = listOf(MuscleGroup.TRICEPS),
            cnsLoad = CNSLoad.MEDIUM
        )
    )

    private val mapping = initialExercises.associateBy { it.id }.mapValues { (_, ex) ->
        ExercisePhysiology(
            exerciseId = ex.id,
            pattern = ex.movementPattern,
            primaryMuscles = ex.primaryMuscles,
            secondaryMuscles = ex.secondaryMuscles,
            cnsFatigueModifier = when(ex.cnsLoad) {
                CNSLoad.HIGH -> 1.5f
                CNSLoad.MEDIUM -> 1.2f
                CNSLoad.LOW -> 1.0f
            }
        )
    }

    fun getPhysiology(exerciseId: String): ExercisePhysiology? = mapping[exerciseId]
}
