package com.al32.fitcheck.ui.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
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
import com.al32.fitcheck.ui.components.XPBar
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.PrestigeGold
import com.al32.fitcheck.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val userStats by viewModel.userStats.collectAsState()
    ProfileScreenContent(userStats)
}

@Preview
@Composable
fun ProfilePreview() {
    FitcheckTheme {
        ProfileScreenContent(
            userStats = UserStatsEntity(
                level = 15,
                totalXp = 14500,
                currentStreak = 8,
                totalWorkouts = 120
            )
        )
    }
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
            ProfileHeader(level = userStats.level)
        }
        
        item {
            XPBar(
                currentXp = userStats.totalXp.toInt() % 1000,
                nextLevelXp = 1000,
                level = userStats.level,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfileStatCard("ATHLETIC STREAK", userStats.currentStreak.toString(), Icons.Default.LocalFireDepartment, Color(0xFFFF5722), Modifier.weight(1f))
                ProfileStatCard("TOTAL SESSIONS", userStats.totalWorkouts.toString(), Icons.Default.EmojiEvents, PrestigeGold, Modifier.weight(1f))
            }
        }
        
        item {
            AchievementSection()
        }
    }
}

@Composable
fun ProfileHeader(level: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(110.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(20.dp))
        Text("ATHLETIC IDENTITY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("CHAMPION", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text("TIER $level ELITE", style = MaterialTheme.typography.titleMedium, color = PrestigeGold, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("PRESTIGE BADGES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BadgeItem("7 DAY STREAK")
            BadgeItem("1000KG CLUB")
            BadgeItem("NIGHT OWL")
            BadgeItem("EARLY RISER")
        }
    }
}

@Composable
fun BadgeItem(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
    }
}
