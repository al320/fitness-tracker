package com.al32.fitcheck.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.theme.EliteWhite
import com.al32.fitcheck.ui.theme.PrestigeGold

@Composable
fun XPBar(
    currentXp: Int,
    nextLevelXp: Int,
    level: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentXp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "XPProgress"
    )

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = "LEVEL $level",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentXp / $nextLevelXp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(EliteWhite.copy(alpha = 0.8f), EliteWhite)
                        )
                    )
            )
        }
    }
}

@Preview
@Composable
fun XPBarPreview() {
    FitcheckTheme {
        Box(modifier = Modifier.background(Color.Black).padding(24.dp)) {
            XPBar(currentXp = 750, nextLevelXp = 1000, level = 12)
        }
    }
}
