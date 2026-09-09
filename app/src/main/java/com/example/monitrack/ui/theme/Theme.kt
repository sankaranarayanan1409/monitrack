package com.example.monitrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = IndigoDeep,
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = AmberDeep,
    tertiary = Emerald,
    tertiaryContainer = EmeraldLight,
    background = SlateBackground,
    onBackground = SlateInk,
    surface = SlateSurface,
    onSurface = SlateInk,
    surfaceVariant = SlateVariant,
    onSurfaceVariant = SlateInkMuted,
)

private val DarkColors = darkColorScheme(
    primary = IndigoLight,
    onPrimary = IndigoDeep,
    primaryContainer = Indigo,
    onPrimaryContainer = Color.White,
    secondary = AmberLight,
    onSecondary = AmberDeep,
    tertiary = Emerald,
    background = SlateDeep,
    onBackground = SlateOnDark,
    surface = SlateDark,
    onSurface = SlateOnDark,
    surfaceVariant = SlateMid,
    onSurfaceVariant = Color(0xFFCBD5E1),
)

@Composable
fun MonitrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color is intentionally disabled so the icon-derived palette always applies.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}

/** The accent color assigned to each tracked activity. */
fun accentFor(type: String): Color = when (type) {
    "Sleep"-> SleepAccent
    "Work" -> WorkAccent
    "Exercise" -> ExerciseAccent
    else -> DefaultAccent
}
