package com.example.practice3kormiletsv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Forest, onPrimary = Color.White,
    primaryContainer = Sage, onPrimaryContainer = Forest,
    secondary = Terracotta, secondaryContainer = Color(0xFFF4E5D8),
    background = Cream, onBackground = Ink,
    surface = Cream, onSurface = Ink,
    surfaceVariant = Color(0xFFF0F0E8), onSurfaceVariant = Muted,
    surfaceContainer = Color(0xFFF2F2E9), surfaceContainerLow = Color.White,
    outline = Color(0xFF8A958A), outlineVariant = Color(0xFFE0E5DB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB5D5B6), onPrimary = Color(0xFF173923),
    primaryContainer = Color(0xFF294C36), onPrimaryContainer = Color(0xFFD8EAD2),
    secondary = Color(0xFFE9B393),
    background = Color(0xFF171D19), onBackground = Color(0xFFE5E9DF),
    surface = Color(0xFF171D19), onSurface = Color(0xFFE5E9DF),
    surfaceVariant = Color(0xFF2A332B), onSurfaceVariant = Color(0xFFB5BFB3),
    surfaceContainer = Color(0xFF202922), surfaceContainerLow = Color(0xFF232B24),
    outlineVariant = Color(0xFF3C473C)
)

@Composable
fun Practice3KormiletsVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
