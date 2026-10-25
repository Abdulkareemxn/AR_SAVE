package com.arsave.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = CharcoalDark,
    primaryContainer = CharcoalSurfaceVariant,
    onPrimaryContainer = TextPrimaryDark,
    secondary = ElectricBlue,
    onSecondary = CharcoalDark,
    background = CharcoalDark,
    onBackground = TextPrimaryDark,
    surface = CharcoalCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = CharcoalCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = CardLight,
    primaryContainer = Color(0xFFDDF4FF),
    onPrimaryContainer = Color(0xFF0969DA),
    secondary = ElectricBlueHover,
    onSecondary = CardLight,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = CardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEAEFF5),
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

@Composable
fun ArSaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
