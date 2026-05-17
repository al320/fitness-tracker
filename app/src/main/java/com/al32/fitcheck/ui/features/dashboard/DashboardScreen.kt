package com.al32.fitcheck.ui.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.ui.components.XPBar
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.PrestigeGold
import com.al32.fitcheck.data.local.entities.WorkoutEntity
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
    val userStats = uiState.userStats
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartWorkout,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Start Workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                HeaderSection()
            }

            item {
                QuickStartSection(
                    activeWorkout = uiState.activeWorkout,
                    templates = uiState.templates,
                    onContinue = onContinueWorkout,
                    onStartTemplate = onStartTemplate
                )
            }

            item {
                XPBar(
                    currentXp = userStats.totalXp.toInt() % 1000,
                    nextLevelXp = 1000,
                    level = userStats.level,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        label = "STREAK",
                        value = userStats.currentStreak.toString(),
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "WORKOUTS",
                        value = userStats.totalWorkouts.toString(),
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                RecentActivitySection()
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "CHAMPION",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
        }
        
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentActivitySection() {
    Column {
        Text(
            text = "ELITE MILESTONES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        AchievementItem(
            title = "HEAVY HITTER",
            desc = "Bench Press PR: 100kg",
            icon = Icons.Default.EmojiEvents
        )
    }
}

@Composable
fun AchievementItem(title: String, desc: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrestigeGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickStartSection(
    activeWorkout: WorkoutEntity?,
    templates: List<WorkoutEntity>,
    onContinue: (Long) -> Unit,
    onStartTemplate: (WorkoutEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (activeWorkout != null) {
            Card(
                onClick = { onContinue(activeWorkout.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("RESUME SESSION", fontWeight = FontWeight.Black, color = Color.Black)
                        Text(activeWorkout.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Black.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Text(
            text = "QUICK START TEMPLATES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (templates.isEmpty()) {
            Text("No templates yet. Save a workout to see it here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }

        templates.take(3).forEach { template ->
            TemplateCard(template, onClick = { onStartTemplate(template) })
        }
    }
}

@Composable
fun TemplateCard(template: WorkoutEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bolt, null, tint = PrestigeGold)
            Spacer(Modifier.width(12.dp))
            Text(template.name.uppercase(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
fun DashboardPreview() {
    FitcheckTheme {
        DashboardContent(
            onStartWorkout = {},
            onContinueWorkout = {},
            onStartTemplate = {},
            uiState = DashboardUiState(
                userStats = UserStatsEntity(
                    level = 12,
                    totalXp = 2450,
                    currentStreak = 5,
                    totalWorkouts = 28
                ),
                templates = listOf(WorkoutEntity(name = "Upper Power", startTime = 0, isTemplate = true))
            )
        )
    }
}
