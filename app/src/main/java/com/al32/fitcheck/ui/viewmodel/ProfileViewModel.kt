package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*

class ProfileViewModel(private val repository: FitcheckRepository) : ViewModel() {

    val userStats: StateFlow<UserStatsEntity> = repository.userStats
        .map { it ?: UserStatsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStatsEntity()
        )
}
