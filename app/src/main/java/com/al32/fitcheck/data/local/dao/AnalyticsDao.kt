package com.al32.fitcheck.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DailyVolume(
    val sessionDate: String,
    val totalVolume: Double
)

@Dao
interface AnalyticsDao {
    @Query("""
        SELECT DATE(w.startTime / 1000, 'unixepoch', 'localtime') as sessionDate,
               SUM(s.weight * s.reps) as totalVolume
        FROM set_entries s
        INNER JOIN exercise_entries e ON s.exerciseEntryId = e.id
        INNER JOIN workout_sessions w ON e.sessionId = w.id
        WHERE w.isCompleted = 1 AND s.isCompleted = 1
        GROUP BY sessionDate
        ORDER BY sessionDate ASC
    """)
    fun getVolumeOverTime(): Flow<List<DailyVolume>>
}
