package com.al32.fitcheck.domain.scoring

import com.al32.fitcheck.domain.physiology.MovementPattern

enum class StrengthLevel {
    BEGINNER, NOVICE, INTERMEDIATE, ADVANCED, ELITE, WORLD_CLASS;

    fun color(): androidx.compose.ui.graphics.Color {
        return when (this) {
            BEGINNER -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
            NOVICE -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            INTERMEDIATE -> androidx.compose.ui.graphics.Color(0xFF2196F3)
            ADVANCED -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
            ELITE -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            WORLD_CLASS -> androidx.compose.ui.graphics.Color(0xFFFFD700)
        }
    }

    fun next(): StrengthLevel? {
        val nextIndex = this.ordinal + 1
        return if (nextIndex < entries.size) entries[nextIndex] else null
    }
}

data class StrengthStandard(
    val beginner: Float,
    val novice: Float,
    val intermediate: Float,
    val advanced: Float,
    val elite: Float,
    val worldClass: Float
) {
    fun getRatio(level: StrengthLevel): Float = when (level) {
        StrengthLevel.BEGINNER -> beginner
        StrengthLevel.NOVICE -> novice
        StrengthLevel.INTERMEDIATE -> intermediate
        StrengthLevel.ADVANCED -> advanced
        StrengthLevel.ELITE -> elite
        StrengthLevel.WORLD_CLASS -> worldClass
    }
}

object StrengthScorer {

    // EXACT multipliers from request
    private val squatStandard = StrengthStandard(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f)
    private val benchStandard = StrengthStandard(0.35f, 0.65f, 1.0f, 1.35f, 1.65f, 2.0f)
    private val deadliftStandard = StrengthStandard(0.65f, 1.25f, 1.75f, 2.25f, 2.75f, 3.25f)
    private val ohpStandard = StrengthStandard(0.25f, 0.45f, 0.65f, 0.85f, 1.1f, 1.3f)

    val strengthStandards: Map<String, StrengthStandard> = mapOf(
        "barbell_bench_press" to benchStandard,
        "barbell_squat" to squatStandard,
        "deadlift" to deadliftStandard,
        "overhead_press" to ohpStandard,
        "barbell_row" to StrengthStandard(0.5f, 0.7f, 0.9f, 1.1f, 1.3f, 1.5f),
        "lat_pulldown" to StrengthStandard(0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f),
        "pullup" to StrengthStandard(0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.1f),
        "dips" to StrengthStandard(0.1f, 0.3f, 0.5f, 0.8f, 1.1f, 1.3f),
        "romanian_deadlift" to StrengthStandard(0.5f, 0.8f, 1.1f, 1.4f, 1.7f, 2.1f),
        "leg_press" to StrengthStandard(1.0f, 1.8f, 2.5f, 3.2f, 3.9f, 4.5f),
    )

    private val patternStandards: Map<MovementPattern, StrengthStandard> = mapOf(
        MovementPattern.PUSH to benchStandard,
        MovementPattern.PULL to StrengthStandard(0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f),
        MovementPattern.SQUAT to squatStandard,
        MovementPattern.HINGE to deadliftStandard,
        MovementPattern.ISOLATION to StrengthStandard(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f)
    )

    fun getStandard(exerciseId: String, pattern: MovementPattern): StrengthStandard {
        return strengthStandards[exerciseId] ?: patternStandards[pattern] ?: StrengthStandard(0f, 0f, 0f, 0f, 0f, 0f)
    }

    fun estimateOneRepMax(weight: Float, reps: Int): Float {
        return try {
            if (reps <= 0) return 0f
            if (reps == 1) return weight
            weight * (1f + reps / 30.0f)
        } catch (e: Exception) {
            0f
        }
    }

    fun computeLevel(oneRepMax: Float, bodyWeight: Float, standard: StrengthStandard): StrengthLevel {
        if (bodyWeight <= 0f) return StrengthLevel.BEGINNER
        val ratio = oneRepMax / bodyWeight
        
        return StrengthLevel.entries.reversed().find { level ->
            ratio >= standard.getRatio(level)
        } ?: StrengthLevel.BEGINNER
    }
}
