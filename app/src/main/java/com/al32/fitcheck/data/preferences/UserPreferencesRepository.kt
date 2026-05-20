package com.al32.fitcheck.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED }

data class UserProfile(
    val name: String = "Athlete",
    val bodyweightKg: Float = 0f,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val hasCompletedOnboarding: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {
    private object PreferencesKeys {
        val NAME = stringPreferencesKey("user_name")
        val BODYWEIGHT = floatPreferencesKey("bodyweight")
        val EXPERIENCE = stringPreferencesKey("experience")
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    private val dataStore = context.userDataStore

    val userProfileFlow: Flow<UserProfile> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            UserProfile(
                name = preferences[PreferencesKeys.NAME] ?: "Athlete",
                bodyweightKg = preferences[PreferencesKeys.BODYWEIGHT] ?: 0f,
                experienceLevel = try {
                    ExperienceLevel.valueOf(preferences[PreferencesKeys.EXPERIENCE] ?: ExperienceLevel.BEGINNER.name)
                } catch (e: Exception) {
                    ExperienceLevel.BEGINNER
                },
                hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
            )
        }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NAME] = profile.name
            preferences[PreferencesKeys.BODYWEIGHT] = profile.bodyweightKg
            preferences[PreferencesKeys.EXPERIENCE] = profile.experienceLevel.name
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = profile.hasCompletedOnboarding
        }
    }
}
