package com.al32.fitcheck.ui.features.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al32.fitcheck.data.local.entities.Exercise
import com.al32.fitcheck.data.local.entities.ExerciseEntry
import com.al32.fitcheck.data.local.entities.SetEntry
import com.al32.fitcheck.ui.theme.EliteWhite
import com.al32.fitcheck.ui.utils.formatElapsedTime
import com.al32.fitcheck.ui.viewmodel.ExerciseWithSets
import com.al32.fitcheck.ui.viewmodel.WorkoutUiState
import com.al32.fitcheck.ui.viewmodel.WorkoutViewModel
import java.util.Locale
import androidx.navigation.NavController

@Composable
fun WorkoutScreen(
    onFinish: (String) -> Unit,
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
            // Wait for session id to be available in state after finish? 
            // Or just navigate to summary with the current id.
            uiState.session?.id?.let { onFinish(it) }
        },
        onCancel = onCancel,
        onAddSet = viewModel::addSet,
        onUpdateSet = viewModel::updateSet,
        onOpenPicker = { viewModel.toggleExercisePicker(true) },
        onClosePicker = { viewModel.toggleExercisePicker(false) },
        onSearch = viewModel::searchExercises,
        onSelect = viewModel::addExercise,
        onRemoveExercise = viewModel::removeExercise,
        onAddRest = { viewModel.addRestTime(30) },
        onSkipRest = { viewModel.skipRest() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutContent(
    uiState: WorkoutUiState,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    onAddSet: (String) -> Unit,
    onUpdateSet: (SetEntry) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (Exercise) -> Unit,
    onRemoveExercise: (ExerciseEntry) -> Unit,
    onAddRest: () -> Unit,
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
        },
        bottomBar = {
            if (uiState.restTimerSeconds > 0) {
                BottomAppBar(
                    containerColor = Color(0xFF1A1A1A),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null,
                            tint = Color(0xFFFF851B), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rest: ${formatRestTime(uiState.restTimerSeconds)}",
                            color = Color.White, style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = onAddRest) {
                            Text("+30s", color = Color(0xFFFF851B))
                        }
                        TextButton(onClick = onSkipRest) {
                            Text("Skip", color = Color.Gray)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
                item {
                    WorkoutStatsBar(uiState)
                }

                itemsIndexed(uiState.exercises, key = { _, item -> item.entry.id }) { _, section ->
                    WorkoutExerciseSection(
                        section = section,
                        onAddSet = { onAddSet(section.entry.id) },
                        onUpdateSet = onUpdateSet,
                        onRemove = { onRemoveExercise(section.entry) }
                    )
                }

                item {
                    AddExerciseButton(onOpenPicker)
                }

                item { Spacer(Modifier.height(100.dp)) }
            }

            if (uiState.isExercisePickerOpen) {
                ExercisePickerSheet(onClosePicker, onSearch, uiState.searchResults, onSelect)
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
                Text(timer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        navigationIcon = {
            IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
        },
        actions = {
            Button(
                onClick = onFinish,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF851B)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp).padding(end = 8.dp)
            ) {
                Text("FINISH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

@Composable
fun WorkoutStatsBar(uiState: WorkoutUiState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatPill("VOLUME", String.format(Locale.getDefault(), "%.0fkg", uiState.totalVolume))
        StatPill("SETS", uiState.totalSetsCount.toString())
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
    }
}

@Composable
fun WorkoutExerciseSection(
    section: ExerciseWithSets,
    onAddSet: () -> Unit,
    onUpdateSet: (SetEntry) -> Unit,
    onRemove: () -> Unit
) {
    val lastSet = section.previousPerformance.firstOrNull()
    val maxPrevWeight = section.previousPerformance.maxOfOrNull { it.weight } ?: 0f

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
            WorkoutSetRow(index = index + 1, set = set, onUpdate = onUpdateSet, isPRAttempt = set.weight > maxPrevWeight)
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
fun WorkoutSetRow(index: Int, set: SetEntry, onUpdate: (SetEntry) -> Unit, isPRAttempt: Boolean) {
    val haptic = LocalHapticFeedback.current
    val rowColor by animateColorAsState(
        targetValue = if (set.isCompleted) Color(0xFF1A3A1A) else Color.Transparent,
        animationSpec = tween(250), label = "rowColor"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (set.isCompleted) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "checkScale"
    )
    
    Column(modifier = Modifier.fillMaxWidth().background(rowColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(index.toString(), modifier = Modifier.width(32.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (set.isCompleted) Color.Green else Color.Gray)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("-", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
            }
            SetInputBox(value = if (set.weight == 0f) "" else set.weight.toString(), onValueChange = { val w = it.toFloatOrNull() ?: 0f; onUpdate(set.copy(weight = w)) }, modifier = Modifier.weight(1f))
            SetInputBox(value = if (set.reps == 0) "" else set.reps.toString(), onValueChange = { val r = it.toIntOrNull() ?: 0; onUpdate(set.copy(reps = r)) }, modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    if (!set.isCompleted) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpdate(set.copy(isCompleted = !set.isCompleted, completedAt = System.currentTimeMillis()))
                },
                modifier = Modifier.size(44.dp).graphicsLayer(scaleX = checkScale, scaleY = checkScale).background(if (set.isCompleted) Color.Green else Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Check, null, tint = if (set.isCompleted) Color.Black else Color.DarkGray, modifier = Modifier.size(20.dp))
            }
        }
        if (isPRAttempt && !set.isCompleted) {
            Text("🏆 PR ATTEMPT",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF6D00),
                modifier = Modifier.padding(start = 60.dp, bottom = 4.dp),
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SetInputBox(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(34.dp).background(Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(4.dp),
        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp, textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true,
        cursorBrush = SolidColor(Color.White)
    )
}

@Composable
fun AddExerciseButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("ADD EXERCISE", style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(onDismiss: () -> Unit, onSearch: (String) -> Unit, results: List<Exercise>, onSelect: (Exercise) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF0D0D0D), dragHandle = null) {
        Column(modifier = Modifier.fillMaxHeight(0.85f).padding(16.dp)) {
            var query by remember { mutableStateOf("") }
            TextField(value = query, onValueChange = { query = it; onSearch(it) }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search Exercises") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.DarkGray.copy(alpha = 0.2f), unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.2f)))
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                itemsIndexed(results) { _, exercise ->
                    ListItem(headlineContent = { Text(exercise.name.uppercase(), fontWeight = FontWeight.Bold) }, supportingContent = { Text(exercise.movementPattern.name) }, modifier = Modifier.clickable { onSelect(exercise) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))
                }
            }
        }
    }
}

private fun formatRestTime(seconds: Int): String {
    return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
}
