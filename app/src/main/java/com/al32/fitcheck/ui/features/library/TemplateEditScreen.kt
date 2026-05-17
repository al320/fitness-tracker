package com.al32.fitcheck.ui.features.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.ui.features.workout.ExercisePickerSheet
import com.al32.fitcheck.ui.theme.AmoledBlack
import com.al32.fitcheck.ui.theme.EliteWhite
import com.al32.fitcheck.ui.theme.Gunmetal
import com.al32.fitcheck.ui.viewmodel.WorkoutUiState
import com.al32.fitcheck.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var templateName by remember(uiState.workoutId) { mutableStateOf("") }
    
    // Initial name load if editing existing
    // For now we use the ViewModel's active workout as the edit target
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    TextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        placeholder = { Text("TEMPLATE NAME", style = MaterialTheme.typography.titleMedium) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        viewModel.saveAsTemplate(templateName)
                        onBack()
                    }) {
                        Text("SAVE", fontWeight = FontWeight.Black, color = EliteWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                item { Spacer(Modifier.height(16.dp)) }
                
                itemsIndexed(uiState.exercises) { index, exerciseWithSets ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Gunmetal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}", modifier = Modifier.width(24.dp), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                            Text(exerciseWithSets.exercise.name.uppercase(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeExercise(exerciseWithSets.exercise.id) }) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.toggleExercisePicker(true) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gunmetal)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD EXERCISE")
                    }
                }
            }

            if (uiState.isExercisePickerOpen) {
                ExercisePickerSheet(
                    onDismiss = { viewModel.toggleExercisePicker(false) },
                    onSearch = { viewModel.searchExercises(it) },
                    results = uiState.searchResults,
                    onSelect = { viewModel.addExercise(it) }
                )
            }
        }
    }
}
