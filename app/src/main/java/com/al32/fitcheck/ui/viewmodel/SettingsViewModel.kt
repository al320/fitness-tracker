package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.preferences.UserPreferences
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SettingsViewModel(
    private val repository: FitcheckRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun updateWeightUnit(unit: String) {
        viewModelScope.launch {
            preferencesRepository.updateWeightUnit(unit)
        }
    }

    fun updateHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHapticEnabled(enabled)
        }
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val backup = repository.getBackupData()
            val json = Json.encodeToString(com.al32.fitcheck.data.local.BackupData.serializer(), backup)
            onResult(json)
        }
    }

    fun importData(json: String, onComplete: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val data = Json.decodeFromString<com.al32.fitcheck.data.local.BackupData>(json)
                repository.restoreBackup(data)
                onComplete()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown parsing error")
            }
        }
    }
}
