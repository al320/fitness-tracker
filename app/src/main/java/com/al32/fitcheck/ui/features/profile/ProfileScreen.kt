package com.al32.fitcheck.ui.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.al32.fitcheck.data.local.entities.UserStatsEntity
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.EliteWhite
import com.al32.fitcheck.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    ProfileScreenContent(userStats)
}

@Composable
fun ProfileScreenContent(userStats: UserStatsEntity) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(Modifier.height(48.dp))
            AthleteHeader(userStats)
        }
        
        item {
            StrengthScoreSection()
        }
        
        item {
            AthleteMetricsGrid(userStats)
        }
        
        item {
            PerformanceRanksSection()
        }
    }
}

@Composable
fun AthleteHeader(userStats: UserStatsEntity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(56.dp), tint = Color.Gray)
        }
        Spacer(Modifier.height(16.dp))
        Text("ATHLETE IDENTITY", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Text("CHAMPION", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("TIER ${userStats.level} ATHLETE", style = MaterialTheme.typography.titleMedium, color = EliteWhite, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StrengthScoreSection() {
    Surface(
        color = Color.DarkGray.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("STRENGTH SCORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScoreBar("PUSH", 68)
                ScoreBar("PULL", 42)
                ScoreBar("LEGS", 85)
            }
        }
    }
}

@Composable
fun ScoreBar(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { value / 100f },
                modifier = Modifier.fillMaxSize(),
                color = EliteWhite,
                strokeWidth = 4.dp,
                trackColor = Color.DarkGray.copy(alpha = 0.3f),
            )
            Text("$value", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AthleteMetricsGrid(userStats: UserStatsEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("LIFETIME VOLUME", String.format(java.util.Locale.getDefault(), "%.1fM kg", userStats.totalXp / 1000000.0), Modifier.weight(1f))
            MetricCard("TOTAL SESSIONS", userStats.totalWorkouts.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PerformanceRanksSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("PERFORMANCE RANKS", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        RankItem("BENCH PRESS", "ADVANCED")
        RankItem("DEADLIFT", "INTERMEDIATE")
        RankItem("SQUAT", "ELITE")
    }
}

@Composable
fun RankItem(name: String, rank: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Surface(
            color = if (rank == "ELITE") Color(0xFFD4AF37).copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(rank, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
        }
    }
}

@Preview
@Composable
fun ProfilePreview() {
    FitcheckTheme {
        ProfileScreenContent(
            userStats = UserStatsEntity(
                level = 15,
                totalXp = 1450000,
                currentStreak = 8,
                totalWorkouts = 120
            )
        )
    }
}
