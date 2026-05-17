package com.al32.fitcheck.ui.features.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.Exercise
import com.al32.fitcheck.domain.physiology.CNSLoad
import com.al32.fitcheck.domain.physiology.MovementPattern
import com.al32.fitcheck.domain.physiology.MuscleGroup
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCustomExerciseScreen(
    onSave: (Exercise) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(MovementPattern.ISOLATION) }
    val primaryMuscles = remember { mutableStateListOf<MuscleGroup>() }
    val secondaryMuscles = remember { mutableStateListOf<MuscleGroup>() }
    var cnsLoad by remember { mutableStateOf(CNSLoad.LOW) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CREATE EXERCISE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    Exercise(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        movementPattern = pattern,
                                        primaryMuscles = primaryMuscles.toList(),
                                        secondaryMuscles = secondaryMuscles.toList(),
                                        cnsLoad = cnsLoad,
                                        isCustom = true
                                    )
                                )
                            }
                        }
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Black, color = Color(0xFFFF851B))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("Movement Pattern", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MovementPattern.entries.forEach { p ->
                        FilterChip(
                            selected = pattern == p,
                            onClick = { pattern = p },
                            label = { Text(p.name) }
                        )
                    }
                }
            }

            item {
                Text("Primary Muscles (Max 3)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MuscleGroup.entries.forEach { m ->
                        val selected = primaryMuscles.contains(m)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) primaryMuscles.remove(m)
                                else if (primaryMuscles.size < 3) primaryMuscles.add(m)
                            },
                            label = { Text(m.name.replace("_", " ")) }
                        )
                    }
                }
            }

            item {
                Text("CNS Load", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                FlowRow(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CNSLoad.entries.forEach { load ->
                        FilterChip(
                            selected = cnsLoad == load,
                            onClick = { cnsLoad = load },
                            label = { Text(load.name) }
                        )
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
