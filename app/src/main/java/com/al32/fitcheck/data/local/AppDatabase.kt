package com.al32.fitcheck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.al32.fitcheck.data.local.dao.*
import com.al32.fitcheck.data.local.entities.*
import com.al32.fitcheck.domain.physiology.ExerciseSeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.room.migration.Migration

@Database(
    entities = [
        WorkoutSession::class,
        Exercise::class,
        ExerciseEntry::class,
        SetEntry::class,
        WorkoutTemplate::class,
        TemplateExercise::class,
        PersonalRecord::class,
        WeeklyScheduleDay::class
    ],
    version = 7, // Added isCompleted to WorkoutSession
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun setDao(): SetDao
    abstract fun templateDao(): TemplateDao
    abstract fun prDao(): PRDao
    abstract fun weeklyScheduleDao(): WeeklyScheduleDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create weekly_schedule table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weekly_schedule` (
                        `dayOfWeek` INTEGER NOT NULL, 
                        `templateId` TEXT, 
                        `isRestDay` INTEGER NOT NULL, 
                        PRIMARY KEY(`dayOfWeek`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isCompleted column to workout_sessions
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitcheck_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getDatabase(context).exerciseDao().insertExercises(ExerciseSeedData.exercises)
                        }
                    }
                })
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
