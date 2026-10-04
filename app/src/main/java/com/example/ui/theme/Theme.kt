package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandGold,
    onSecondary = Color.White,
    secondaryContainer = BrandGoldLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = BrandOk,
    onTertiary = Color.White,
    tertiaryContainer = BrandOkLight,
    onTertiaryContainer = Emerald700,
    background = BrandPaper,
    onBackground = BrandInk,
    surface = Color.White,
    onSurface = BrandInk,
    surfaceVariant = Color(0xFFF2F6FC),
    onSurfaceVariant = BrandMuted,
    outline = BrandLine,
    outlineVariant = Color(0xFFDDE5F0),
    error = BrandErr,
    onError = Color.White,
    errorContainer = BrandErrLight,
    onErrorContainer = Rose700
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7BAAF7),
    onPrimary = Color(0xFF04275E),
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5A2A04),
    onSecondaryContainer = BrandGoldLight,
    tertiary = Color(0xFF4ADE80),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF0D5328),
    onTertiaryContainer = BrandOkLight,
    background = Color(0xFF0B1626),
    onBackground = Color(0xFFF0F4FC),
    surface = Color(0xFF132034),
    onSurface = Color(0xFFF0F4FC),
    surfaceVariant = Color(0xFF1B2C46),
    onSurfaceVariant = Color(0xFFA6B7D0),
    outline = Color(0xFF2C4164),
    outlineVariant = Color(0xFF3B547E),
    error = Color(0xFFF87171),
    onError = Color.White,
    errorContainer = Color(0xFF5E0B0B),
    onErrorContainer = BrandErrLight
)

@Composable
fun CoopSocietyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
