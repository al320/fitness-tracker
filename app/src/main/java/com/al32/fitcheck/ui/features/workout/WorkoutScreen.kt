package com.al32.fitcheck.ui.features.workout

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al32.fitcheck.data.local.entities.ExerciseEntity
import com.al32.fitcheck.data.local.entities.SetEntity
import com.al32.fitcheck.ui.theme.*
import com.al32.fitcheck.ui.viewmodel.ExerciseWithSets
import com.al32.fitcheck.ui.viewmodel.WorkoutUiState
import com.al32.fitcheck.ui.viewmodel.WorkoutViewModel
import java.util.Locale

@Composable
fun WorkoutScreen(
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    viewModel: WorkoutViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    WorkoutContent(
        uiState = uiState,
        onFinish = {
            viewModel.finishWorkout()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFinish()
        },
        onCancel = onCancel,
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSet,
        onOpenPicker = { viewModel.toggleExercisePicker(true) },
        onClosePicker = { viewModel.toggleExercisePicker(false) },
        onSearch = viewModel::searchExercises,
        onSelect = viewModel::addExercise,
        onRemoveExercise = viewModel::removeExercise,
        onSkipRest = viewModel::skipRestTimer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutContent(
    uiState: WorkoutUiState,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    onAddSet: (Long) -> Unit,
    onUpdateSet: (Long, Double, Int, Boolean) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (ExerciseEntity) -> Unit,
    onRemoveExercise: (Long) -> Unit,
    onSkipRest: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            WorkoutTopBar(
                timer = formatElapsedTime(uiState.elapsedTime),
                onCancel = onCancel,
                onFinish = onFinish
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    WorkoutStatsRow(uiState)
                }

                itemsIndexed(uiState.exercises, key = { _, item -> item.exercise.id }) { _, section ->
                    WorkoutExerciseSection(
                        section = section,
                        onAddSet = { onAddSet(section.exercise.id) },
                        onUpdateSet = onUpdateSet,
                        onRemove = { onRemoveExercise(section.exercise.id) }
                    )
                }

                item {
                    AddExerciseAction(onOpenPicker)
                }

                item { Spacer(Modifier.height(100.dp)) }
            }

            if (uiState.isExercisePickerOpen) {
                ExercisePickerSheet(onClosePicker, onSearch, uiState.searchResults, onSelect)
            }

            uiState.restTimerSeconds?.let {
                RecoveryTimerBar(it, onSkipRest)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTopBar(timer: String, onCancel: () -> Unit, onFinish: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SESSION", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Spacer(Modifier.width(12.dp))
                Text(timer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = EliteWhite)
            }
        },
        navigationIcon = {
            IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
        },
        actions = {
            TextButton(onClick = onFinish) {
                Text("FINISH", fontWeight = FontWeight.Black, color = Color(0xFF00ACC1))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AmoledBlack)
    )
}

@Composable
fun WorkoutStatsRow(uiState: WorkoutUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StatItem("VOLUME", String.format(Locale.getDefault(), "%.0fkg", uiState.totalVolume))
        StatItem("SETS", uiState.totalSets.toString())
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
fun WorkoutExerciseSection(
    section: ExerciseWithSets,
    onAddSet: () -> Unit,
    onUpdateSet: (Long, Double, Int, Boolean) -> Unit,
    onRemove: () -> Unit
) {
    val lastSet = section.previousPerformance.firstOrNull()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(section.exercise.name.uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
                if (lastSet != null) {
                    Text("PREV: ${lastSet.weight}kg x ${lastSet.reps}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Default.MoreVert, null, tint = Color.DarkGray) }
        }

        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("SET", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, textAlign = TextAlign.Center)
            Text("PREV", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, textAlign = TextAlign.Center)
            Text("KG", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, textAlign = TextAlign.Center)
            Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, textAlign = TextAlign.Center)
            Spacer(Modifier.width(44.dp))
        }

        section.sets.forEachIndexed { index, set ->
            WorkoutSetTableTile(
                index = index + 1,
                set = set,
                onUpdate = { w, r, c -> onUpdateSet(set.id, w, r, c) }
            )
        }

        TextButton(onClick = onAddSet, modifier = Modifier.padding(horizontal = 12.dp)) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(Modifier.width(6.dp))
            Text("ADD SET", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
        
        HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))
    }
}

@Composable
fun WorkoutSetTableTile(index: Int, set: SetEntity, onUpdate: (Double, Int, Boolean) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var weightText by remember(set.id) { mutableStateOf(if (set.weight == 0.0) "" else set.weight.toString()) }
    var repsText by remember(set.id) { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp)
            .background(if (set.isCompleted) Color(0xFF00ACC1).copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(index.toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (set.isCompleted) Color(0xFF00ACC1) else Color.Gray)
        
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text("-", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }

        TacticalInput(
            value = weightText,
            onValueChange = { weightText = it; it.toDoubleOrNull()?.let { w -> onUpdate(w, repsText.toIntOrNull() ?: 0, set.isCompleted) } },
            modifier = Modifier.weight(1f)
        )

        TacticalInput(
            value = repsText,
            onValueChange = { repsText = it; it.toIntOrNull()?.let { r -> onUpdate(weightText.toDoubleOrNull() ?: 0.0, r, set.isCompleted) } },
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                val w = weightText.toDoubleOrNull() ?: 0.0
                val r = repsText.toIntOrNull() ?: 0
                if (!set.isCompleted) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onUpdate(w, r, !set.isCompleted)
            },
            modifier = Modifier.size(44.dp).background(if (set.isCompleted) Color(0xFF00ACC1) else Gunmetal, RoundedCornerShape(4.dp))
        ) {
            Icon(Icons.Default.Check, null, tint = if (set.isCompleted) Color.Black else Color.DarkGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun TacticalInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(34.dp).background(Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(4.dp),
        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true,
        cursorBrush = SolidColor(EliteWhite)
    )
}

@Composable
fun AddExerciseAction(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.DarkGray)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("ADD EXERCISE", style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(onDismiss: () -> Unit, onSearch: (String) -> Unit, results: List<ExerciseEntity>, onSelect: (ExerciseEntity) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Onyx, dragHandle = null) {
        Column(modifier = Modifier.fillMaxHeight(0.85f).padding(16.dp)) {
            var query by remember { mutableStateOf("") }
            TextField(value = query, onValueChange = { query = it; onSearch(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search Movements...") }, colors = TextFieldDefaults.colors(focusedContainerColor = Gunmetal, unfocusedContainerColor = Gunmetal))
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                itemsIndexed(results) { _, exercise ->
                    ListItem(headlineContent = { Text(exercise.name.uppercase(), fontWeight = FontWeight.Bold) }, supportingContent = { Text(exercise.muscleGroup) }, modifier = Modifier.clickable { onSelect(exercise) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                }
            }
        }
    }
}

@Composable
fun RecoveryTimerBar(seconds: Int, onSkip: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(color = Onyx, border = BorderStroke(1.dp, Color(0xFF00ACC1).copy(alpha = 0.4f)), shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = Color(0xFF00ACC1), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(12.dp))
                Text(String.format(Locale.getDefault(), "RECOVERY: %02d:%02d", seconds / 60, seconds % 60), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(16.dp))
                Text("SKIP", modifier = Modifier.clickable(onClick = onSkip), color = Color(0xFF00ACC1), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
            }
        }
    }
}

fun formatElapsedTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%02d:%02d", m, s)
}
