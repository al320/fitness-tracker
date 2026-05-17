package com.al32.fitcheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al32.fitcheck.domain.scoring.StrengthLevel

@Composable
fun ExerciseStrengthCard(
    exerciseName: String,
    estimated1RM: Float,
    bodyweightRatio: Float,
    currentLevel: StrengthLevel,
    nextLevel: StrengthLevel,
    nextLevelRatio: Float,
    lastSessionSummary: String,
    bodyweightKg: Float,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exerciseName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(Modifier.weight(1f))
                StrengthLevelBadge(level = currentLevel)
            }
            Spacer(Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatItem(label = "EST. 1RM", value = "${estimated1RM.toInt()} KG")
                StatItem(label = "VS BODYWEIGHT", value = "${String.format("%.2f", bodyweightRatio)}×")
            }
            
            Spacer(Modifier.height(16.dp))
            
            if (currentLevel != StrengthLevel.WORLD_CLASS) {
                Text(
                    text = "NEXT: $nextLevel — NEEDS ${(nextLevelRatio * bodyweightKg).toInt()} KG (${String.format("%.2f", nextLevelRatio)}× BW)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = currentLevel.color(),
                    trackColor = Color(0xFF2A2A2A)
                )
            } else {
                Text(
                    text = "MAXIMUM LEVEL REACHED",
                    style = MaterialTheme.typography.labelSmall,
                    color = currentLevel.color(),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            Text(
                text = lastSessionSummary,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun StrengthLevelBadge(level: StrengthLevel) {
    Surface(
        color = level.color().copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, level.color().copy(alpha = 0.5f))
    ) {
        Text(
            text = level.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = level.color(),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
