package com.al32.fitcheck.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val weightUnit: String = "KG",
    val hapticEnabled: Boolean = true,
    val defaultRestSeconds: Int = 90,
    val themeIntensity: Float = 1.0f
)

class UserPreferencesRepository(private val context: Context) {
    private object PreferencesKeys {
        val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
        val THEME_INTENSITY = floatPreferencesKey("theme_intensity")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                weightUnit = preferences[PreferencesKeys.WEIGHT_UNIT] ?: "KG",
                hapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                defaultRestSeconds = preferences[PreferencesKeys.DEFAULT_REST_SECONDS] ?: 90,
                themeIntensity = preferences[PreferencesKeys.THEME_INTENSITY] ?: 1.0f
            )
        }

    suspend fun updateWeightUnit(unit: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_UNIT] = unit
        }
    }

    suspend fun updateHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun updateDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }
}
