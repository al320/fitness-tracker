package com.al32.fitcheck.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.data.repository.FitcheckRepository
import com.al32.fitcheck.domain.scoring.StrengthLevel
import com.al32.fitcheck.domain.scoring.StrengthScorer
import com.al32.fitcheck.domain.physiology.MovementPattern
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt

data class ExerciseStrengthInfo(
    val exerciseId: String,
    val exerciseName: String,
    val pattern: MovementPattern,
    val estimated1RM: Float,
    val bodyweightRatio: Float,
    val currentLevel: StrengthLevel,
    val nextLevel: StrengthLevel?,
    val needsKgToReachNext: Int,
    val lastSessionSummary: String,
    val progress: Float
)

data class StrengthScores(
    val exercises: List<ExerciseStrengthInfo> = emptyList(),
    val missingBodyweight: Boolean = false
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
        if (profile.bodyweightKg <= 0f) {
            return@combine StrengthScores(missingBodyweight = true)
        }

        val exerciseInfos = bests.mapNotNull { best ->
            val exercise = allExercises.find { it.id == best.exerciseId } ?: return@mapNotNull null
            val standard = StrengthScorer.getStandard(exercise.id, exercise.movementPattern)
            val ratio = best.bestE1RM / profile.bodyweightKg
            val currentLevel = StrengthScorer.computeLevel(best.bestE1RM, profile.bodyweightKg, standard)
            val nextLevel = currentLevel.next()
            
            val nextThreshold = nextLevel?.let { standard.getRatio(it) } ?: standard.getRatio(currentLevel)
            val nextWeightNeeded = (nextThreshold * profile.bodyweightKg)
            val kgDiff = (nextWeightNeeded - best.bestE1RM).coerceAtLeast(0f).roundToInt()

            // Corrected progress calculation: 
            // If currentLevel is BEGINNER and target is NOVICE, progress is (ratio / novice_ratio)
            val progress = (best.bestE1RM / nextWeightNeeded).coerceIn(0f, 1f)

            ExerciseStrengthInfo(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                pattern = exercise.movementPattern,
                estimated1RM = best.bestE1RM,
                bodyweightRatio = ratio,
                currentLevel = currentLevel,
                nextLevel = nextLevel,
                progress = progress,
                needsKgToReachNext = kgDiff,
                lastSessionSummary = ""
            )
        }
        StrengthScores(exercises = exerciseInfos)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
