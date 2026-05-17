package com.al32.fitcheck.domain.recovery

import com.al32.fitcheck.data.local.dao.SetWithPhysiology
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlin.math.ln
import kotlin.math.pow

data class Readiness(
    val group: MuscleGroup,
    val score: Float,
    val fatigueLevel: Float,
    val recoveryPercentage: Int,
    val hoursUntilRecovered: Int
)

object RecoveryEngine {

    fun computeReadinessFromJoined(sets: List<SetWithPhysiology>): Map<MuscleGroup, Readiness> {
        val now = System.currentTimeMillis()
        val fatigueEvents = sets.flatMap { set ->
            val stimulus = ln(1.0 + (set.weight * set.reps) / 100.0).toFloat()
            val primary = set.primaryMuscles.map { FatigueEvent(it, stimulus * 1.0f, set.completedAt) }
            val secondary = set.secondaryMuscles.map { FatigueEvent(it, stimulus * 0.35f, set.completedAt) }
            primary + secondary
        }

        return MuscleGroup.entries.associateWith { group ->
            val groupEvents = fatigueEvents.filter { it.muscle == group }
            val currentFatigue = groupEvents.sumOf { event ->
                val hoursPassed = (now - event.timestamp).toDouble() / 3_600_000.0
                val halfLife = if (group == MuscleGroup.LOWER_BACK) 72.0 else 48.0
                event.stimulus * (0.5).pow(hoursPassed / halfLife)
            }.toFloat()

            val readiness = (1.0f - (currentFatigue / 2.0f)).coerceIn(0f, 1f)
            val hours = if (currentFatigue > 0.1f) {
                (48 * (ln(currentFatigue / 0.1) / ln(2.0))).toInt().coerceAtLeast(0)
            } else 0

            Readiness(group, readiness, currentFatigue, (readiness * 100).toInt(), hours)
        }
    }

    private data class FatigueEvent(val muscle: MuscleGroup, val stimulus: Float, val timestamp: Long)
}
