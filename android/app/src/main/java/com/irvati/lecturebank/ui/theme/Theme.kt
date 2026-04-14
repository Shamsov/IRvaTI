package com.irvati.lecturebank.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Светлая цветовая схема
private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = SurfaceLight,
    primaryContainer = Indigo100,
    onPrimaryContainer = Indigo900,
    secondary = Blue600,
    onSecondary = SurfaceLight,
    secondaryContainer = Blue200,
    onSecondaryContainer = Blue800,
    tertiary = DeepPurple400,
    onTertiary = SurfaceLight,
    background = SurfaceLight,
    onBackground = Indigo900,
    surface = SurfaceLight,
    onSurface = Indigo900,
    surfaceVariant = Indigo50,
    onSurfaceVariant = Indigo700,
    error = ErrorRed,
    onError = SurfaceLight
)

// Тёмная цветовая схема
private val DarkColorScheme = darkColorScheme(
    primary = Indigo200,
    onPrimary = Indigo900,
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo100,
    secondary = Blue200,
    onSecondary = Blue800,
    secondaryContainer = Blue800,
    onSecondaryContainer = Blue200,
    tertiary = DeepPurple200,
    onTertiary = SurfaceDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Indigo800,
    onSurfaceVariant = Indigo100,
    error = ErrorRedDark,
    onError = SurfaceDark
)

// Главная тема приложения «Банк лекций»
@Composable
fun LectureBankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
