package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.preferences.UserProfile
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: FitcheckRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val strengthScoreViewModel: StrengthScoreViewModel
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = preferencesRepository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val strengthScores = strengthScoreViewModel.strengthScores

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            preferencesRepository.updateProfile(profile)
        }
    }
}
