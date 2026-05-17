package com.al32.fitcheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.recovery.Readiness
import com.al32.fitcheck.ui.theme.FitcheckTheme

@Composable
fun BodyHeatmap(
    muscleStates: Map<MuscleGroup, Readiness>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(360.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BodySilhouette(muscleStates, isFront = true, modifier = Modifier.weight(1f))
        BodySilhouette(muscleStates, isFront = false, modifier = Modifier.weight(1f))
    }
}

@Composable
fun BodySilhouette(
    muscleStates: Map<MuscleGroup, Readiness>,
    isFront: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize().padding(16.dp)) {
        val w = size.width
        val h = size.height
        
        // Athletic Lean Outline
        val outlinePath = AnatomicalPaths.drawLeanOutline(w, h)
        drawPath(outlinePath, color = Color.DarkGray.copy(alpha = 0.4f), style = Stroke(width = 1.5f))

        MuscleGroup.entries.forEach { group ->
            val isVisible = when(group) {
                MuscleGroup.LATS, MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS, MuscleGroup.TRAPS, MuscleGroup.REAR_DELTS -> !isFront
                else -> isFront
            }
            
            if (isVisible) {
                drawMuscleRegion(this, w, h, group, muscleStates[group])
            }
        }
    }
}

private fun drawMuscleRegion(
    scope: DrawScope,
    w: Float,
    h: Float,
    group: MuscleGroup,
    state: Readiness?
) {
    val score = state?.score ?: 1.0f
    val color = when {
        score < 0.3f -> Color(0xFFD32F2F) // Red (Overworked)
        score < 0.6f -> Color(0xFFFF8F00) // Amber (Fatigued)
        score < 0.9f -> Color(0xFF606060) // Gray (Neutral)
        else -> Color(0xFF00ACC1) // Blue/Cyan (Recovered)
    }.copy(alpha = 0.8f)

    val paths = AnatomicalPaths.getMusclePaths(group, w, h)
    paths.forEach { path ->
        scope.drawPath(path, color = color, style = Fill)
    }
}

@Preview
@Composable
fun BodyHeatmapPreview() {
    FitcheckTheme {
        Box(modifier = Modifier.size(400.dp)) {
            BodyHeatmap(emptyMap())
        }
    }
}
