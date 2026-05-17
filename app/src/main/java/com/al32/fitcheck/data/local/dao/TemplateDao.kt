package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.data.local.entities.TemplateExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    fun getAllTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun getExercisesForTemplate(templateId: String): Flow<List<TemplateExercise>>

    @Upsert
    suspend fun upsertTemplate(template: WorkoutTemplate)

    @Upsert
    suspend fun upsertTemplateExercise(exercise: TemplateExercise)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteExercisesForTemplate(templateId: String)
}
