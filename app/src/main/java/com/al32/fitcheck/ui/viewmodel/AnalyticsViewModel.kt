package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.SetEntry
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class AnalyticsUiState(
    val volumeHistory: List<Pair<String, Double>> = emptyList(),
    val muscleIntensity: Map<String, Float> = emptyMap(),
    val totalWorkouts: Int = 0,
    val totalVolume: Double = 0.0,
    val sessionHistory: List<WorkoutSessionWithDetails> = emptyList()
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

    private val thirtyDaysAgo = System.currentTimeMillis() - (86400000L * 30)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.allSessions,
        repository.getCompletedSetsWithPhysiology(thirtyDaysAgo)
    ) { sessions, sets ->
        val muscleStates = RecoveryEngine.computeReadinessFromJoined(sets)
        val totalVol = sets.sumOf { (it.weight * it.reps).toDouble() }
        
        val history = sessions.filter { it.endTime != null }
            .sortedBy { it.startTime }
            .takeLast(10)
            .map { session ->
                val sessionSets = sets.filter { it.completedAt >= session.startTime && it.completedAt <= (session.endTime ?: Long.MAX_VALUE) }
                val sessionVolume = sessionSets.sumOf { (it.weight * it.reps).toDouble() }
                val date = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(session.startTime))
                date to sessionVolume
            }

        // Detailed session history
        val fullHistory = sessions.filter { it.isCompleted }
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
            totalWorkouts = sessions.count { it.endTime != null },
            totalVolume = totalVol,
            muscleIntensity = muscleStates.entries.associate { it.key.name to it.value.score },
            volumeHistory = history,
            sessionHistory = fullHistory
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )
}
