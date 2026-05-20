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
    val isTimerRunning: Boolean = false,
    val isFinishing: Boolean = false,
    val weightError: String? = null,
    val repsError: String? = null
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
    private val _isTimerRunning = MutableStateFlow(false)
    private val _isFinishing = MutableStateFlow(false)
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _weightError = MutableStateFlow<String?>(null)
    private val _repsError = MutableStateFlow<String?>(null)

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
        combine(_elapsedTime, _isTimerRunning, _isExercisePickerOpen, searchResultsFlow, _restTimerSeconds, _isFinishing, _weightError, _repsError) { args ->
            args
        }
    ) { sessionId, activeSession, exercises, other ->
        val elapsed = other[0] as Long
        val timerRunning = other[1] as Boolean
        val pickerOpen = other[2] as Boolean
        @Suppress("UNCHECKED_CAST")
        val searchResults = other[3] as List<Exercise>
        val restTimer = other[4] as Int
        val finishing = other[5] as Boolean
        val wErr = other[6] as String?
        val rErr = other[7] as String?

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
            isTimerRunning = timerRunning,
            isFinishing = finishing,
            weightError = wErr,
            repsError = rErr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    fun startWorkout(name: String, templateId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.createSession(name)
            if (templateId != null) {
                repository.startWorkoutFromTemplate(templateId, id)
            }
            withContext(Dispatchers.Main) {
                savedStateHandle[KEY_SESSION_ID] = id
            }
        }
    }

    fun resumeWorkout(id: String) {
        savedStateHandle[KEY_SESSION_ID] = id
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob != null) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis() - (_elapsedTime.value * 1000)
            while (isActive) {
                _elapsedTime.value = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isTimerRunning.value = false
    }

    fun finishWorkout(onComplete: (String) -> Unit) {
        val sessionId = savedStateHandle.get<String>(KEY_SESSION_ID) ?: return
        if (_isFinishing.value) return
        _isFinishing.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val session = repository.getSessionById(sessionId) ?: run {
                    withContext(Dispatchers.Main) { _isFinishing.value = false }
                    return@launch
                }
                val entries = repository.getEntriesForSessionSync(sessionId)
                val sets = entries.flatMap { repository.getSetsForEntrySync(it.id) }
                
                repository.completeWorkout(session, entries, sets)
                
                withContext(Dispatchers.Main) {
                    stopTimer()
                    savedStateHandle.remove<String>(KEY_SESSION_ID)
                    _isFinishing.value = false
                    onComplete(sessionId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { _isFinishing.value = false }
            }
        }
    }

    fun addExercise(exercise: Exercise) {
        val sessionId = savedStateHandle.get<String>(KEY_SESSION_ID) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExerciseToSession(sessionId, exercise.id, uiState.value.exercises.size)
            withContext(Dispatchers.Main) {
                toggleExercisePicker(false)
            }
        }
    }

    fun removeExercise(entry: ExerciseEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntry(entry)
        }
    }

    fun addSet(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSetToEntry(entryId)
        }
    }

    fun updateSet(set: SetEntry) {
        if (set.weight < 0 || set.reps < 0) {
            _weightError.value = "Invalid input"
            return
        }
        _weightError.value = null
        
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSet(set)
            if (set.isCompleted && set.weight > 0 && set.reps > 0) {
                withContext(Dispatchers.Main) {
                    startRestTimer(90)
                }
            }
        }
    }

    private fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            _restTimerSeconds.value = seconds
            while (_restTimerSeconds.value > 0) {
                delay(1000L)
                _restTimerSeconds.value -= 1
            }
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
        viewModelScope.launch(Dispatchers.IO) {
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
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restTimerJob?.cancel()
    }
}
