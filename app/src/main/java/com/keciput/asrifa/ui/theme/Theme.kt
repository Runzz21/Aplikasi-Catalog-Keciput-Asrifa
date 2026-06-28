package com.keciput.asrifa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoralMid,
    onPrimary = Color.White,
    primaryContainer = CoralSoft,
    onPrimaryContainer = CoralDark,
    secondary = Gold,
    onSecondary = Color.White,
    tertiary = GreenWa,
    background = Cream,
    surface = Color.White,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Cream,
    onSurfaceVariant = InkMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralMid,
    onPrimary = Color.White,
    background = Color(0xFF1A110D), // Darker version of Ink for background
    surface = Color(0xFF2B1810),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun KeciputAsrifaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Force light theme for now to match the PRD's warm aesthetic 
    // or use darkTheme if you want to support it. 
    // Given the PRD focus on "Cream" and "Ink", light theme is the priority.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KeciputTypography,
        shapes = KeciputShapes,
        content = content
    )
}
