package com.al32.fitcheck.ui.features.exercise_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.viewmodel.ExerciseDetailUiState
import com.al32.fitcheck.ui.viewmodel.ExerciseDetailViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(viewModel: ExerciseDetailViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.exercise?.name?.uppercase() ?: "DETAIL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        ExerciseDetailContent(uiState, Modifier.padding(padding))
    }
}

@Composable
fun ExerciseDetailContent(uiState: ExerciseDetailUiState, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(uiState.history) {
        if (uiState.history.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(uiState.history.map { it.weight.toDouble() }.reversed())
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("ESTIMATED 1RM PROGRESSION", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    CartesianChartHost(
                        chart = rememberCartesianChart(rememberLineCartesianLayer()),
                        modelProducer = modelProducer,
                        modifier = Modifier.height(180.dp)
                    )
                }
            }
        }

        item {
            Text("SESSION HISTORY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
        }

        items(uiState.history) { set ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${set.weight}kg x ${set.reps}", style = MaterialTheme.typography.bodyLarge)
                Text("Performance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}
