package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.recovery.Readiness
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val userStats: UserStatsEntity = UserStatsEntity(),
    val activeWorkout: WorkoutEntity? = null,
    val templates: List<WorkoutEntity> = emptyList(),
    val muscleStates: Map<MuscleGroup, Readiness> = emptyMap(),
    val recommendations: List<String> = emptyList()
)

class DashboardViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Observe all sets to calculate fatigue
        repository.getAllCompletedSets()
            .onEach { sets ->
                val muscleStates = RecoveryEngine.computeReadiness(sets)
                val recs = generateRecommendations(muscleStates)
                _uiState.update { it.copy(muscleStates = muscleStates, recommendations = recs) }
            }.launchIn(viewModelScope)

        combine(
            repository.userStats,
            repository.activeWorkout,
            repository.templates
        ) { stats, active, templates ->
            _uiState.update { it.copy(
                userStats = stats ?: UserStatsEntity(),
                activeWorkout = active,
                templates = templates
            ) }
        }.launchIn(viewModelScope)
    }

    private fun generateRecommendations(muscleStates: Map<MuscleGroup, Readiness>): List<String> {
        val fullyRecovered = muscleStates.filter { it.value.recoveryPercentage >= 95 }.keys
        val heavilyFatigued = muscleStates.filter { it.value.recoveryPercentage < 30 }.keys
        
        val recs = mutableListOf<String>()
        if (heavilyFatigued.isNotEmpty()) {
            recs.add("RECOVERY REQUIRED: ${heavilyFatigued.take(3).joinToString { it.displayName.uppercase() }}")
        }
        if (fullyRecovered.isNotEmpty()) {
            recs.add("OPTIMAL READINESS: ${fullyRecovered.take(3).joinToString { it.displayName.uppercase() }}")
        }
        return recs
    }
}
