package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.PhysiologyProvider
import com.al32.fitcheck.domain.scoring.StrengthScorer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class WorkoutUiState(
    val workoutId: Long? = null,
    val exercises: List<ExerciseWithSets> = emptyList(),
    val elapsedTime: Long = 0,
    val totalVolume: Double = 0.0,
    val completedSets: Int = 0,
    val totalSets: Int = 0,
    val isExercisePickerOpen: Boolean = false,
    val searchResults: List<ExerciseEntity> = emptyList(),
    val recentPr: SetEntity? = null,
    val restTimerSeconds: Int? = null,
    val summary: WorkoutSummary? = null
)

data class WorkoutSummary(
    val totalVolume: Double,
    val totalSets: Int,
    val durationSeconds: Long,
    val prCount: Int
)

data class ExerciseWithSets(
    val exercise: ExerciseEntity,
    val sets: List<SetEntity>,
    val previousPerformance: List<SetEntity> = emptyList()
)

class WorkoutViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var searchJob: Job? = null
    private var restTimerJob: Job? = null

    companion object {
        private const val KEY_WORKOUT_ID = "workout_id"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
        private const val KEY_IS_PICKER_OPEN = "is_picker_open"
    }

    init {
        val restoredId = savedStateHandle.get<Long>(KEY_WORKOUT_ID)
        val restoredTime = savedStateHandle.get<Long>(KEY_ELAPSED_TIME) ?: 0L
        val restoredPicker = savedStateHandle.get<Boolean>(KEY_IS_PICKER_OPEN) ?: false

        _uiState.update { it.copy(
            workoutId = restoredId,
            elapsedTime = restoredTime,
            isExercisePickerOpen = restoredPicker
        ) }

        if (restoredId != null) startTimer(restoredTime)
        initWorkoutObserver()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initWorkoutObserver() {
        _uiState
            .map { it.workoutId }
            .distinctUntilChanged()
            .filterNotNull()
            .flatMapLatest { workoutId ->
                combine(
                    repository.getExercisesForWorkout(workoutId),
                    repository.getSetsForWorkout(workoutId)
                ) { exercises, sets ->
                    exercises.map { exercise ->
                        ExerciseWithSets(
                            exercise = exercise,
                            sets = sets.filter { it.exerciseId == exercise.id },
                            previousPerformance = repository.getPreviousPerformance(exercise.id, workoutId)
                        )
                    }
                }
            }
            .onEach { exercisesWithSets ->
                val totalVolume = exercisesWithSets.sumOf { it.sets.filter { s -> s.isCompleted }.sumOf { set -> set.weight * set.reps } }
                val totalSets = exercisesWithSets.sumOf { it.sets.size }
                val completedSets = exercisesWithSets.sumOf { it.sets.count { s -> s.isCompleted } }
                
                _uiState.update { it.copy(
                    exercises = exercisesWithSets,
                    totalVolume = totalVolume,
                    totalSets = totalSets,
                    completedSets = completedSets
                ) }
            }
            .launchIn(viewModelScope)
    }

    fun startWorkout(name: String, templateId: Long? = null) {
        viewModelScope.launch {
            val id = repository.startWorkout(name)
            savedStateHandle[KEY_WORKOUT_ID] = id
            _uiState.update { it.copy(workoutId = id) }
            startTimer(0)
            
            if (templateId != null) {
                repository.getExercisesForWorkout(templateId).first().forEachIndexed { index, exercise ->
                    repository.addExerciseToWorkout(id, exercise.id, index)
                }
            }
        }
    }

    fun resumeWorkout(workoutId: Long) {
        savedStateHandle[KEY_WORKOUT_ID] = workoutId
        _uiState.update { it.copy(workoutId = workoutId) }
        startTimer(_uiState.value.elapsedTime)
    }

    private fun startTimer(initialTime: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = initialTime
            while (isActive) {
                _uiState.update { it.copy(elapsedTime = time) }
                savedStateHandle[KEY_ELAPSED_TIME] = time
                delay(1000)
                time++
            }
        }
    }

    fun toggleExercisePicker(open: Boolean) {
        savedStateHandle[KEY_IS_PICKER_OPEN] = open
        _uiState.update { it.copy(isExercisePickerOpen = open) }
        if (open) searchExercises("")
    }

    fun searchExercises(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.searchExercises(query).collect { results ->
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun addExercise(exercise: ExerciseEntity) {
        val workoutId = _uiState.value.workoutId ?: return
        viewModelScope.launch {
            repository.addExerciseToWorkout(workoutId, exercise.id, _uiState.value.exercises.size)
            toggleExercisePicker(false)
        }
    }

    fun removeExercise(exerciseId: Long) {
        val workoutId = _uiState.value.workoutId ?: return
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(workoutId, exerciseId)
        }
    }

    fun addSet(exerciseId: Long) {
        val workoutId = _uiState.value.workoutId ?: return
        viewModelScope.launch {
            repository.addSet(
                SetEntity(
                    workoutId = workoutId,
                    exerciseId = exerciseId,
                    weight = 0.0,
                    reps = 0,
                    timestamp = System.currentTimeMillis(),
                    isCompleted = false
                )
            )
        }
    }

    fun updateSet(setId: Long, weight: Double, reps: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val oldSet = _uiState.value.exercises.flatMap { it.sets }.find { it.id == setId }
            repository.updateSet(setId, reps, weight, false, isCompleted)
            
            if (isCompleted && (oldSet?.isCompleted == false)) {
                handleSetCompletion(setId, weight, reps)
            }
        }
    }

    private suspend fun handleSetCompletion(setId: Long, weight: Double, reps: Int) {
        val set = _uiState.value.exercises.flatMap { it.sets }.find { it.id == setId } ?: return
        startRestTimer(90)
        
        val pr = repository.getPersonalRecord(set.exerciseId)
        val currentMax = StrengthScorer.estimateOneRepMax(weight, reps)
        val prevMax = pr?.let { StrengthScorer.estimateOneRepMax(it.weight, it.reps) } ?: 0.0
        
        if (currentMax > prevMax) {
            repository.updateSet(setId, reps, weight, true, true)
        }
    }

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining >= 0) {
                _uiState.update { it.copy(restTimerSeconds = remaining) }
                delay(1000)
                remaining--
            }
            _uiState.update { it.copy(restTimerSeconds = null) }
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(restTimerSeconds = null) }
    }

    fun saveAsTemplate(name: String) {
        val workoutId = _uiState.value.workoutId ?: return
        viewModelScope.launch {
            repository.updateTemplateName(workoutId, name)
            _uiState.update { WorkoutUiState() }
        }
    }

    fun finishWorkout() {
        val state = _uiState.value
        val workoutId = state.workoutId ?: return
        
        val summary = WorkoutSummary(
            totalVolume = state.totalVolume,
            totalSets = state.totalSets,
            durationSeconds = state.elapsedTime,
            prCount = state.exercises.flatMap { it.sets }.count { it.isPr }
        )

        viewModelScope.launch {
            repository.finishWorkout(workoutId, 0) // XP system removed
            timerJob?.cancel()
            savedStateHandle.remove<Long>(KEY_WORKOUT_ID)
            _uiState.update { it.copy(summary = summary, workoutId = null) }
        }
    }

    fun dismissSummary() {
        _uiState.update { WorkoutUiState() }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
