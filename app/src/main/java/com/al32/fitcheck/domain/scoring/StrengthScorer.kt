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
            WORLD_CLASS -> androidx.compose.ui.graphics.Color(0xFFF44336)
        }
    }

    fun next(): StrengthLevel {
        val nextIndex = (this.ordinal + 1).coerceAtMost(entries.size - 1)
        return entries[nextIndex]
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

    val strengthStandards: Map<String, StrengthStandard> = mapOf(
        "barbell_bench_press" to StrengthStandard(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f),
        "barbell_squat" to StrengthStandard(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.1f),
        "deadlift" to StrengthStandard(1.0f, 1.25f, 1.5f, 1.75f, 2.1f, 2.5f),
        "overhead_press" to StrengthStandard(0.35f, 0.5f, 0.65f, 0.8f, 1.0f, 1.2f),
        "barbell_row" to StrengthStandard(0.5f, 0.65f, 0.8f, 1.0f, 1.2f, 1.4f),
        "lat_pulldown" to StrengthStandard(0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f),
        "pullup" to StrengthStandard(0.0f, 0.1f, 0.3f, 0.5f, 0.7f, 1.0f),
        "dips" to StrengthStandard(0.0f, 0.1f, 0.3f, 0.5f, 0.75f, 1.0f),
        "romanian_deadlift" to StrengthStandard(0.5f, 0.75f, 1.0f, 1.3f, 1.6f, 2.0f),
        "leg_press" to StrengthStandard(1.0f, 1.5f, 2.0f, 2.5f, 3.0f, 3.5f),
    )

    private val patternStandards: Map<MovementPattern, StrengthStandard> = mapOf(
        MovementPattern.PUSH to StrengthStandard(0.5f, 0.7f, 0.9f, 1.1f, 1.3f, 1.5f),
        MovementPattern.PULL to StrengthStandard(0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f),
        MovementPattern.SQUAT to StrengthStandard(0.6f, 0.8f, 1.1f, 1.4f, 1.7f, 2.0f),
        MovementPattern.HINGE to StrengthStandard(0.8f, 1.1f, 1.4f, 1.7f, 2.0f, 2.4f),
        MovementPattern.ISOLATION to StrengthStandard(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f)
    )

    fun getStandard(exerciseId: String, pattern: MovementPattern): StrengthStandard {
        return strengthStandards[exerciseId] ?: patternStandards[pattern] ?: StrengthStandard(0f, 0f, 0f, 0f, 0f, 0f)
    }

    fun estimateOneRepMax(weight: Float, reps: Int): Float {
        if (reps <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1f + reps / 30.0f)
    }

    fun computeLevel(oneRepMax: Float, bodyWeight: Float, standard: StrengthStandard): StrengthLevel {
        if (bodyWeight <= 0f) return StrengthLevel.BEGINNER
        val ratio = oneRepMax / bodyWeight
        
        return StrengthLevel.entries.reversed().find { level ->
            ratio >= standard.getRatio(level)
        } ?: StrengthLevel.BEGINNER
    }
}
