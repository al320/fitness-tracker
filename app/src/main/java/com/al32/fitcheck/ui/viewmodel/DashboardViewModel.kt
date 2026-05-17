package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.recovery.Readiness
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import com.al32.fitcheck.data.preferences.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.al32.fitcheck.data.local.entities.WeeklyScheduleDay
import java.util.Calendar

sealed class TodayWorkoutState {
    object NotConfigured : TodayWorkoutState()
    object RestDay : TodayWorkoutState()
    data class WorkoutReady(val template: WorkoutTemplate) : TodayWorkoutState()
}

data class DashboardUiState(
    val userProfile: UserProfile = UserProfile(),
    val activeWorkout: WorkoutSession? = null,
    val templates: List<WorkoutTemplate> = emptyList(),
    val muscleStates: Map<MuscleGroup, Readiness> = emptyMap(),
    val recommendations: List<String> = emptyList(),
    val totalVolume: Float = 0f,
    val sessionCount: Int = 0,
    val weeklySetTargets: Map<MuscleGroup, Int> = MuscleGroup.entries.associateWith { 10 },
    val weeklySetCurrent: Map<MuscleGroup, Int> = emptyMap(),
    val todayWorkout: TodayWorkoutState = TodayWorkoutState.NotConfigured
)

class DashboardViewModel(
    val repository: FitcheckRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun setScheduleDay(dayOfWeek: Int, templateId: String?) {
        viewModelScope.launch {
            repository.upsertScheduleDay(
                WeeklyScheduleDay(dayOfWeek = dayOfWeek, templateId = templateId, isRestDay = templateId == null)
            )
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getCompletedSetsWithPhysiology(0),
        repository.activeSession,
        repository.templates,
        repository.allSessions,
        repository.weeklySchedule
    ) { sets, active, templates, all, schedule ->
        val startOfWeek = getStartOfWeek()
        val muscleStates = RecoveryEngine.computeReadinessFromJoined(sets)
        val recs = generateRecommendations(muscleStates)
        
        val weeklySets = sets.filter { it.completedAt >= startOfWeek }
            .flatMap { s -> s.primaryMuscles }
            .groupingBy { it }.eachCount()

        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        
        val todayAssignment = schedule.find { it.dayOfWeek == dayOfWeek }
        val todayState = when {
            todayAssignment == null -> TodayWorkoutState.NotConfigured
            todayAssignment.isRestDay -> TodayWorkoutState.RestDay
            else -> {
                val template = templates.find { it.id == todayAssignment.templateId }
                if (template != null) TodayWorkoutState.WorkoutReady(template)
                else TodayWorkoutState.NotConfigured
            }
        }

        DashboardUiState(
            activeWorkout = active,
            templates = templates,
            muscleStates = muscleStates,
            recommendations = recs,
            weeklySetCurrent = weeklySets,
            sessionCount = all.count { s -> s.endTime != null },
            todayWorkout = todayState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    private fun generateRecommendations(muscleStates: Map<MuscleGroup, Readiness>): List<String> {
        val fullyRecovered = muscleStates.filter { it.value.recoveryPercentage >= 95 }.keys
        val recs = mutableListOf<String>()
        if (fullyRecovered.isNotEmpty()) recs.add("OPTIMAL READINESS: ${fullyRecovered.take(3).joinToString { it.name.replace("_", " ") }}")
        else recs.add("RECOVERY IN PROGRESS")
        return recs
    }
}
