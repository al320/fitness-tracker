package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*

data class DashboardUiState(
    val userStats: UserStatsEntity = UserStatsEntity(),
    val activeWorkout: WorkoutEntity? = null,
    val templates: List<WorkoutEntity> = emptyList()
)

class DashboardViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.userStats,
            repository.activeWorkout,
            repository.templates
        ) { stats, active, templates ->
            DashboardUiState(
                userStats = stats ?: UserStatsEntity(),
                activeWorkout = active,
                templates = templates
            )
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }
}
