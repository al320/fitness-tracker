package com.al32.fitcheck

import android.app.Application
import com.al32.fitcheck.data.local.AppDatabase
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.physiology.PhysiologyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitcheckApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        FitcheckRepository(
            database,
            database.workoutDao(),
            database.exerciseDao(),
            database.setDao(),
            database.templateDao(),
            database.prDao(),
            database.weeklyScheduleDao()
        ) 
    }

    override fun onCreate() {
        super.onCreate()
        preseedExercises()
    }

    private fun preseedExercises() {
        applicationScope.launch {
            if (repository.getExerciseCount() == 0) {
                // Using seed data from AppDatabase.Callback now but repository still has insert logic
            }
        }
    }
}
