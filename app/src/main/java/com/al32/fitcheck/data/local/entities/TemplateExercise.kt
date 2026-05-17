package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId"), Index("exerciseId")]
)
@Serializable
data class TemplateExercise(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val templateId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: Int = 10,
    val targetWeight: Float? = null,
    val restSeconds: Int = 120,
    val notes: String = ""
)
