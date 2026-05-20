package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.TemplateExercise
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class TemplateUiState(
    val templates: List<WorkoutTemplate> = emptyList(),
    val selectedTemplate: WorkoutTemplate? = null,
    val exercisesForSelected: List<TemplateExercise> = emptyList()
)

class TemplateViewModel(
    private val repository: FitcheckRepository
) : ViewModel() {

    private val _selectedTemplate = MutableStateFlow<WorkoutTemplate?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TemplateUiState> = combine(
        repository.templates,
        _selectedTemplate.flatMapLatest { template ->
            if (template == null) flowOf(emptyList<TemplateExercise>())
            else repository.getExercisesForTemplate(template.id)
        },
        _selectedTemplate
    ) { allTemplates, exercises, selected ->
        TemplateUiState(
            templates = allTemplates,
            selectedTemplate = selected,
            exercisesForSelected = exercises
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TemplateUiState()
    )

    fun createTemplate(name: String, description: String) {
        viewModelScope.launch {
            val template = WorkoutTemplate(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            repository.upsertTemplate(template)
        }
    }

    fun deleteTemplate(template: WorkoutTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
            if (_selectedTemplate.value?.id == template.id) {
                _selectedTemplate.value = null
            }
        }
    }

    fun selectTemplate(template: WorkoutTemplate) {
        _selectedTemplate.value = template
    }

    fun addExerciseToTemplate(
        templateId: String,
        exerciseId: String,
        targetSets: Int,
        targetReps: Int,
        targetWeight: Float?
    ) {
        viewModelScope.launch {
            val currentExercises = uiState.value.exercisesForSelected
            val nextIndex = currentExercises.size
            
            repository.upsertTemplateExercise(
                TemplateExercise(
                    templateId = templateId,
                    exerciseId = exerciseId,
                    orderIndex = nextIndex,
                    targetSets = targetSets,
                    targetReps = targetReps,
                    targetWeight = targetWeight
                )
            )
        }
    }

    fun renameTemplate(template: WorkoutTemplate, newName: String) {
        viewModelScope.launch {
            repository.upsertTemplate(template.copy(name = newName))
        }
    }
}
