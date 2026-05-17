package com.al32.fitcheck.domain.physiology

enum class MuscleGroup {
    CHEST_UPPER, CHEST_LOWER, FRONT_DELT, SIDE_DELT, REAR_DELT,
    BICEPS, TRICEPS, FOREARMS,
    UPPER_BACK, LATS, LOWER_BACK,
    QUADS, HAMSTRINGS, GLUTES, CALVES, ABS
}

enum class MovementPattern { PUSH, PULL, HINGE, SQUAT, CARRY, ISOLATION }
enum class CNSLoad { LOW, MEDIUM, HIGH }

data class MuscleWithTimeRemaining(
    val muscleGroup: MuscleGroup,
    val hoursRemaining: Int
)

fun MuscleGroup.displayName(): String = name.lowercase().replace("_", " ")
    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

fun MuscleGroup.frontPaths(): List<String> = when(this) {
    MuscleGroup.CHEST_UPPER    -> listOf("pec_upper_l", "pec_upper_r")
    MuscleGroup.CHEST_LOWER    -> listOf("pec_lower_l", "pec_lower_r")
    MuscleGroup.FRONT_DELT     -> listOf("ant_delt_l", "ant_delt_r")
    MuscleGroup.SIDE_DELT      -> listOf("lat_delt_l", "lat_delt_r")
    MuscleGroup.REAR_DELT      -> emptyList()
    MuscleGroup.BICEPS         -> listOf("bicep_l", "bicep_r")
    MuscleGroup.TRICEPS        -> emptyList()
    MuscleGroup.FOREARMS       -> listOf("forearm_front_l", "forearm_front_r")
    MuscleGroup.UPPER_BACK     -> emptyList()
    MuscleGroup.LATS           -> emptyList()
    MuscleGroup.LOWER_BACK     -> emptyList()
    MuscleGroup.ABS            -> listOf("abs_upper", "abs_mid", "abs_lower", "oblique_l", "oblique_r")
    MuscleGroup.QUADS          -> listOf("quad_l", "quad_r")
    MuscleGroup.HAMSTRINGS     -> emptyList()
    MuscleGroup.GLUTES         -> emptyList()
    MuscleGroup.CALVES         -> listOf("tibialis_l", "tibialis_r")
}

fun MuscleGroup.backPaths(): List<String> = when(this) {
    MuscleGroup.CHEST_UPPER    -> emptyList()
    MuscleGroup.CHEST_LOWER    -> emptyList()
    MuscleGroup.FRONT_DELT     -> emptyList()
    MuscleGroup.SIDE_DELT      -> emptyList()
    MuscleGroup.REAR_DELT      -> listOf("rear_delt_l", "rear_delt_r")
    MuscleGroup.BICEPS         -> emptyList()
    MuscleGroup.TRICEPS        -> listOf("tricep_l", "tricep_r")
    MuscleGroup.FOREARMS       -> emptyList()
    MuscleGroup.UPPER_BACK     -> listOf("trap_upper_l", "trap_upper_r", "trap_mid")
    MuscleGroup.LATS           -> listOf("lat_l", "lat_r")
    MuscleGroup.LOWER_BACK     -> listOf("lower_back")
    MuscleGroup.ABS            -> emptyList()
    MuscleGroup.QUADS          -> emptyList()
    MuscleGroup.HAMSTRINGS     -> listOf("hamstring_l", "hamstring_r")
    MuscleGroup.GLUTES         -> listOf("glute_l", "glute_r")
    MuscleGroup.CALVES         -> listOf("calf_l", "calf_r")
}
