package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.WeeklyScheduleDay
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val schedule: Map<Int, WorkoutTemplate?> = emptyMap(),
    val allTemplates: List<WorkoutTemplate> = emptyList(),
    val isLoading: Boolean = true
)

class ScheduleViewModel(
    private val repository: FitcheckRepository
) : ViewModel() {

    val uiState: StateFlow<ScheduleUiState> = combine(
        repository.weeklySchedule,
        repository.templates
    ) { scheduleDays, templates ->
        // Initialize all 7 days if schedule is empty on first load
        if (scheduleDays.isEmpty()) {
            initializeSchedule()
        }

        val scheduleMap = (1..7).associateWith { dayOfWeek ->
            val dayAssignment = scheduleDays.find { it.dayOfWeek == dayOfWeek }
            if (dayAssignment?.isRestDay == false) {
                templates.find { it.id == dayAssignment.templateId }
            } else {
                null
            }
        }

        ScheduleUiState(
            schedule = scheduleMap,
            allTemplates = templates,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScheduleUiState()
    )

    private fun initializeSchedule() {
        viewModelScope.launch {
            (1..7).forEach { day ->
                repository.upsertScheduleDay(
                    WeeklyScheduleDay(dayOfWeek = day, templateId = null, isRestDay = false)
                )
            }
        }
    }

    fun assignTemplateToDay(dayOfWeek: Int, templateId: String) {
        viewModelScope.launch {
            repository.upsertScheduleDay(
                WeeklyScheduleDay(dayOfWeek = dayOfWeek, templateId = templateId, isRestDay = false)
            )
        }
    }

    fun setRestDay(dayOfWeek: Int) {
        viewModelScope.launch {
            repository.upsertScheduleDay(
                WeeklyScheduleDay(dayOfWeek = dayOfWeek, templateId = null, isRestDay = true)
            )
        }
    }

    fun clearDay(dayOfWeek: Int) {
        viewModelScope.launch {
            repository.upsertScheduleDay(
                WeeklyScheduleDay(dayOfWeek = dayOfWeek, templateId = null, isRestDay = false)
            )
        }
    }
}
