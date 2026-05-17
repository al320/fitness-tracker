package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val templates: List<WorkoutEntity> = emptyList()
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

    fun deleteTemplate(template: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(template)
        }
    }

    fun duplicateTemplate(template: WorkoutEntity) {
        viewModelScope.launch {
            repository.duplicateTemplate(template.id)
        }
    }

    fun createTemplate(name: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.startWorkout(name, isTemplate = true)
            onComplete(id)
        }
    }
}
