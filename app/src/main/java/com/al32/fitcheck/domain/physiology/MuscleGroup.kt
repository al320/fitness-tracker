package com.al32.fitcheck.domain.physiology

enum class MuscleGroup(val displayName: String, val recoveryHalfLifeHours: Int) {
    UPPER_CHEST("Upper Chest", 48),
    LOWER_CHEST("Lower Chest", 48),
    FRONT_DELTS("Front Delts", 36),
    SIDE_DELTS("Side Delts", 36),
    REAR_DELTS("Rear Delts", 36),
    BICEPS("Biceps", 24),
    TRICEPS("Triceps", 24),
    FOREARMS("Forearms", 18),
    UPPER_BACK("Upper Back", 48),
    LATS("Lats", 48),
    TRAPS("Traps", 36),
    LOWER_BACK("Lower Back", 72),
    ABS("Abs", 18),
    OBLIQUES("Obliques", 18),
    GLUTES("Glutes", 60),
    QUADS("Quads", 60),
    HAMSTRINGS("Hamstrings", 72),
    CALVES("Calves", 36)
}

data class MuscleContribution(
    val muscle: MuscleGroup,
    val coefficient: Float // 1.0 for primary, 0.2-0.6 for secondary
)

enum class MovementPattern {
    PUSH, PULL, HINGE, SQUAT, CARRY, ISOLATION
}
