package com.al32.fitcheck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.al32.fitcheck.data.local.dao.ExerciseDao
import com.al32.fitcheck.data.local.dao.SetDao
import com.al32.fitcheck.data.local.dao.UserStatsDao
import com.al32.fitcheck.data.local.dao.WorkoutDao
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.data.local.entities.WorkoutExerciseEntity

@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        UserStatsEntity::class,
        WorkoutExerciseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitcheck_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
