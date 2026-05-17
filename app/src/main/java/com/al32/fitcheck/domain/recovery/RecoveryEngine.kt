package com.al32.fitcheck.domain.recovery

import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.physiology.PhysiologyProvider
import kotlin.math.ln
import kotlin.math.pow

data class Readiness(
    val group: MuscleGroup,
    val score: Float, // 0.0 to 1.0 (1.0 = Fresh)
    val fatigueLevel: Float, // stimulus units
    val recoveryPercentage: Int,
    val hoursUntilRecovered: Int
)

object RecoveryEngine {

    fun computeReadiness(sets: List<SetEntity>): Map<MuscleGroup, Readiness> {
        val now = System.currentTimeMillis()
        val fatigueEvents = sets.filter { it.isCompleted }.flatMap { set ->
            val phys = PhysiologyProvider.getPhysiology(set.exerciseId) ?: return@flatMap emptyList<FatigueEvent>()
            
            // stimulus = ln(1 + weight * reps / 100) * cnsModifier
            val stimulus = ln(1.0 + (set.weight * set.reps) / 100.0).toFloat() * phys.cnsFatigueModifier
            
            val primary = phys.primaryMuscles.map { 
                FatigueEvent(it.muscle, stimulus * it.coefficient, set.timestamp) 
            }
            val secondary = phys.secondaryMuscles.map { 
                FatigueEvent(it.muscle, stimulus * it.coefficient, set.timestamp) 
            }
            
            primary + secondary
        }

        return MuscleGroup.entries.associateWith { group ->
            val groupEvents = fatigueEvents.filter { it.muscle == group }
            
            val currentFatigue = groupEvents.sumOf { event ->
                val hoursPassed = (now - event.timestamp).toFloat() / (1000 * 60 * 60)
                val halfLife = group.recoveryHalfLifeHours.toFloat()
                val remaining = event.stimulus * (0.5).pow((hoursPassed / halfLife).toDouble())
                remaining
            }.toFloat()

            val readiness = (1.0f - (currentFatigue / 2.0f)).coerceIn(0f, 1f)
            val recoveryPct = (readiness * 100).toInt()
            
            val hoursUntilRecovered = if (currentFatigue > 0.1f) {
                (group.recoveryHalfLifeHours * (ln(currentFatigue / 0.1) / ln(2.0))).toInt().coerceAtLeast(0)
            } else 0

            Readiness(group, readiness, currentFatigue, recoveryPct, hoursUntilRecovered)
        }
    }

    private data class FatigueEvent(
        val muscle: MuscleGroup,
        val stimulus: Float,
        val timestamp: Long
    )
}
