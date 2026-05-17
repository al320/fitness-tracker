package com.al32.fitcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val CinematicColorScheme = darkColorScheme(
    primary = EliteWhite,
    onPrimary = AmoledBlack,
    secondary = MutedCyan,
    onSecondary = EliteWhite,
    tertiary = PrestigeGold,
    onTertiary = AmoledBlack,
    background = AmoledBlack,
    onBackground = HighEmphasis,
    surface = Onyx,
    onSurface = HighEmphasis,
    surfaceVariant = Gunmetal,
    onSurfaceVariant = MediumEmphasis,
    outline = LowEmphasis
)

@Composable
fun FitcheckTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = CinematicColorScheme,
            typography = Typography,
            content = content
        )
    }
}
