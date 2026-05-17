package com.al32.fitcheck.ui.features.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.al32.fitcheck.data.local.entities.PRType
import com.al32.fitcheck.domain.physiology.displayName
import com.al32.fitcheck.ui.utils.formatElapsedTime
import com.al32.fitcheck.ui.viewmodel.SessionSummaryViewModel
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SessionSummaryScreen(
    sessionId: String,
    navController: NavController,
    viewModel: SessionSummaryViewModel
) {
    val summary by viewModel.summary.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Session Complete", style = MaterialTheme.typography.headlineMedium,
            color = Color.White, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))

        // Stats row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Duration", formatElapsedTime(summary.durationMs / 1000), Modifier.weight(1f))
            StatCard("Volume", "${summary.totalVolumeKg.toInt()} kg", Modifier.weight(1f))
            StatCard("Sets", "${summary.setsCompleted}", Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        // vs last session
        if (summary.volumeChangePercent != null) {
            val sign = if (summary.volumeChangePercent!! >= 0) "↑" else "↓"
            val color = if (summary.volumeChangePercent!! >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
            Text("$sign ${abs(summary.volumeChangePercent!!).toInt()}% vs last ${summary.templateName} session",
                color = color, style = MaterialTheme.typography.bodyMedium)
        }

        // New PRs
        if (summary.newPRs.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("NEW PRs", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF6D00), fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            summary.newPRs.forEach { pr ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(pr.exerciseName.uppercase(), color = Color.White,
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(when (pr.type) {
                            PRType.WEIGHT -> "${pr.value.toInt()} kg"
                            PRType.VOLUME -> "${pr.value.toInt()} kg total"
                            PRType.REPS_AT_WEIGHT -> "${pr.value.toInt()} reps"
                        }, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Muscles worked
        Spacer(Modifier.height(24.dp))
        Text("MUSCLES TRAINED", style = MaterialTheme.typography.labelLarge, color = Color.Gray, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            summary.musclesWorked.forEach { muscle ->
                SuggestionChip(
                    onClick = {}, 
                    label = { Text(muscle.displayName().uppercase()) },
                    colors = SuggestionChipDefaults.suggestionChipColors(labelColor = Color.White)
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                navController.navigate("dashboard") {
                    popUpTo("dashboard") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
        ) {
            Text("DONE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}
