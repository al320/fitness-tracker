package com.al32.fitcheck.ui.features.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.R
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.domain.physiology.MuscleGroup
import com.al32.fitcheck.domain.physiology.MuscleWithTimeRemaining
import com.al32.fitcheck.domain.physiology.displayName
import com.al32.fitcheck.ui.components.BodyHeatmap
import com.al32.fitcheck.ui.utils.formatRecoveryTime
import com.al32.fitcheck.ui.viewmodel.DashboardUiState
import com.al32.fitcheck.ui.viewmodel.DashboardViewModel
import com.al32.fitcheck.ui.viewmodel.TodayWorkoutState

@Composable
fun DashboardScreen(
    onStartWorkout: () -> Unit,
    onContinueWorkout: (String) -> Unit,
    onStartTemplate: (WorkoutTemplate) -> Unit,
    onConfigureSchedule: () -> Unit,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    DashboardContent(onStartWorkout, onContinueWorkout, onStartTemplate, onConfigureSchedule, uiState)
}

@Composable
fun DashboardContent(
    onStartWorkout: () -> Unit,
    onContinueWorkout: (String) -> Unit,
    onStartTemplate: (WorkoutTemplate) -> Unit,
    onConfigureSchedule: () -> Unit,
    uiState: DashboardUiState
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartWorkout,
                containerColor = Color(0xFFFF851B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            
            BodyHeatmap(
                muscleStates = uiState.muscleStates,
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )

            val recovering = uiState.muscleStates.filter { it.value.score < 0.85f }
                .map { MuscleWithTimeRemaining(it.key, it.value.hoursUntilRecovered) }
            val ready = uiState.muscleStates.filter { it.value.score >= 0.85f }.keys.toList()
            
            CollapsibleRecoveryCard(
                recoveringMuscles = recovering,
                readyMuscles = ready
            )

            TodayWorkoutCard(
                state = uiState.todayWorkout, 
                onStartQuick = onStartWorkout, 
                onStartTemplate = onStartTemplate, 
                onConfigure = onConfigureSchedule
            )

            TrainingPlanSection(uiState.templates, onStartTemplate)

            WeeklyVolumeSection(uiState)

            StatsSummaryRow(uiState)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollapsibleRecoveryCard(
    recoveringMuscles: List<MuscleWithTimeRemaining>,
    readyMuscles: List<MuscleGroup>
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(8.dp).background(
                        color = if (recoveringMuscles.isEmpty()) Color(0xFF2E7D32) else Color(0xFFEF5350),
                        shape = CircleShape
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (recoveringMuscles.isEmpty()) "ALL MUSCLES READY"
                           else "NEEDS RECOVERY (${recoveringMuscles.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (recoveringMuscles.isEmpty()) Color(0xFF4CAF50) else Color(0xFFEF5350),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Black
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    if (recoveringMuscles.isNotEmpty()) {
                        Text("Recovering", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            recoveringMuscles.sortedBy { it.hoursRemaining }.forEach { m ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${m.muscleGroup.displayName()} — ${formatRecoveryTime(m.hoursRemaining.toFloat())}",
                                        style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                    if (readyMuscles.isNotEmpty()) {
                        Text("Ready to train", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            readyMuscles.forEach { m ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(m.displayName(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF1565C0)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayWorkoutCard(
    state: TodayWorkoutState,
    onStartQuick: () -> Unit,
    onStartTemplate: (WorkoutTemplate) -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when(state) {
                        is TodayWorkoutState.RestDay -> Icons.Default.Bedtime
                        is TodayWorkoutState.WorkoutReady -> Icons.Default.FitnessCenter
                        else -> Icons.AutoMirrored.Filled.EventNote
                    },
                    null,
                    tint = Color(0xFFFF851B)
                )
                Spacer(Modifier.width(12.dp))
                Text("TODAY'S WORKOUT", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            
            when(state) {
                is TodayWorkoutState.NotConfigured -> {
                    Text("No routine scheduled", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onConfigure,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B))
                    ) {
                        Text("CONFIGURE WEEKLY PLAN")
                    }
                }
                is TodayWorkoutState.RestDay -> {
                    Text("REST DAY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("RECOVERY IN PROGRESS", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                is TodayWorkoutState.WorkoutReady -> {
                    Text(state.template.name.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onStartTemplate(state.template) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B))
                    ) {
                        Text("START WORKOUT")
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingPlanSection(templates: List<WorkoutTemplate>, onSelect: (WorkoutTemplate) -> Unit) {
    Column {
        Text("YOUR TRAINING PLAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (templates.isEmpty()) {
                Text("No routines created yet", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
            } else {
                templates.take(3).forEach { template ->
                    Card(
                        modifier = Modifier.width(160.dp).clickable { onSelect(template) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(template.name.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                            Text("Suggested", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2196F3))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyVolumeSection(uiState: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("WEEKLY VOLUME TARGETS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        
        if (uiState.weeklySetCurrent.isEmpty()) {
            Text("No training data this week", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
        } else {
            uiState.weeklySetCurrent.entries.take(4).forEach { (group, count) ->
                val target = uiState.weeklySetTargets[group] ?: 10
                val progress = count.toFloat() / target
                val color = when {
                    progress >= 0.8f -> Color.Green
                    progress >= 0.5f -> Color(0xFFFF851B)
                    else -> Color.Red
                }
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(group.name.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        Text("$count / $target sets", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = color,
                        trackColor = Color.DarkGray.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

@Composable
fun StatsSummaryRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DashboardStatItem("STREAK", "0D")
        DashboardStatItem("SESSIONS", uiState.sessionCount.toString())
        DashboardStatItem("VOLUME", formatVolume(uiState.totalVolume))
    }
}

private fun formatVolume(value: Float): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000 -> String.format("%.1fK", value / 1_000.0)
        else -> String.format("%.0f", value)
    }
}

@Composable
fun DashboardStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSheet(onSave: (String, Float) -> Unit) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { /* Block dismissal until saved */ },
        dragHandle = null,
        containerColor = Color(0xFF111111)
    ) {
        Column(Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text(stringResource(R.string.onboarding_welcome), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text(stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Name") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF851B), unfocusedBorderColor = Color.DarkGray)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = weight, 
                onValueChange = { weight = it }, 
                label = { Text("Bodyweight (kg)") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF851B), unfocusedBorderColor = Color.DarkGray)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSave(name, weight.toFloatOrNull() ?: 0f) },
                enabled = name.isNotBlank() && weight.toFloatOrNull() != null && (weight.toFloatOrNull() ?: 0f) > 0f,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B))
            ) {
                Text("GET STARTED", fontWeight = FontWeight.Bold)
            }
        }
    }
}
