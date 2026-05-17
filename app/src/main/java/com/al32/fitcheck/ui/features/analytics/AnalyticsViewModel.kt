package com.al32.fitcheck.ui.features.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import kotlinx.coroutines.flow.*

data class AnalyticsUiState(
    val volumeHistory: List<Pair<String, Double>> = emptyList(),
    val muscleIntensity: Map<String, Float> = emptyMap(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0
)

class AnalyticsViewModel(
    private val repository: FitcheckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    val allSessions: Flow<List<WorkoutSession>> = repository.allSessions

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val thirtyDaysAgo = System.currentTimeMillis() - (86400000L * 30)
        
        combine(
            repository.allSessions,
            repository.getCompletedSetsWithPhysiology(thirtyDaysAgo)
        ) { sessions, sets ->
            val muscleStates = RecoveryEngine.computeReadinessFromJoined(sets)
            val totalVol = sets.sumOf { (it.weight * it.reps).toDouble() }
            
            AnalyticsUiState(
                totalWorkouts = sessions.count { it.endTime != null },
                totalVolume = totalVol,
                muscleIntensity = muscleStates.entries.associate { it.key.name to it.value.score },
                volumeHistory = emptyList() 
            )
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
