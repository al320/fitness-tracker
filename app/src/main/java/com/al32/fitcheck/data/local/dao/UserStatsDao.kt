package com.al32.fitcheck.data.local.dao

import androidx.room.*
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE userId = 'default_user'")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET totalXp = totalXp + :xpGained WHERE userId = 'default_user'")
    suspend fun addXp(xpGained: Int)

    @Query("SELECT * FROM user_stats WHERE userId = 'default_user'")
    suspend fun getUserStatsSync(): UserStatsEntity?
}
