package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.scoring.StrengthLevel
import com.al32.fitcheck.domain.scoring.StrengthScorer
import com.al32.fitcheck.domain.physiology.MovementPattern
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

data class ExerciseStrengthInfo(
    val exerciseId: String,
    val exerciseName: String,
    val pattern: MovementPattern,
    val estimated1RM: Float,
    val bodyweightRatio: Float,
    val currentLevel: StrengthLevel,
    val nextLevel: StrengthLevel,
    val nextLevelRatio: Float,
    val progress: Float,
    val lastSessionSummary: String
)

data class StrengthScores(
    val exercises: List<ExerciseStrengthInfo> = emptyList()
)

class StrengthScoreViewModel(
    private val repository: FitcheckRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val userProfile = preferencesRepository.userProfileFlow

    val strengthScores: StateFlow<StrengthScores?> = combine(
        repository.getBestE1RMPerExercise(System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)),
        repository.allExercises,
        userProfile
    ) { bests, allExercises, profile ->
        if (bests.isEmpty()) null
        else {
            val exerciseInfos = bests.mapNotNull { best ->
                val exercise = allExercises.find { it.id == best.exerciseId } ?: return@mapNotNull null
                val standard = StrengthScorer.getStandard(exercise.id, exercise.movementPattern)
                val ratio = best.bestE1RM / (if (profile.bodyweightKg > 0) profile.bodyweightKg else 1f)
                val currentLevel = StrengthScorer.computeLevel(best.bestE1RM, profile.bodyweightKg, standard)
                val nextLevel = currentLevel.next()
                
                val currentThreshold = standard.getRatio(currentLevel)
                val nextThreshold = standard.getRatio(nextLevel)
                
                val progress = if (nextThreshold > currentThreshold) {
                    ((ratio - currentThreshold) / (nextThreshold - currentThreshold)).coerceIn(0f, 1f)
                } else 1f

                ExerciseStrengthInfo(
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    pattern = exercise.movementPattern,
                    estimated1RM = best.bestE1RM,
                    bodyweightRatio = ratio,
                    currentLevel = currentLevel,
                    nextLevel = nextLevel,
                    nextLevelRatio = nextThreshold,
                    progress = progress,
                    lastSessionSummary = "LOGGED RECENTLY" // Placeholder for now
                )
            }
            StrengthScores(exercises = exerciseInfos)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
