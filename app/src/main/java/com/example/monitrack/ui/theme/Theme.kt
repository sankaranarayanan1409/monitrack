package com.example.monitrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = SoftPeach,
    onPrimaryContainer = DeepClay,
    secondary = WarmAmber,
    onSecondary = Color.White,
    secondaryContainer = WarmAmberLight,
    onSecondaryContainer = AmberDeep,
    tertiary = Sage,
    tertiaryContainer = SageLight,
    background = Cream,
    onBackground = WarmBrown,
    surface = CreamSurface,
    onSurface = WarmBrown,
    surfaceVariant = SandVariant,
    onSurfaceVariant = WarmGrey,
)

private val DarkColors = darkColorScheme(
    primary = TerracottaLight,
    onPrimary = DeepClay,
    primaryContainer = Terracotta,
    onPrimaryContainer = SoftPeach,
    secondary = WarmAmberLight,
    onSecondary = AmberDeep,
    tertiary = Sage,
    background = DarkBackground,
    onBackground = CreamOnDark,
    surface = DarkSurface,
    onSurface = CreamOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SoftPeach,
)

@Composable
fun MonitrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color is intentionally disabled so the curated warm palette always applies.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

/** The warm accent color assigned to each tracked activity. */
fun accentFor(type: String): Color = when (type) {
    "Sleep"-> SleepAccent
    "Work" -> WorkAccent
    "Exercise" -> ExerciseAccent
    else -> DefaultAccent
}
