package com.al32.fitcheck.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.WeeklyScheduleDay
import com.al32.fitcheck.data.local.entities.WorkoutTemplate
import com.al32.fitcheck.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScheduleScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scheduleState by viewModel.repository.weeklySchedule.collectAsState(initial = emptyList())
    
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WEEKLY PLAN", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Your Training Schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text("Assign workouts or rest days to each day of the week.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
            }

            items(7) { index ->
                val dayNum = index + 1
                val assigned = scheduleState.find { it.dayOfWeek == dayNum }
                val template = uiState.templates.find { it.id == assigned?.templateId }
                
                ScheduleDayRow(
                    dayName = days[index],
                    assignment = template?.name ?: if (assigned?.isRestDay == true) "Rest Day" else "Not set",
                    isRestDay = assigned?.isRestDay == true,
                    onClick = { selectedDay = dayNum }
                )
            }
        }
    }

    if (selectedDay != null) {
        TemplatePickerBottomSheet(
            templates = uiState.templates,
            onDismiss = { selectedDay = null },
            onSelect = { templateId ->
                viewModel.setScheduleDay(selectedDay!!, templateId)
                selectedDay = null
            }
        )
    }
}

@Composable
fun ScheduleDayRow(
    dayName: String,
    assignment: String,
    isRestDay: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(dayName.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(assignment, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (isRestDay) Color.Gray else Color.White)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePickerBottomSheet(
    templates: List<WorkoutTemplate>,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF111111)) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("SELECT ROUTINE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 16.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text("Rest Day", fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.Bedtime, null, tint = Color.Gray) },
                    modifier = Modifier.clickable { onSelect(null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            items(templates.size) { index ->
                val template = templates[index]
                ListItem(
                    headlineContent = { Text(template.name, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFFFF851B)) },
                    modifier = Modifier.clickable { onSelect(template.id) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
