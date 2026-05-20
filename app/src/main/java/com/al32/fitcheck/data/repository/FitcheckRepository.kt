package com.al32.fitcheck.data.repository

import androidx.room.withTransaction
import com.al32.fitcheck.data.local.AppDatabase
import com.al32.fitcheck.data.local.dao.*
import com.al32.fitcheck.data.local.entities.*
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.preferences.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FitcheckRepository(
    private val database: AppDatabase,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val setDao: SetDao,
    private val templateDao: TemplateDao,
    private val prDao: PRDao,
    private val weeklyScheduleDao: WeeklyScheduleDao,
    private val analyticsDao: AnalyticsDao,
    private val preferencesRepository: UserPreferencesRepository
) {
    // Session flows
    val allSessions: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
    val activeSession: Flow<WorkoutSession?> = workoutDao.getActiveSession()
    val templates: Flow<List<WorkoutTemplate>> = templateDao.getAllTemplates()
    val weeklySchedule: Flow<List<WeeklyScheduleDay>> = weeklyScheduleDao.getAllScheduleDays()
    val userProfileFlow: Flow<UserProfile> = preferencesRepository.userProfileFlow

    suspend fun updateProfile(profile: UserProfile) = preferencesRepository.updateProfile(profile)
    
    fun getDailyVolume(): Flow<List<DailyVolume>> = analyticsDao.getVolumeOverTime()

    suspend fun upsertScheduleDay(day: WeeklyScheduleDay) {
        weeklyScheduleDao.upsertScheduleDay(day)
    }

    suspend fun createSession(name: String): String {
        val id = UUID.randomUUID().toString()
        workoutDao.insertSession(WorkoutSession(id, System.currentTimeMillis(), null, name))
        return id
    }

    suspend fun getSessionById(id: String): WorkoutSession? = workoutDao.getSessionById(id)

    suspend fun startWorkoutFromTemplate(templateId: String, sessionId: String) {
        val templateExercises = templateDao.getExercisesForTemplate(templateId).first()
        templateExercises.forEach { te ->
            val entryId = UUID.randomUUID().toString()
            workoutDao.insertEntry(ExerciseEntry(entryId, sessionId, te.exerciseId, te.orderIndex))
            
            repeat(te.targetSets) {
                setDao.insertSet(SetEntry(
                    id = UUID.randomUUID().toString(),
                    exerciseEntryId = entryId,
                    weight = te.targetWeight ?: 0f,
                    reps = te.targetReps,
                    completedAt = System.currentTimeMillis(),
                    isCompleted = false
                ))
            }
        }
    }

    suspend fun completeWorkout(
        session: WorkoutSession,
        exerciseEntries: List<ExerciseEntry>,
        setEntries: List<SetEntry>
    ) {
        database.withTransaction {
            workoutDao.upsertSession(session.copy(
                endTime = System.currentTimeMillis(),
                isCompleted = true
            ))
            exerciseEntries.forEach { workoutDao.insertEntry(it) }
            setEntries.filter { it.isCompleted }.forEach { setDao.insertSet(it) }
        }
        detectNewPRs(session.id)
    }

    // Exercise Entries
    fun getEntriesForSession(sessionId: String): Flow<List<ExerciseEntry>> = workoutDao.getEntriesForSession(sessionId)

    suspend fun addExerciseToSession(sessionId: String, exerciseId: String, order: Int) {
        val id = UUID.randomUUID().toString()
        workoutDao.insertEntry(ExerciseEntry(id, sessionId, exerciseId, order))
    }

    suspend fun deleteEntry(entry: ExerciseEntry) = workoutDao.deleteEntry(entry)

    suspend fun getEntriesForSessionSync(sessionId: String) = workoutDao.getEntriesForSessionSyncNow(sessionId)

    // Sets
    fun getSetsForEntry(entryId: String): Flow<List<SetEntry>> = setDao.getSetsForEntry(entryId)

    suspend fun getSetsForEntrySync(entryId: String) = setDao.getSetsForEntrySyncNow(entryId)

    suspend fun addSetToEntry(entryId: String) {
        val id = UUID.randomUUID().toString()
        setDao.insertSet(SetEntry(id, entryId, 0f, 0, System.currentTimeMillis(), false))
    }

    suspend fun updateSet(set: SetEntry) = setDao.updateSet(set)

    suspend fun deleteSet(set: SetEntry) = setDao.deleteSet(set)

    // Templates
    fun getExercisesForTemplate(templateId: String): Flow<List<TemplateExercise>> = templateDao.getExercisesForTemplate(templateId)
    
    suspend fun upsertTemplate(template: WorkoutTemplate) = templateDao.upsertTemplate(template)
    
    suspend fun upsertTemplateExercise(exercise: TemplateExercise) = templateDao.upsertTemplateExercise(exercise)
    
    suspend fun deleteTemplate(template: WorkoutTemplate) = templateDao.deleteTemplate(template)

    // Exercise Library
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    
    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()
    
    suspend fun insertExercises(exercises: List<Exercise>) = exerciseDao.insertExercises(exercises)

    suspend fun getExerciseById(id: String): Exercise? = exerciseDao.getExerciseById(id)

    fun getHistoryForExercise(exerciseId: String): Flow<List<SetEntry>> = setDao.getHistoryForExercise(exerciseId)

    suspend fun getPersonalRecord(exerciseId: String): SetEntry? = setDao.getPersonalRecord(exerciseId)

    fun getSetsForSession(sessionId: String): Flow<List<SetEntry>> = setDao.getSetsForSession(sessionId)

    fun getPRsForSession(sessionId: String): Flow<List<PersonalRecord>> = prDao.getPRsForSession(sessionId)

    fun getCompletedSetsWithPhysiology(since: Long): Flow<List<SetWithPhysiology>> = setDao.getCompletedSetsWithPhysiology(since)

    fun getPreviousPerformance(exerciseId: String, currentSessionId: String): Flow<List<SetEntry>> = 
        setDao.getPreviousPerformance(exerciseId, currentSessionId)

    fun getBestE1RMPerExercise(since: Long): Flow<List<ExerciseBest1RM>> = setDao.getBestE1RMPerExercise(since)

    suspend fun detectNewPRs(sessionId: String): List<PersonalRecord> {
        val newPRs = mutableListOf<PersonalRecord>()
        val sessionSets = setDao.getCompletedSetsForSessionSync(sessionId)
        val entries = workoutDao.getEntriesForSessionSyncNow(sessionId)

        for (set in sessionSets) {
            val entry = entries.find { it.id == set.exerciseEntryId } ?: continue
            val exerciseId = entry.exerciseId
            
            val allPrev = setDao.getCompletedSetsForExerciseSync(exerciseId)
                .filter { it.completedAt < set.completedAt }

            val maxPrevWeight = allPrev.maxOfOrNull { it.weight } ?: 0f
            if (set.weight > maxPrevWeight) {
                val pr = PersonalRecord(exerciseId = exerciseId, type = PRType.WEIGHT, value = set.weight, achievedAt = set.completedAt, sessionId = sessionId)
                prDao.upsertPR(pr)
                newPRs.add(pr)
            }
            
            val thisVolume = set.weight * set.reps
            val maxPrevVolume = allPrev.maxOfOrNull { it.weight * it.reps } ?: 0f
            if (thisVolume > maxPrevVolume) {
                val pr = PersonalRecord(exerciseId = exerciseId, type = PRType.VOLUME, value = thisVolume, achievedAt = set.completedAt, sessionId = sessionId)
                prDao.upsertPR(pr)
                newPRs.add(pr)
            }
        }
        return newPRs
    }
}
