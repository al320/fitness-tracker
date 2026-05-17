package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "personal_records")
@Serializable
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: String,
    val type: PRType,
    val value: Float,
    val achievedAt: Long,
    val sessionId: String
)

enum class PRType { WEIGHT, VOLUME, REPS_AT_WEIGHT }
