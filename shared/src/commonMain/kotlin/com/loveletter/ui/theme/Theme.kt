package com.loveletter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Love Letter themed colors
val LoveRed = Color(0xFFD32F2F)
val LoveRedDark = Color(0xFFB71C1C)
val LoveRedLight = Color(0xFFFFCDD2)
val LovePink = Color(0xFFFFE4E1)
val LovePinkDark = Color(0xFFFFB6C1)
val RoyalGold = Color(0xFFFFD700)
val RoyalGoldDark = Color(0xFFC5A100)
val VelvetPurple = Color(0xFF6A1B9A)
val CreamWhite = Color(0xFFFFF8F5)
val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF16213E)
val CardBackground = Color(0xFFFFFBF0)

private val LightColorScheme = lightColorScheme(
    primary = LoveRed,
    onPrimary = Color.White,
    primaryContainer = LoveRedLight,
    onPrimaryContainer = LoveRedDark,
    secondary = VelvetPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1BEE7),
    onSecondaryContainer = VelvetPurple,
    tertiary = RoyalGold,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFF3CD),
    onTertiaryContainer = RoyalGoldDark,
    background = CreamWhite,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = LovePink,
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB00020),
    onError = Color.White,
    outline = Color(0xFF79747E)
)

private val DarkColorScheme = darkColorScheme(
    primary = LoveRedLight,
    onPrimary = LoveRedDark,
    primaryContainer = LoveRed,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFCE93D8),
    onSecondary = VelvetPurple,
    secondaryContainer = VelvetPurple,
    onSecondaryContainer = Color(0xFFE1BEE7),
    tertiary = RoyalGold,
    onTertiary = Color.Black,
    tertiaryContainer = RoyalGoldDark,
    onTertiaryContainer = Color(0xFFFFF3CD),
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2D4A),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFCF6679),
    onError = Color.Black,
    outline = Color(0xFF938F99)
)

@Composable
fun LoveLetterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
