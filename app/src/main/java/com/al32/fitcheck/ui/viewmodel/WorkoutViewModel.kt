package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.*
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

data class WorkoutUiState(
    val session: WorkoutSession? = null,
    val exercises: List<ExerciseWithSets> = emptyList(),
    val elapsedTime: Long = 0,
    val totalVolume: Float = 0f,
    val completedSetsCount: Int = 0,
    val totalSetsCount: Int = 0,
    val isExercisePickerOpen: Boolean = false,
    val searchResults: List<Exercise> = emptyList(),
    val restTimerSeconds: Int = 0,
    val summary: WorkoutSummary? = null
)

data class WorkoutSummary(
    val durationSeconds: Long,
    val totalVolume: Float,
    val setsCompleted: Int
)

data class ExerciseWithSets(
    val entry: ExerciseEntry,
    val exercise: Exercise,
    val sets: List<SetEntry>,
    val previousPerformance: List<SetEntry> = emptyList()
)

class WorkoutViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val KEY_SESSION_ID = "session_id"
    private val sessionIdFlow = savedStateHandle.getStateFlow<String?>(KEY_SESSION_ID, null)

    private val _isExercisePickerOpen = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _elapsedTime = MutableStateFlow(0L)
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _summary = MutableStateFlow<WorkoutSummary?>(null)

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercisesWithSetsFlow: Flow<List<ExerciseWithSets>> = sessionIdFlow
        .flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(emptyList())
            else {
                repository.getEntriesForSession(sessionId).flatMapLatest { entries ->
                    if (entries.isEmpty()) flowOf(emptyList())
                    else {
                        val flows = entries.map { entry ->
                            combine(
                                flow { emit(repository.getExerciseById(entry.exerciseId)) },
                                repository.getSetsForEntry(entry.id),
                                repository.getPreviousPerformance(entry.exerciseId, sessionId)
                            ) { exercise, sets, prev ->
                                ExerciseWithSets(entry, exercise!!, sets, prev)
                            }
                        }
                        combine(flows) { it.toList() }
                    }
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val searchResultsFlow: Flow<List<Exercise>> = _searchQuery
        .flatMapLatest { query ->
            repository.allExercises.map { list ->
                list.filter { it.name.contains(query, ignoreCase = true) }
            }
        }

    val uiState: StateFlow<WorkoutUiState> = combine(
        sessionIdFlow,
        repository.activeSession,
        exercisesWithSetsFlow,
        combine(_elapsedTime, _isExercisePickerOpen, searchResultsFlow, _restTimerSeconds, _summary) { args ->
            args
        }
    ) { sessionId, activeSession, exercises, other ->
        val elapsed = other[0] as Long
        val pickerOpen = other[1] as Boolean
        val searchResults = other[2] as List<Exercise>
        val restTimer = other[3] as Int
        val summary = other[4] as WorkoutSummary?

        val session = if (sessionId != null && activeSession?.id == sessionId) activeSession else null
        
        val totalVol = exercises.sumOf { e -> 
            e.sets.filter { it.isCompleted }.sumOf { s -> (s.weight * s.reps).toDouble() } 
        }.toFloat()
        val totalSets = exercises.sumOf { it.sets.size }
        val completed = exercises.sumOf { it.sets.count { it.isCompleted } }

        WorkoutUiState(
            session = session,
            exercises = exercises,
            elapsedTime = elapsed,
            totalVolume = totalVol,
            completedSetsCount = completed,
            totalSetsCount = totalSets,
            isExercisePickerOpen = pickerOpen,
            searchResults = searchResults,
            restTimerSeconds = restTimer,
            summary = summary
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    init {
        sessionIdFlow.onEach { id ->
            if (id != null) startTimer()
            else stopTimer()
        }.launchIn(viewModelScope)
    }

    fun startWorkout(name: String) {
        viewModelScope.launch {
            val id = repository.createSession(name)
            savedStateHandle[KEY_SESSION_ID] = id
            _summary.value = null
        }
    }

    fun resumeWorkout(id: String) {
        savedStateHandle[KEY_SESSION_ID] = id
        _summary.value = null
    }

    private fun startTimer() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            val start = System.currentTimeMillis()
            while (isActive) {
                _elapsedTime.value = (System.currentTimeMillis() - start) / 1000
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _elapsedTime.value = 0
    }

    fun finishWorkout() {
        val state = uiState.value
        val session = state.session ?: return
        viewModelScope.launch {
            val entries = state.exercises.map { it.entry }
            val sets = state.exercises.flatMap { it.sets }
            repository.completeWorkout(session, entries, sets)
            stopTimer()
            _summary.value = WorkoutSummary(
                durationSeconds = state.elapsedTime,
                totalVolume = state.totalVolume,
                setsCompleted = state.completedSetsCount
            )
            savedStateHandle.remove<String>(KEY_SESSION_ID)
        }
    }

    fun dismissSummary() {
        _summary.value = null
    }

    fun addExercise(exercise: Exercise) {
        val sessionId = savedStateHandle.get<String>(KEY_SESSION_ID) ?: return
        viewModelScope.launch {
            repository.addExerciseToSession(sessionId, exercise.id, uiState.value.exercises.size)
            toggleExercisePicker(false)
        }
    }

    fun removeExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun addSet(entryId: String) {
        viewModelScope.launch {
            repository.addSetToEntry(entryId)
        }
    }

    fun updateSet(set: SetEntry) {
        viewModelScope.launch {
            repository.updateSet(set)
            if (set.isCompleted) {
                startRestTimer(90) // Default rest timer
            }
        }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            _restTimerSeconds.value = seconds
            while (_restTimerSeconds.value > 0) {
                delay(1000L)
                _restTimerSeconds.value -= 1
            }
            // Vibrate/Sound logic could go here
        }
    }

    fun addRestTime(seconds: Int) {
        _restTimerSeconds.value += seconds
    }

    fun skipRest() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
    }

    fun toggleExercisePicker(open: Boolean) {
        _isExercisePickerOpen.value = open
        if (open) _searchQuery.value = ""
    }

    fun searchExercises(query: String) {
        _searchQuery.value = query
    }

    fun saveAsTemplate(name: String) {
        val exercises = uiState.value.exercises
        viewModelScope.launch {
            val templateId = UUID.randomUUID().toString()
            repository.upsertTemplate(WorkoutTemplate(id = templateId, name = name))
            exercises.forEachIndexed { index, ex ->
                repository.upsertTemplateExercise(
                    TemplateExercise(
                        templateId = templateId,
                        exerciseId = ex.exercise.id,
                        orderIndex = index,
                        targetSets = ex.sets.size,
                        targetReps = ex.sets.firstOrNull()?.reps ?: 10,
                        targetWeight = ex.sets.firstOrNull()?.weight
                    )
                )
            }
            savedStateHandle.remove<String>(KEY_SESSION_ID)
            _restTimerSeconds.value = 0
            _summary.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
