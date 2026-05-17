package com.al32.fitcheck.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.ui.components.ExerciseStrengthCard
import com.al32.fitcheck.ui.components.StrengthLevelBadge
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.data.preferences.UserProfile
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.viewmodel.ProfileViewModel
import com.al32.fitcheck.ui.viewmodel.StrengthScores
import com.al32.fitcheck.ui.viewmodel.ExerciseStrengthInfo

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val strengthScores by viewModel.strengthScores.collectAsState()
    
    var isEditing by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(Modifier.height(32.dp))
                ProfileInfoCard(profile, onEdit = { isEditing = true })
            }

            if (strengthScores != null && strengthScores!!.exercises.isNotEmpty()) {
                item {
                    Text("STRENGTH PROGRESS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                }
                
                items(strengthScores!!.exercises.size) { index ->
                    val info = strengthScores!!.exercises[index]
                    ExerciseStrengthCard(
                        exerciseName = info.exerciseName,
                        estimated1RM = info.estimated1RM,
                        bodyweightRatio = info.bodyweightRatio,
                        currentLevel = info.currentLevel,
                        nextLevel = info.nextLevel,
                        nextLevelRatio = info.nextLevelRatio,
                        lastSessionSummary = info.lastSessionSummary,
                        bodyweightKg = profile.bodyweightKg,
                        progress = info.progress
                    )
                }
                
                // Placeholder for missing core patterns
                val loggedPatterns = strengthScores!!.exercises.map { it.pattern }.toSet()
                val corePatterns = listOf(MovementPattern.PUSH, MovementPattern.PULL, MovementPattern.SQUAT, MovementPattern.HINGE)
                val missingPatterns = corePatterns.filter { it !in loggedPatterns }
                
                if (missingPatterns.isNotEmpty()) {
                    items(missingPatterns.size) { index ->
                        MissingPatternPlaceholder(missingPatterns[index])
                    }
                }
            } else {
                item {
                    NoDataPlaceholder()
                }
            }
            
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (isEditing) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { isEditing = false },
            onSave = { 
                viewModel.updateProfile(it)
                isEditing = false
            }
        )
    }
}

@Composable
fun MissingPatternPlaceholder(pattern: MovementPattern) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "LOG A ${pattern.name} WORKOUT TO SEE STRENGTH DATA",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileInfoCard(profile: UserProfile, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Color.Gray)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(if (profile.name.isEmpty()) "UNNAMED ATHLETE" else profile.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Text("${profile.bodyweightKg}kg • ${profile.experienceLevel}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = Color(0xFFFF851B))
            }
        }
    }
}

@Composable
fun NoDataPlaceholder() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Analytics, null, modifier = Modifier.size(48.dp), tint = Color.DarkGray)
        Spacer(Modifier.height(16.dp))
        Text("NO TRAINING DATA", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text("Log your first workout to compute strength scores.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun EditProfileDialog(profile: UserProfile, onDismiss: () -> Unit, onSave: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var weight by remember { mutableStateOf(profile.bodyweightKg.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onSave(profile.copy(name = name, bodyweightKg = weight.toFloatOrNull() ?: 0f)) }) {
                Text("SAVE")
            }
        },
        title = { Text("EDIT PROFILE") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Bodyweight (kg)") })
            }
        }
    )
}
