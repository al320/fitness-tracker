package com.al32.fitcheck.ui.features.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.R
import com.al32.fitcheck.ui.components.ExerciseStrengthCard
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.data.preferences.UserProfile
import com.al32.fitcheck.data.preferences.ExperienceLevel
import com.al32.fitcheck.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

            if (strengthScores?.missingBodyweight == true || profile.bodyweightKg <= 0f) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6B35).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFFF6B35))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6B35))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Bodyweight not set", fontWeight = FontWeight.Bold, color = Color(0xFFFF6B35))
                                Text("Tap to set your bodyweight to unlock strength standards", 
                                     style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
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
                        kgToNext = info.needsKgToReachNext,
                        lastSessionSummary = info.lastSessionSummary,
                        bodyweightKg = profile.bodyweightKg,
                        progress = info.progress
                    )
                }
                
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
        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
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
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(32.dp), tint = Color.Gray)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(profile: UserProfile, onDismiss: () -> Unit, onSave: (UserProfile) -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var weight by remember { mutableStateOf(if (profile.bodyweightKg > 0) profile.bodyweightKg.toString() else "") }
    var experience by remember { mutableStateOf(profile.experienceLevel) }
    
    val isWeightValid = weight.toFloatOrNull() != null && weight.toFloat() > 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { 
                    if (isWeightValid) {
                        onSave(profile.copy(
                            name = name, 
                            bodyweightKg = weight.toFloat(),
                            experienceLevel = experience
                        )) 
                    }
                },
                enabled = isWeightValid && name.isNotBlank()
            ) {
                Text("SAVE", color = Color(0xFFFF851B), fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        title = { Text("EDIT PROFILE", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF851B))
                )
                Column {
                    OutlinedTextField(
                        value = weight, 
                        onValueChange = { weight = it }, 
                        label = { Text("Bodyweight (kg)") },
                        singleLine = true,
                        isError = !isWeightValid && weight.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF851B)),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!isWeightValid && weight.isNotEmpty()) {
                        Text("Required for strength scoring", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Column {
                    Text("Experience Level", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExperienceLevel.entries.forEach { level ->
                            FilterChip(
                                selected = experience == level,
                                onClick = { experience = level },
                                label = { Text(level.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF851B).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFFF851B)
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF111111),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
