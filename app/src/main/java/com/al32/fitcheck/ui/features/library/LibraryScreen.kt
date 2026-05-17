package com.al32.fitcheck.ui.features.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.ui.viewmodel.LibraryUiState
import com.al32.fitcheck.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onStartTemplate: (WorkoutTemplate) -> Unit,
    onEditTemplate: (WorkoutTemplate) -> Unit,
    onCreateTemplate: () -> Unit,
    onCreateExercise: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("LIBRARY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SectionHeader("ROUTINES", onCreateTemplate)
                if (uiState.templates.isEmpty()) {
                    EmptyState("No routines yet", "Create Routine", onCreateTemplate)
                }
            }

            items(uiState.templates) { template ->
                LibraryTemplateCard(
                    template = template,
                    onStart = { onStartTemplate(template) },
                    onEdit = { onEditTemplate(template) },
                    onDelete = { viewModel.deleteTemplate(template) }
                )
            }

            item {
                SectionHeader("EXERCISES", onCreateExercise)
                // Exercise list could go here or a button to manage custom ones
            }
            
            item {
                Button(
                    onClick = onCreateExercise,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.2f))
                ) {
                    Text("Create Custom Exercise", color = Color.White)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        IconButton(onClick = onAdd) {
            Icon(Icons.Default.Add, null, tint = Color(0xFFFF851B))
        }
    }
}

@Composable
fun EmptyState(text: String, actionText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAction, border = BorderStroke(1.dp, Color.DarkGray)) {
            Text(actionText, color = Color.White)
        }
    }
}

@Composable
fun LibraryTemplateCard(
    template: WorkoutTemplate,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(template.name.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text("Last performed: Never", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onStart, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B))) {
                    Text("START SESSION", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
                OutlinedButton(onClick = onDelete, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
