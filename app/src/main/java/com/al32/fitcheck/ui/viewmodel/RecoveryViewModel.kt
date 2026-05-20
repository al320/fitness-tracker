package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.physiology.MuscleWithTimeRemaining
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import kotlinx.coroutines.flow.*

class RecoveryViewModel(private val repository: FitcheckRepository) : ViewModel() {

    private val RECOVERY_THRESHOLD = 0.85f

    val readinessStates: StateFlow<Map<MuscleGroup, com.al32.fitcheck.domain.recovery.Readiness>> = repository.getCompletedSetsWithPhysiology(
        since = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
    ).map { sets ->
        RecoveryEngine.computeReadinessFromJoined(sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val readyMuscles: StateFlow<List<MuscleGroup>> = readinessStates.map { states ->
        states.filter { it.value.score >= RECOVERY_THRESHOLD }.keys.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveringMuscles: StateFlow<List<MuscleWithTimeRemaining>> = readinessStates.map { states ->
        states.filter { it.value.score < RECOVERY_THRESHOLD }.map { (group, readiness) ->
            MuscleWithTimeRemaining(group, readiness.hoursUntilRecovered)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
