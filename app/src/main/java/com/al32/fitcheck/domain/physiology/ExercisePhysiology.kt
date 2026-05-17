package com.al32.fitcheck.domain.physiology

data class ExercisePhysiology(
    val exerciseId: Long,
    val pattern: MovementPattern,
    val primaryMuscles: List<MuscleContribution>,
    val secondaryMuscles: List<MuscleContribution>,
    val cnsFatigueModifier: Float = 1.0f
)
