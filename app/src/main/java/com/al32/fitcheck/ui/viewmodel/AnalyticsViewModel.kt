package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.SetEntry
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

enum class VolumeRange {
    WEEK, MONTH, ALL
}

data class AnalyticsUiState(
    val volumeHistory: List<Pair<String, Double>> = emptyList(),
    val muscleIntensity: Map<String, Float> = emptyMap(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val sessionHistory: List<WorkoutSessionWithDetails> = emptyList(),
    val selectedRange: VolumeRange = VolumeRange.MONTH,
    val hasEnoughDataForChart: Boolean = false
)

data class WorkoutSessionWithDetails(
    val session: WorkoutSession,
    val exercises: List<ExerciseWithSetsDetail>,
    val totalVolumeKg: Float,
    val durationMs: Long
)

data class ExerciseWithSetsDetail(
    val exerciseName: String,
    val sets: List<SetEntry>
)

class AnalyticsViewModel(
    private val repository: FitcheckRepository
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(VolumeRange.MONTH)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.allSessions,
        repository.getDailyVolume(),
        _selectedRange,
        repository.getCompletedSetsWithPhysiology(0)
    ) { sessions, dailyVolumes, range, sets ->
        
        val filteredVolumes = when (range) {
            VolumeRange.WEEK -> {
                val cutoff = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
                dailyVolumes.filter { 
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.sessionDate)
                    date != null && date.time >= cutoff
                }
            }
            VolumeRange.MONTH -> {
                val cutoff = System.currentTimeMillis() - 30 * 24 * 3600 * 1000L
                dailyVolumes.filter { 
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.sessionDate)
                    date != null && date.time >= cutoff
                }
            }
            VolumeRange.ALL -> dailyVolumes
        }

        val history = filteredVolumes.map { 
            val rawDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.sessionDate)
            val formatted = rawDate?.let { d -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(d) } ?: it.sessionDate
            formatted to it.totalVolume 
        }

        val muscleStates = RecoveryEngine.computeReadinessFromJoined(sets)
        val totalVol = sets.sumOf { (it.weight * it.reps).toDouble() }

        val fullHistory = sessions.filter { it.isCompleted }
            .sortedByDescending { it.startTime }
            .map { session ->
                val entries = repository.getEntriesForSessionSync(session.id)
                val sessionSets = repository.getSetsForSession(session.id).first()
                
                val exercises = entries.map { entry ->
                    val exercise = repository.getExerciseById(entry.exerciseId)
                    val setsForEntry = sessionSets.filter { it.exerciseEntryId == entry.id }
                    ExerciseWithSetsDetail(exercise?.name ?: "Unknown", setsForEntry)
                }
                
                val vol = sessionSets.filter { it.isCompleted }.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                val dur = if (session.endTime != null) session.endTime - session.startTime else 0L
                
                WorkoutSessionWithDetails(session, exercises, vol, dur)
            }

        AnalyticsUiState(
            totalWorkouts = sessions.count { it.isCompleted },
            totalVolume = totalVol,
            muscleIntensity = muscleStates.entries.associate { it.key.name to it.value.score },
            volumeHistory = history,
            sessionHistory = fullHistory,
            selectedRange = range,
            hasEnoughDataForChart = history.size >= 2
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun setRange(range: VolumeRange) {
        _selectedRange.value = range
    }
}
