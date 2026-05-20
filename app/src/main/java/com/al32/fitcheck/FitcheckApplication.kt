package com.al32.fitcheck

import android.app.Application
import com.al32.fitcheck.data.local.AppDatabase
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.repository.FitcheckRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FitcheckApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    
    val preferencesRepository by lazy { UserPreferencesRepository(this) }

    val repository by lazy { 
        FitcheckRepository(
            database,
            database.workoutDao(),
            database.exerciseDao(),
            database.setDao(),
            database.templateDao(),
            database.prDao(),
            database.weeklyScheduleDao(),
            database.analyticsDao(),
            preferencesRepository
        ) 
    }

    override fun onCreate() {
        super.onCreate()
    }
}
