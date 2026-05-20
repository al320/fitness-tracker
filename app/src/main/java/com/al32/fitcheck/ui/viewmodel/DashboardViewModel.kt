package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WorkoutSession
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.local.dao.SetWithPhysiology
import com.al32.fitcheck.data.local.entities.WeeklyScheduleDay
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.recovery.Readiness
import com.al32.fitcheck.domain.recovery.RecoveryEngine
import com.al32.fitcheck.data.preferences.UserProfile
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val streak: Int = 0,
    val weeklySetTargets: Map<MuscleGroup, Int> = MuscleGroup.entries.associateWith { 10 },
    val weeklySetCurrent: Map<MuscleGroup, Int> = emptyMap(),
    val todayWorkout: TodayWorkoutState = TodayWorkoutState.NotConfigured
)

class DashboardViewModel(
    val repository: FitcheckRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userProfileFlow = preferencesRepository.userProfileFlow

    fun setScheduleDay(dayOfWeek: Int, templateId: String?) {
        viewModelScope.launch {
            repository.upsertScheduleDay(
                WeeklyScheduleDay(dayOfWeek = dayOfWeek, templateId = templateId, isRestDay = templateId == null)
            )
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getCompletedSetsWithPhysiology(0),
        repository.activeSession,
        repository.templates,
        repository.allSessions,
        repository.weeklySchedule,
        userProfileFlow
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val setsList = args[0] as List<SetWithPhysiology>
        val active = args[1] as WorkoutSession?
        @Suppress("UNCHECKED_CAST")
        val templatesList = args[2] as List<WorkoutTemplate>
        @Suppress("UNCHECKED_CAST")
        val allSessionsList = args[3] as List<WorkoutSession>
        @Suppress("UNCHECKED_CAST")
        val scheduleList = args[4] as List<WeeklyScheduleDay>
        val profile = args[5] as UserProfile

        val startOfWeek = getStartOfWeek()
        val muscleStates = RecoveryEngine.computeReadinessFromJoined(setsList)
        val recs = generateRecommendations(muscleStates)
        
        val weeklySets = setsList.filter { it.completedAt >= startOfWeek }
            .flatMap { it.primaryMuscles }
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
        
        val todayAssignment = scheduleList.find { it.dayOfWeek == dayOfWeek }
        val todayState = when {
            todayAssignment == null -> TodayWorkoutState.NotConfigured
            todayAssignment.isRestDay -> TodayWorkoutState.RestDay
            else -> {
                val template = templatesList.find { it.id == todayAssignment.templateId }
                if (template != null) TodayWorkoutState.WorkoutReady(template)
                else TodayWorkoutState.NotConfigured
            }
        }

        DashboardUiState(
            userProfile = profile,
            activeWorkout = active,
            templates = templatesList,
            muscleStates = muscleStates,
            recommendations = recs,
            weeklySetCurrent = weeklySets,
            sessionCount = allSessionsList.count { it.isCompleted },
            streak = calculateStreak(allSessionsList),
            todayWorkout = todayState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (System.currentTimeMillis() < cal.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, -7)
        }
        return cal.timeInMillis
    }

    private fun generateRecommendations(muscleStates: Map<MuscleGroup, Readiness>): List<String> {
        val fullyRecovered = muscleStates.filter { it.value.recoveryPercentage >= 95 }.keys
        val recs = mutableListOf<String>()
        if (fullyRecovered.isNotEmpty()) recs.add("OPTIMAL READINESS: ${fullyRecovered.take(3).joinToString { it.name.replace("_", " ") }}")
        else if (muscleStates.isEmpty()) recs.add("Log your first workout to see analytics")
        else recs.add("RECOVERY IN PROGRESS")
        return recs
    }

    private fun calculateStreak(sessions: List<WorkoutSession>): Int {
        val completedDates = sessions
            .filter { it.isCompleted }
            .map { session ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = session.startTime
                // Normalize to date only
                DateTriple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            }
            .distinct()
            .sortedWith(compareByDescending<DateTriple> { it.year }.thenByDescending { it.month }.thenByDescending { it.day })

        if (completedDates.isEmpty()) return 0

        val today = Calendar.getInstance()
        val todayTriple = DateTriple(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayTriple = DateTriple(yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH), yesterday.get(Calendar.DAY_OF_MONTH))

        // Streak is still alive if last workout was today or yesterday
        if (completedDates.first() != todayTriple && completedDates.first() != yesterdayTriple) return 0

        var streak = 0
        val checkDate = Calendar.getInstance()
        if (completedDates.first() == todayTriple) {
            streak = 1
            checkDate.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Last workout was yesterday
            streak = 1
            checkDate.apply { 
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, -1) // Yesterday
                add(Calendar.DAY_OF_YEAR, -1) // Day before yesterday
            }
        }

        val remainingDates = completedDates.drop(1)
        for (date in remainingDates) {
            val expected = DateTriple(checkDate.get(Calendar.YEAR), checkDate.get(Calendar.MONTH), checkDate.get(Calendar.DAY_OF_MONTH))
            if (date == expected) {
                streak++
                checkDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private data class DateTriple(val year: Int, val month: Int, val day: Int)
}
