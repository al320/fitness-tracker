package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.physiology.MuscleWithTimeRemaining
import kotlinx.coroutines.flow.*
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

class RecoveryViewModel(private val repository: FitcheckRepository) : ViewModel() {

    private val RECOVERY_THRESHOLD = 0.85f
    private val MAX_FATIGUE_THRESHOLD = 5000.0f

    val muscleStates: StateFlow<Map<MuscleGroup, Float>> = repository.getCompletedSetsWithPhysiology(
        since = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
    ).map { sets ->
        computeMuscleStates(sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val readyMuscles: StateFlow<List<MuscleGroup>> = muscleStates.map { states ->
        states.filter { it.value >= RECOVERY_THRESHOLD }.keys.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveringMuscles: StateFlow<List<MuscleWithTimeRemaining>> = muscleStates.map { states ->
        states.filter { it.value < RECOVERY_THRESHOLD }.map { (group, state) ->
            val halfLife = getHalfLife(group)
            val currentFatigue = (1.0f - state) * MAX_FATIGUE_THRESHOLD
            val targetFatigue = (1.0f - RECOVERY_THRESHOLD) * MAX_FATIGUE_THRESHOLD
            val lambda = ln(2.0) / halfLife
            val hoursLeft = if (currentFatigue > targetFatigue) {
                (ln(currentFatigue.toDouble() / targetFatigue) / lambda).toInt()
            } else 0
            MuscleWithTimeRemaining(group, hoursLeft)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun computeMuscleStates(sets: List<com.al32.fitcheck.data.local.dao.SetWithPhysiology>): Map<MuscleGroup, Float> {
        val now = System.currentTimeMillis()
        val currentFatigue = mutableMapOf<MuscleGroup, Float>()

        sets.forEach { set ->
            val hoursSince = (now - set.completedAt).toFloat() / (1000 * 60 * 60)
            
            set.primaryMuscles.forEach { muscle ->
                val lambda = ln(2.0) / getHalfLife(muscle)
                val contribution = (set.weight * set.reps * 1.0f) * exp(-lambda * hoursSince).toFloat()
                currentFatigue[muscle] = (currentFatigue[muscle] ?: 0f) + contribution
            }
            set.secondaryMuscles.forEach { muscle ->
                val lambda = ln(2.0) / getHalfLife(muscle)
                val contribution = (set.weight * set.reps * 0.35f) * exp(-lambda * hoursSince).toFloat()
                currentFatigue[muscle] = (currentFatigue[muscle] ?: 0f) + contribution
            }
        }
        
        return MuscleGroup.entries.associateWith { muscle ->
            val fatigue = currentFatigue[muscle] ?: 0f
            (1.0f - (fatigue / MAX_FATIGUE_THRESHOLD)).coerceIn(0f, 1f)
        }
    }

    private fun getHalfLife(group: MuscleGroup): Double {
        return when(group) {
            MuscleGroup.LOWER_BACK, MuscleGroup.GLUTES, MuscleGroup.QUADS, MuscleGroup.HAMSTRINGS -> 72.0
            else -> 48.0
        }
    }
}
