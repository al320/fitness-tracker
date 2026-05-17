package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.PersonalRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PRDao {
    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId")
    fun getPRsForExercise(exerciseId: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE sessionId = :sessionId")
    fun getPRsForSession(sessionId: String): Flow<List<PersonalRecord>>

    @Upsert
    suspend fun upsertPR(pr: PersonalRecord)

    @Query("SELECT MAX(value) FROM personal_records WHERE exerciseId = :exerciseId AND type = 'WEIGHT'")
    suspend fun getMaxWeight(exerciseId: String): Float?

    @Query("SELECT MAX(value) FROM personal_records WHERE exerciseId = :exerciseId AND type = 'VOLUME'")
    suspend fun getMaxVolume(exerciseId: String): Float?
}
