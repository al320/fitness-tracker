package com.al32.fitcheck.ui.features.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.R
import com.al32.fitcheck.ui.utils.formatElapsedTime
import com.al32.fitcheck.ui.viewmodel.AnalyticsUiState
import com.al32.fitcheck.ui.viewmodel.AnalyticsViewModel
import com.al32.fitcheck.ui.viewmodel.VolumeRange
import com.al32.fitcheck.ui.viewmodel.WorkoutSessionWithDetails
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, onViewHistory: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("OVERVIEW", "HISTORY")

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("PROGRESS ANALYTICS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        IconButton(onClick = onViewHistory) {
                            Icon(Icons.Default.History, null, tint = Color.Gray)
                        }
                    }
                )
                @OptIn(ExperimentalMaterial3Api::class)
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFF851B),
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> AnalyticsOverview(uiState, onRangeChange = { viewModel.setRange(it) })
                1 -> HistoryTab(uiState.sessionHistory)
            }
        }
    }
}

@Composable
fun AnalyticsOverview(uiState: AnalyticsUiState, onRangeChange: (VolumeRange) -> Unit) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val xLabels = remember(uiState.volumeHistory) { uiState.volumeHistory.map { it.first } }
    
    LaunchedEffect(uiState.volumeHistory) {
        if (uiState.hasEnoughDataForChart) {
            modelProducer.runTransaction {
                lineSeries {
                    series(uiState.volumeHistory.map { it.second })
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL VOLUME PER SESSION", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                        RangeToggle(selectedRange = uiState.selectedRange, onRangeChange = onRangeChange)
                    }
                    Spacer(Modifier.height(20.dp))
                    
                    if (!uiState.hasEnoughDataForChart) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Analytics, null, tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(stringResource(R.string.chart_empty_state), color = Color.DarkGray, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    } else {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(
                                    valueFormatter = { _, v, _ -> formatVolume(v) }
                                ),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = { _, v, _ -> xLabels.getOrNull(v.toInt()) ?: "" }
                                )
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("KEY METRICS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalyticsStatCard("ALL-TIME VOLUME", formatVolume(uiState.totalVolume), Modifier.weight(1f))
                    AnalyticsStatCard("COMPLETED", uiState.totalWorkouts.toString(), Modifier.weight(1f))
                }
                val avgVol = if (uiState.totalWorkouts > 0) uiState.totalVolume / uiState.totalWorkouts else 0.0
                AnalyticsStatCard("AVG. VOLUME / SESSION", formatVolume(avgVol))
            }
        }
        
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun RangeToggle(selectedRange: VolumeRange, onRangeChange: (VolumeRange) -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        VolumeRange.entries.forEach { range ->
            val isSelected = selectedRange == range
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) Color(0xFFFF851B) else Color.Transparent)
                    .clickable { onRangeChange(range) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range.name.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatVolume(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format(Locale.getDefault(), "%.2fM KG", value / 1_000_000.0)
        value >= 1_000 -> String.format(Locale.getDefault(), "%.2fK KG", value / 1_000.0)
        else -> String.format(Locale.getDefault(), "%.0f KG", value)
    }
}

@Composable
fun HistoryTab(sessions: List<WorkoutSessionWithDetails>) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No workout history yet", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }
        items(sessions, key = { it.session.id }) { item ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.session.name.uppercase(), color = Color.White,
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                            Text(java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(java.util.Date(item.session.startTime)), color = Color.Gray,
                                style = MaterialTheme.typography.labelSmall)
                        }
                        Text("${item.totalVolumeKg.toInt()} kg",
                            color = Color(0xFFFF851B),
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(12.dp))
                        Text(formatElapsedTime(item.durationMs / 1000), color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall)
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null, tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            item.exercises.forEach { exercise ->
                                Text(exercise.exerciseName.uppercase(), color = Color.White,
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                exercise.sets.filter { it.isCompleted }.forEach { set ->
                                    Text("  ${set.weight}kg × ${set.reps}",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}
