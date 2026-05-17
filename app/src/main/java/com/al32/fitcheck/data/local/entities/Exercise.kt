package com.al32.fitcheck.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.al32.fitcheck.domain.physiology.CNSLoad
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlinx.serialization.Serializable

@Entity(tableName = "exercises")
@Serializable
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val movementPattern: MovementPattern,
    val primaryMuscles: List<MuscleGroup>,
    val secondaryMuscles: List<MuscleGroup>,
    val primaryFatigueCoefficient: Float = 1.0f,
    val secondaryFatigueCoefficient: Float = 0.35f,
    val cnsLoad: CNSLoad,
    val isCustom: Boolean = false
)
