package com.personal.financeapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary              = Forest,
    onPrimary            = Paper,
    primaryContainer     = ForestSoft,
    onPrimaryContainer   = Forest,
    secondary            = Terra,
    onSecondary          = Paper,
    secondaryContainer   = TerraSoft,
    onSecondaryContainer = Terra,
    tertiary             = GoldTone,
    onTertiary           = Paper,
    error                = Crimson,
    onError              = Paper,
    errorContainer       = TerraSoft,
    onErrorContainer     = Crimson,
    background           = Cream,
    onBackground         = Ink,
    surface              = Paper,
    onSurface            = Ink,
    surfaceVariant       = Color(0xFFEDE8DF),
    onSurfaceVariant     = Ink3,
    outline              = Color(0x1A1F2218),
    outlineVariant       = Color(0x0F1F2218),
    inverseSurface       = Ink,
    inverseOnSurface     = Paper,
    surfaceContainer     = Paper,
    surfaceContainerLow  = Cream,
    surfaceContainerHigh = Color(0xFFEDE8DF),
)

private val DarkScheme = darkColorScheme(
    primary              = DarkForest,
    onPrimary            = DarkBg,
    primaryContainer     = DarkForestSoft,
    onPrimaryContainer   = DarkForest,
    secondary            = DarkTerra,
    onSecondary          = DarkBg,
    secondaryContainer   = DarkTerraSoft,
    onSecondaryContainer = DarkTerra,
    tertiary             = Color(0xFFD4A847),
    onTertiary           = DarkBg,
    error                = DarkCrimson,
    onError              = DarkBg,
    errorContainer       = DarkTerraSoft,
    onErrorContainer     = DarkCrimson,
    background           = DarkBg,
    onBackground         = DarkInk,
    surface              = DarkPaper,
    onSurface            = DarkInk,
    surfaceVariant       = Color(0xFF262A23),
    onSurfaceVariant     = DarkInk3,
    outline              = Color(0x1AE6E3D8),
    outlineVariant       = Color(0x0AE6E3D8),
    inverseSurface       = DarkInk,
    inverseOnSurface     = DarkBg,
    surfaceContainer     = DarkPaper,
    surfaceContainerLow  = DarkBg,
)

@Composable
fun FinanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
