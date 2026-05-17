package com.al32.fitcheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.recovery.Readiness

@Composable
fun BodyHeatmap(
    muscleStates: Map<MuscleGroup, Readiness>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text("FRONT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            BodySilhouetteView(muscleStates, isFront = true, modifier = Modifier.fillMaxHeight())
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text("BACK", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            BodySilhouetteView(muscleStates, isFront = false, modifier = Modifier.fillMaxHeight())
        }
    }
}

@Composable
fun BodySilhouetteView(
    muscleStates: Map<MuscleGroup, Readiness>,
    isFront: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.aspectRatio(200f / 520f).padding(8.dp)) {
        val w = size.width
        val h = size.height
        val scaleX = w / 200f
        val scaleY = h / 520f
        
        scale(scaleX, scaleY, pivot = Offset.Zero) {
            // Draw silhouette background
            val silhouette = AnatomicalPaths.getSilhouette(isFront)
            drawPath(silhouette, Color(0xFF1C1C1E), style = Fill)
            drawPath(silhouette, Color.White.copy(alpha = 0.1f), style = Stroke(width = 1f))
            
            MuscleGroup.entries.forEach { group ->
                val readiness = muscleStates[group]
                val score = readiness?.score ?: 1.0f
                
                val color = when {
                    score < 0.3f -> Color(0xFFD32F2F)    // Red (Crucial)
                    score < 0.6f -> Color(0xFFFF851B)    // Orange (Significant)
                    score < 0.85f -> Color(0xFF888888)   // Gray (Recovering)
                    else -> Color(0xFF2196F3)            // Blue (Ready)
                }
                
                AnatomicalPaths.getMusclePaths(group, isFront).forEach { path ->
                    drawPath(path, color, style = Fill)
                    drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(width = 0.5f))
                }
            }
        }
    }
}
