package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.PRType
import com.al32.fitcheck.data.local.entities.SetEntry
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlinx.coroutines.flow.*

data class SessionSummaryUiState(
    val durationMs: Long = 0,
    val totalVolumeKg: Float = 0f,
    val setsCompleted: Int = 0,
    val volumeChangePercent: Float? = null,
    val templateName: String = "Session",
    val newPRs: List<SessionPR> = emptyList(),
    val musclesWorked: List<MuscleGroup> = emptyList()
)

data class SessionPR(
    val exerciseName: String,
    val type: PRType,
    val value: Float
)

class SessionSummaryViewModel(
    private val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    val summary: StateFlow<SessionSummaryUiState> = flow {
        val session = repository.allSessions.first().find { it.id == sessionId } ?: return@flow
        val entries = repository.getEntriesForSessionSync(sessionId)
        val sets: List<SetEntry> = repository.getSetsForSession(sessionId).first().filter { it.isCompleted }
        
        val duration = if (session.endTime != null) session.endTime - session.startTime else 0L
        val volume = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
        
        val prList = repository.getPRsForSession(sessionId).first()
        val prs = prList.map { pr ->
            val exercise = repository.getExerciseById(pr.exerciseId)
            SessionPR(exercise?.name ?: "Unknown", pr.type, pr.value)
        }

        val muscles = entries.flatMap { entry ->
            val exercise = repository.getExerciseById(entry.exerciseId)
            (exercise?.primaryMuscles ?: emptyList()) + (exercise?.secondaryMuscles ?: emptyList())
        }.distinct()

        emit(SessionSummaryUiState(
            durationMs = duration,
            totalVolumeKg = volume,
            setsCompleted = sets.size,
            templateName = session.name,
            newPRs = prs,
            musclesWorked = muscles
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionSummaryUiState())
}
