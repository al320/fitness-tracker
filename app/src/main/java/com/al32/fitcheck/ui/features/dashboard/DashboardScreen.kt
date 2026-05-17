package com.al32.fitcheck.ui.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.WorkoutEntity
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.ui.components.BodyHeatmap
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.EliteWhite
import com.al32.fitcheck.ui.viewmodel.DashboardUiState
import com.al32.fitcheck.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onStartWorkout: () -> Unit,
    onContinueWorkout: (Long) -> Unit,
    onStartTemplate: (WorkoutEntity) -> Unit,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardContent(onStartWorkout, onContinueWorkout, onStartTemplate, uiState)
}

@Composable
fun DashboardContent(
    onStartWorkout: () -> Unit,
    onContinueWorkout: (Long) -> Unit,
    onStartTemplate: (WorkoutEntity) -> Unit,
    uiState: DashboardUiState
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartWorkout,
                containerColor = EliteWhite,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("RECOVERY BRIEFING", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            }

            item {
                BodyHeatmap(muscleStates = uiState.muscleStates)
            }

            if (uiState.recommendations.isNotEmpty()) {
                item {
                    RecommendationCard(uiState.recommendations)
                }
            }

            item {
                QuickStartGrid(
                    activeWorkout = uiState.activeWorkout,
                    templates = uiState.templates,
                    onContinue = onContinueWorkout,
                    onStartTemplate = onStartTemplate
                )
            }

            item {
                MuscleVolumeSection(uiState)
            }

            item {
                TrainingStatsRow(uiState)
            }
            
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun RecommendationCard(recs: List<String>) {
    Surface(
        color = Color.DarkGray.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TRAINING FOCUS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            recs.forEach { rec ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val color = if (rec.contains("RECOVERY")) Color(0xFFD32F2F) else Color(0xFF00ACC1)
                    Box(Modifier.size(6.dp).background(color, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(rec, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickStartGrid(
    activeWorkout: WorkoutEntity?,
    templates: List<WorkoutEntity>,
    onContinue: (Long) -> Unit,
    onStartTemplate: (WorkoutEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (activeWorkout != null) {
            Button(
                onClick = { onContinue(activeWorkout.id) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EliteWhite)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("RESUME: ${activeWorkout.name.uppercase()}", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }

        Text("QUICK START", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        templates.take(4).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { template ->
                    OutlinedButton(
                        onClick = { onStartTemplate(template) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)
                    ) {
                        Text(template.name.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MuscleVolumeSection(uiState: DashboardUiState) {
    val states = uiState.muscleStates
    val pushVal = (states[MuscleGroup.LOWER_CHEST]?.fatigueLevel ?: 0f) / 2f
    val pullVal = (states[MuscleGroup.LATS]?.fatigueLevel ?: 0f) / 2f
    val legsVal = (states[MuscleGroup.QUADS]?.fatigueLevel ?: 0f) / 2f

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("TRAINING INTENSITY BALANCE", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        VolumeBar("PUSH PATTERNS", pushVal.coerceIn(0f, 1f))
        VolumeBar("PULL PATTERNS", pullVal.coerceIn(0f, 1f))
        VolumeBar("LOWER BODY", legsVal.coerceIn(0f, 1f))
    }
}

@Composable
fun VolumeBar(label: String, progress: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = Color(0xFF00ACC1),
            trackColor = Color.DarkGray.copy(alpha = 0.3f),
        )
    }
}

@Composable
fun TrainingStatsRow(uiState: DashboardUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SimpleStat("STREAK", "${uiState.userStats.currentStreak}D")
        SimpleStat("SESSIONS", uiState.userStats.totalWorkouts.toString())
        SimpleStat("VOLUME", String.format(java.util.Locale.getDefault(), "%.1fM", uiState.userStats.totalXp / 1000000.0))
    }
}

@Composable
fun SimpleStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}
