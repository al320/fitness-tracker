package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*

data class AnalyticsUiState(
    val volumeHistory: List<Pair<String, Double>> = emptyList(),
    val muscleIntensity: Map<String, Float> = emptyMap(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val xpHistory: List<Pair<Long, Int>> = emptyList()
)

class AnalyticsViewModel(
    private val repository: FitcheckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val thirtyDaysAgo = System.currentTimeMillis() - (86400000L * 30)
        
        combine(
            repository.allWorkouts,
            repository.userStats,
            repository.totalVolume,
            repository.dailyVolumeHistory,
            repository.getMuscleIntensity(thirtyDaysAgo)
        ) { workouts, stats, totalVol, volHistory, muscleIntensity ->
            
            val maxIntensity = muscleIntensity.maxOfOrNull { it.intensity } ?: 1.0
            val normalizedMuscleMap = muscleIntensity.associate { 
                it.muscleGroup to (it.intensity / maxIntensity).toFloat() 
            }

            AnalyticsUiState(
                totalWorkouts = stats?.totalWorkouts ?: 0,
                totalVolume = totalVol ?: 0.0,
                muscleIntensity = normalizedMuscleMap,
                volumeHistory = volHistory.map { it.day to it.volume }.reversed()
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
