package com.al32.fitcheck.domain.recovery

import com.al32.fitcheck.data.local.dao.SetWithPhysiology
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlin.math.exp
import kotlin.math.ln

data class Readiness(
    val group: MuscleGroup,
    val score: Float,
    val fatigueLevel: Float,
    val recoveryPercentage: Int,
    val hoursUntilRecovered: Int
)

object RecoveryEngine {
    private const val MAX_FATIGUE_THRESHOLD = 20.0f
    private const val RECOVERY_THRESHOLD = 0.85f

    fun computeReadinessFromJoined(sets: List<SetWithPhysiology>): Map<MuscleGroup, Readiness> {
        val now = System.currentTimeMillis()
        val currentFatigueMap = mutableMapOf<MuscleGroup, Float>()

        sets.forEach { set ->
            val hoursSince = (now - set.completedAt).toDouble() / 3_600_000.0
            val intensityFactor = (set.weight / 100f).coerceIn(0.5f, 3.0f)
            
            val primary = if (set.primaryMuscles.isNotEmpty()) set.primaryMuscles else inferMuscles(set.movementPattern, true)
            val secondary = if (set.secondaryMuscles.isNotEmpty()) set.secondaryMuscles else inferMuscles(set.movementPattern, false)
            
            primary.forEach { muscle ->
                val lambda = ln(2.0) / getHalfLife(muscle)
                val contribution = intensityFactor * exp(-lambda * hoursSince).toFloat()
                currentFatigueMap[muscle] = (currentFatigueMap[muscle] ?: 0f) + contribution
            }
            secondary.forEach { muscle ->
                val lambda = ln(2.0) / getHalfLife(muscle)
                val contribution = (intensityFactor * 0.35f) * exp(-lambda * hoursSince).toFloat()
                currentFatigueMap[muscle] = (currentFatigueMap[muscle] ?: 0f) + contribution
            }
        }

        return MuscleGroup.entries.associateWith { group ->
            val fatigue = currentFatigueMap[group] ?: 0f
            val score = (1.0f - (fatigue / MAX_FATIGUE_THRESHOLD)).coerceIn(0f, 1f)
            
            val halfLife = getHalfLife(group)
            val lambda = ln(2.0) / halfLife
            val targetFatigue = (1.0f - RECOVERY_THRESHOLD) * MAX_FATIGUE_THRESHOLD
            
            val hours = if (fatigue > targetFatigue) {
                (ln(fatigue.toDouble() / targetFatigue) / lambda).toInt().coerceIn(0, 96)
            } else 0

            Readiness(group, score, fatigue, (score * 100).toInt(), hours)
        }
    }

    private fun inferMuscles(pattern: MovementPattern, isPrimary: Boolean): List<MuscleGroup> {
        return when (pattern) {
            MovementPattern.SQUAT -> if (isPrimary) listOf(MuscleGroup.QUADS, MuscleGroup.GLUTES) else emptyList()
            MovementPattern.HINGE -> if (isPrimary) listOf(MuscleGroup.HAMSTRINGS, MuscleGroup.LOWER_BACK) else listOf(MuscleGroup.GLUTES)
            MovementPattern.PUSH -> if (isPrimary) listOf(MuscleGroup.CHEST_UPPER, MuscleGroup.CHEST_LOWER) else listOf(MuscleGroup.FRONT_DELT, MuscleGroup.TRICEPS)
            MovementPattern.PULL -> if (isPrimary) listOf(MuscleGroup.LATS, MuscleGroup.UPPER_BACK) else listOf(MuscleGroup.BICEPS, MuscleGroup.REAR_DELT)
            MovementPattern.ISOLATION -> emptyList()
            MovementPattern.CARRY -> if (isPrimary) listOf(MuscleGroup.FOREARMS, MuscleGroup.UPPER_BACK) else emptyList()
        }
    }

    private fun getHalfLife(group: MuscleGroup): Double = when (group) {
        MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES -> 60.0
        MuscleGroup.LOWER_BACK -> 72.0
        MuscleGroup.UPPER_BACK, MuscleGroup.LATS, MuscleGroup.CHEST_UPPER, MuscleGroup.CHEST_LOWER -> 48.0
        else -> 36.0
    }
}
