package com.al32.fitcheck.domain.physiology

data class ExercisePhysiology(
    val exerciseId: String,
    val pattern: MovementPattern,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val cnsFatigueModifier: Float = 1.0f
)
