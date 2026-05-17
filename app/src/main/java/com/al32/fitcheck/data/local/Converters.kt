package com.al32.fitcheck.data.local

import androidx.room.TypeConverter
import com.al32.fitcheck.domain.physiology.MuscleGroup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromMuscleGroupList(value: List<MuscleGroup>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toMuscleGroupList(value: String): List<MuscleGroup> {
        return Json.decodeFromString(value)
    }
}
