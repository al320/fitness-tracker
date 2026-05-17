package com.al32.fitcheck.ui.features.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.ui.components.BodyHeatmap
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.viewmodel.AnalyticsUiState
import com.al32.fitcheck.ui.viewmodel.AnalyticsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.util.Locale

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    AnalyticsScreenContent(uiState)
}

@Preview
@Composable
fun AnalyticsPreview() {
    FitcheckTheme {
        AnalyticsScreenContent(
            uiState = AnalyticsUiState(
                totalWorkouts = 42,
                totalVolume = 125400.0,
                muscleIntensity = mapOf("Chest" to 0.9f, "Back" to 0.4f, "Legs" to 0.7f),
                volumeHistory = listOf("Mon" to 3000.0, "Tue" to 4000.0, "Wed" to 3500.0)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreenContent(uiState: AnalyticsUiState) {
    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(uiState.volumeHistory) {
        if (uiState.volumeHistory.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(uiState.volumeHistory.map { it.second })
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PERFORMANCE ANALYTICS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "VOLUME PROGRESSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(20.dp))
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.height(200.dp)
                        )
                    }
                }
            }
            
            item {
                // Simplified legacy mapping for heatmap refactor
                BodyHeatmap(
                    muscleStates = emptyMap(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalyticsStatCard("TOTAL PERFORMANCE VOLUME", String.format(Locale.getDefault(), "%.0f KG", uiState.totalVolume), Modifier.weight(1f))
                    AnalyticsStatCard("TOTAL SESSIONS", uiState.totalWorkouts.toString(), Modifier.weight(1f))
                }
            }
            
            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }
    }
}
