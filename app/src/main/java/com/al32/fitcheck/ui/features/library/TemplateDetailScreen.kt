package com.al32.fitcheck.ui.features.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    viewModel: TemplateViewModel,
    onLoadIntoSession: (WorkoutTemplate) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val template = uiState.selectedTemplate ?: return

    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(template.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Default.Edit, null, tint = Color.Gray)
                    }
                    IconButton(onClick = { 
                        viewModel.deleteTemplate(template)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (template.description.isNotEmpty()) {
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item {
                    Text(
                        "EXERCISES (${uiState.exercisesForSelected.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Black
                    )
                }

                if (uiState.exercisesForSelected.isEmpty()) {
                    item {
                        Text("No exercises added yet", color = Color.DarkGray, modifier = Modifier.padding(vertical = 32.dp))
                    }
                } else {
                    items(uiState.exercisesForSelected) { exercise ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    exercise.exerciseId.replace("_", " ").uppercase(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${exercise.targetSets} sets × ${exercise.targetReps} reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Button(
                    onClick = { onLoadIntoSession(template) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text("START WORKOUT", fontWeight = FontWeight.Black)
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameTemplateDialog(
            currentName = template.name,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                viewModel.renameTemplate(template, newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
fun RenameTemplateDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("RENAME ROUTINE", fontWeight = FontWeight.Black) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("New Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onRename(name) }) {
                Text("SAVE", color = Color(0xFFFF851B))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF111111),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
