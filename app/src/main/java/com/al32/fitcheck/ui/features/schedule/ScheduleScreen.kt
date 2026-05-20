package com.al32.fitcheck.ui.features.schedule

import androidx.compose.foundation.clickable
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
import com.al32.fitcheck.ui.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WEEKLY SCHEDULE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF851B))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val dayNames = listOf("", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                
                items(7) { index ->
                    val dayOfWeek = index + 1
                    val template = uiState.schedule[dayOfWeek]
                    val isRestDay = uiState.schedule.containsKey(dayOfWeek) && template == null
                    
                    ScheduleDayItem(
                        dayName = dayNames[dayOfWeek],
                        templateName = template?.name,
                        isRestDay = isRestDay,
                        onClick = { selectedDay = dayOfWeek }
                    )
                }
            }
        }
    }

    if (selectedDay != null) {
        DayAssignmentBottomSheet(
            templates = uiState.allTemplates,
            onDismiss = { selectedDay = null },
            onSelectTemplate = { templateId ->
                viewModel.assignTemplateToDay(selectedDay!!, templateId)
                selectedDay = null
            },
            onSetRestDay = {
                viewModel.setRestDay(selectedDay!!)
                selectedDay = null
            },
            onClear = {
                viewModel.clearDay(selectedDay!!)
                selectedDay = null
            }
        )
    }
}

@Composable
fun ScheduleDayItem(
    dayName: String,
    templateName: String?,
    isRestDay: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = dayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        templateName != null -> templateName
                        isRestDay -> "Rest Day"
                        else -> "Unassigned"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRestDay) Color(0xFF4CAF50) else Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.DarkGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayAssignmentBottomSheet(
    templates: List<WorkoutTemplate>,
    onDismiss: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onSetRestDay: () -> Unit,
    onClear: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111),
        contentColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            item {
                Text(
                    "ASSIGN ROUTINE",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
            }
            
            items(templates) { template ->
                ListItem(
                    headlineContent = { Text(template.name, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFFFF851B)) },
                    modifier = Modifier.clickable { onSelectTemplate(template.id) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            
            item { HorizontalDivider(color = Color.DarkGray) }
            
            item {
                ListItem(
                    headlineContent = { Text("Set as Rest Day", color = Color(0xFF4CAF50)) },
                    leadingContent = { Icon(Icons.Default.Bedtime, null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.clickable(onClick = onSetRestDay),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Clear Assignment", color = Color.Red) },
                    leadingContent = { Icon(Icons.Default.Clear, null, tint = Color.Red) },
                    modifier = Modifier.clickable(onClick = onClear),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
