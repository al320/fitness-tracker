package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_stats")
@Serializable
data class UserStatsEntity(
    @PrimaryKey val userId: String = "default_user",
    val totalXp: Long = 0,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val lastWorkoutTimestamp: Long = 0,
    val totalWorkouts: Int = 0
)
