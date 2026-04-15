package com.ahmed.morseapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C27B0),
    onPrimary = Color.White,
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFD700),
    background = Color(0xFF0D0D1A),
    surface = Color(0xFF1A1A2E),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
)

@Composable
fun MorseAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
