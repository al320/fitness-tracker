package com.al32.fitcheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.EliteWhite

@Composable
fun BodyHeatmap(
    muscleIntensity: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ATHLETIC RECOVERY",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BodySilhouette(muscleIntensity, isFront = true, modifier = Modifier.size(140.dp, 280.dp))
            BodySilhouette(muscleIntensity, isFront = false, modifier = Modifier.size(140.dp, 280.dp))
        }
    }
}

@Composable
fun BodySilhouette(
    muscleIntensity: Map<String, Float>,
    isFront: Boolean,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            quadraticTo(w * 0.65f, h * 0.05f, w * 0.6f, h * 0.15f)
            lineTo(w * 0.85f, h * 0.2f)
            lineTo(w * 0.95f, h * 0.45f)
            lineTo(w * 0.85f, h * 0.45f)
            lineTo(w * 0.8f, h * 0.25f)
            lineTo(w * 0.7f, h * 0.45f)
            lineTo(w * 0.75f, h * 0.55f)
            lineTo(w * 0.7f, h * 0.95f)
            lineTo(w * 0.55f, h * 0.95f)
            lineTo(w * 0.52f, h * 0.58f)
            lineTo(w * 0.48f, h * 0.58f)
            lineTo(w * 0.45f, h * 0.95f)
            lineTo(w * 0.3f, h * 0.95f)
            lineTo(w * 0.25f, h * 0.55f)
            lineTo(w * 0.3f, h * 0.45f)
            lineTo(w * 0.2f, h * 0.25f)
            lineTo(w * 0.05f, h * 0.45f)
            moveTo(w * 0.05f, h * 0.45f)
            lineTo(w * 0.15f, h * 0.2f)
            lineTo(w * 0.4f, h * 0.15f)
            quadraticTo(w * 0.35f, h * 0.05f, w * 0.5f, h * 0.05f)
        }
        
        drawPath(path, color = outlineColor, style = Stroke(width = 2f))
        
        if (isFront) {
            drawHeatZone(this, "Chest", muscleIntensity["Chest"] ?: 0f, Size(w * 0.4f, h * 0.15f), Offset(w * 0.3f, h * 0.22f))
        } else {
            drawHeatZone(this, "Back", muscleIntensity["Back"] ?: 0f, Size(w * 0.4f, h * 0.25f), Offset(w * 0.3f, h * 0.22f))
        }
        
        drawHeatZone(this, "Shoulders", muscleIntensity["Shoulders"] ?: 0f, Size(w * 0.12f, h * 0.1f), Offset(w * 0.18f, h * 0.21f))
        drawHeatZone(this, "Shoulders", muscleIntensity["Shoulders"] ?: 0f, Size(w * 0.12f, h * 0.1f), Offset(w * 0.7f, h * 0.21f))
        
        drawHeatZone(this, "Legs", muscleIntensity["Legs"] ?: 0f, Size(w * 0.18f, h * 0.3f), Offset(w * 0.32f, h * 0.6f))
        drawHeatZone(this, "Legs", muscleIntensity["Legs"] ?: 0f, Size(w * 0.18f, h * 0.3f), Offset(w * 0.5f, h * 0.6f))
    }
}

private fun drawHeatZone(
    scope: DrawScope,
    name: String,
    intensity: Float,
    rect: Size,
    offset: Offset
) {
    if (intensity <= 0f) return
    val color = EliteWhite.copy(alpha = (intensity * 0.6f).coerceIn(0.1f, 0.8f))
    scope.drawRoundRect(
        color = color,
        topLeft = offset,
        size = rect,
        cornerRadius = CornerRadius(rect.width / 2, rect.height / 2)
    )
}

@Preview
@Composable
fun BodyHeatmapPreview() {
    FitcheckTheme {
        Box(modifier = Modifier.background(Color.Black).padding(24.dp)) {
            BodyHeatmap(
                muscleIntensity = mapOf(
                    "Chest" to 0.9f,
                    "Shoulders" to 0.5f,
                    "Legs" to 0.3f
                )
            )
        }
    }
}
