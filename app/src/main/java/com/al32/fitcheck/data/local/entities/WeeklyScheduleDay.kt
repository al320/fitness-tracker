package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "weekly_schedule")
@Serializable
data class WeeklyScheduleDay(
    @PrimaryKey val dayOfWeek: Int,  // 1=Monday ... 7=Sunday
    val templateId: String?,          // null = rest day
    val isRestDay: Boolean = false
)
