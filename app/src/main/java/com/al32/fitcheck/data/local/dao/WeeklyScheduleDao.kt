package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.WeeklyScheduleDay
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyScheduleDao {
    @Query("SELECT * FROM weekly_schedule ORDER BY dayOfWeek ASC")
    fun getAllScheduleDays(): Flow<List<WeeklyScheduleDay>>

    @Query("SELECT * FROM weekly_schedule WHERE dayOfWeek = :dayOfWeek")
    suspend fun getScheduleDay(dayOfWeek: Int): WeeklyScheduleDay?

    @Upsert
    suspend fun upsertScheduleDay(day: WeeklyScheduleDay)

    @Query("DELETE FROM weekly_schedule")
    suspend fun clearSchedule()
}
