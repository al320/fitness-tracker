package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "set_entries",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntry::class,
            parentColumns = ["id"],
            childColumns = ["exerciseEntryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseEntryId")]
)
@Serializable
data class SetEntry(
    @PrimaryKey val id: String,
    val exerciseEntryId: String,
    val weight: Float,
    val reps: Int,
    val completedAt: Long,
    val isCompleted: Boolean
)
