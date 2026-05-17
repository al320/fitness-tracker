package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val templates: List<WorkoutTemplate> = emptyList()
)

class LibraryViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        repository.templates
            .onEach { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
            .launchIn(viewModelScope)
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun upsertTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            repository.upsertTemplate(template)
        }
    }

    fun insertExercise(exercise: com.al32.fitcheck.data.local.entities.Exercise) {
        viewModelScope.launch {
            repository.insertExercises(listOf(exercise))
        }
    }
}
