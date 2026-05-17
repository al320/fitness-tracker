package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*

data class ExerciseDetailUiState(
    val exercise: ExerciseEntity? = null,
    val history: List<SetEntity> = emptyList(),
    val personalRecord: SetEntity? = null
)

class ExerciseDetailViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(ExerciseDetailUiState())
    val uiState: StateFlow<ExerciseDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        repository.allExercises.map { list ->
            list.find { it.id == exerciseId }
        }.onEach { exercise ->
            _uiState.update { it.copy(exercise = exercise) }
        }.launchIn(viewModelScope)

        repository.getHistoryForExercise(exerciseId)
            .onEach { history ->
                _uiState.update { it.copy(history = history) }
            }.launchIn(viewModelScope)
            
        // Load real PR
        flow {
            emit(repository.getPersonalRecord(exerciseId))
        }.onEach { pr ->
            _uiState.update { it.copy(personalRecord = pr) }
        }.launchIn(viewModelScope)
    }
}
