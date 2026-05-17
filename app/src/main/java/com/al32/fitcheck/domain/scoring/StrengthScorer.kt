package com.al32.fitcheck.domain.scoring

import kotlin.math.roundToInt

enum class PerformanceRank(val displayName: String) {
    BEGINNER("Beginner"),
    NOVICE("Novice"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    ELITE("Elite"),
    WORLD_CLASS("World Class")
}

object StrengthScorer {

    fun estimateOneRepMax(weight: Double, reps: Int): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        // Brzycki Formula
        return weight / (1.0278 - 0.0278 * reps)
    }

    fun computeRank(oneRepMax: Double, bodyWeight: Double, exerciseCategory: String): PerformanceRank {
        if (bodyWeight <= 0) return PerformanceRank.BEGINNER
        
        val multiplier = oneRepMax / bodyWeight
        
        return when (exerciseCategory.uppercase()) {
            "PUSH" -> when {
                multiplier >= 2.0 -> PerformanceRank.WORLD_CLASS
                multiplier >= 1.5 -> PerformanceRank.ELITE
                multiplier >= 1.25 -> PerformanceRank.ADVANCED
                multiplier >= 1.0 -> PerformanceRank.INTERMEDIATE
                multiplier >= 0.75 -> PerformanceRank.NOVICE
                else -> PerformanceRank.BEGINNER
            }
            "SQUAT", "HINGE" -> when {
                multiplier >= 3.0 -> PerformanceRank.WORLD_CLASS
                multiplier >= 2.5 -> PerformanceRank.ELITE
                multiplier >= 2.0 -> PerformanceRank.ADVANCED
                multiplier >= 1.5 -> PerformanceRank.INTERMEDIATE
                multiplier >= 1.0 -> PerformanceRank.NOVICE
                else -> PerformanceRank.BEGINNER
            }
            else -> when { // Pull / Isolation
                multiplier >= 1.5 -> PerformanceRank.WORLD_CLASS
                multiplier >= 1.2 -> PerformanceRank.ELITE
                multiplier >= 1.0 -> PerformanceRank.ADVANCED
                multiplier >= 0.8 -> PerformanceRank.INTERMEDIATE
                multiplier >= 0.5 -> PerformanceRank.NOVICE
                else -> PerformanceRank.BEGINNER
            }
        }
    }
}
