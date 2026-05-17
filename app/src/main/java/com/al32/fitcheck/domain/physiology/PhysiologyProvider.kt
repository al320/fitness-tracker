package com.al32.fitcheck.domain.physiology

object PhysiologyProvider {
    private val mapping = mapOf(
        1L to ExercisePhysiology(
            exerciseId = 1L,
            pattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.LOWER_CHEST, 1.0f), MuscleContribution(MuscleGroup.UPPER_CHEST, 0.6f)),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.FRONT_DELTS, 0.5f),
                MuscleContribution(MuscleGroup.TRICEPS, 0.4f)
            )
        ),
        2L to ExercisePhysiology(
            exerciseId = 2L,
            pattern = MovementPattern.SQUAT,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.QUADS, 1.0f), MuscleContribution(MuscleGroup.GLUTES, 0.8f)),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.LOWER_BACK, 0.4f),
                MuscleContribution(MuscleGroup.HAMSTRINGS, 0.3f),
                MuscleContribution(MuscleGroup.CALVES, 0.2f)
            ),
            cnsFatigueModifier = 1.6f
        ),
        3L to ExercisePhysiology(
            exerciseId = 3L,
            pattern = MovementPattern.HINGE,
            primaryMuscles = listOf(
                MuscleContribution(MuscleGroup.HAMSTRINGS, 1.0f),
                MuscleContribution(MuscleGroup.GLUTES, 0.9f),
                MuscleContribution(MuscleGroup.LOWER_BACK, 0.9f)
            ),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.UPPER_BACK, 0.6f),
                MuscleContribution(MuscleGroup.TRAPS, 0.5f),
                MuscleContribution(MuscleGroup.FOREARMS, 0.7f)
            ),
            cnsFatigueModifier = 2.2f
        ),
        4L to ExercisePhysiology(
            exerciseId = 4L,
            pattern = MovementPattern.PULL,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.LATS, 1.0f), MuscleContribution(MuscleGroup.UPPER_BACK, 0.7f)),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.BICEPS, 0.6f),
                MuscleContribution(MuscleGroup.REAR_DELTS, 0.4f),
                MuscleContribution(MuscleGroup.FOREARMS, 0.5f)
            )
        ),
        5L to ExercisePhysiology(
            exerciseId = 5L,
            pattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.FRONT_DELTS, 1.0f), MuscleContribution(MuscleGroup.SIDE_DELTS, 0.7f)),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.TRICEPS, 0.6f),
                MuscleContribution(MuscleGroup.UPPER_CHEST, 0.4f),
                MuscleContribution(MuscleGroup.TRAPS, 0.5f)
            )
        ),
        8L to ExercisePhysiology( // DB Bench
            exerciseId = 8L,
            pattern = MovementPattern.PUSH,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.LOWER_CHEST, 1.0f), MuscleContribution(MuscleGroup.UPPER_CHEST, 0.7f)),
            secondaryMuscles = listOf(
                MuscleContribution(MuscleGroup.FRONT_DELTS, 0.6f),
                MuscleContribution(MuscleGroup.TRICEPS, 0.3f)
            )
        ),
        10L to ExercisePhysiology( // DB Bicep Curl
            exerciseId = 10L,
            pattern = MovementPattern.ISOLATION,
            primaryMuscles = listOf(MuscleContribution(MuscleGroup.BICEPS, 1.0f)),
            secondaryMuscles = listOf(MuscleContribution(MuscleGroup.FOREARMS, 0.5f))
        )
    )

    fun getPhysiology(exerciseId: Long): ExercisePhysiology? = mapping[exerciseId]
}
