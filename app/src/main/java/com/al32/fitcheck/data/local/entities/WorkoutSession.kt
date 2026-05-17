package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "workout_sessions")
@Serializable
data class WorkoutSession(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long?,
    val name: String,
    val isTemplate: Boolean = false,
    val isCompleted: Boolean = false
)
