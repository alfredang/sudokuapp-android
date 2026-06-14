package com.tertiaryinfotech.sudokuapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Accent matches the iOS AccentColor (system blue).
private val AccentLight = Color(0xFF2973F5)
private val AccentDark = Color(0xFF619EFA)

// iOS-style grouped backgrounds.
val GroupedBackgroundLight = Color(0xFFF2F2F7)
val SecondaryGroupedLight = Color(0xFFFFFFFF)
val GroupedBackgroundDark = Color(0xFF000000)
val SecondaryGroupedDark = Color(0xFF1C1C1E)

private val LightColors = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    background = GroupedBackgroundLight,
    onBackground = Color(0xFF11181C),
    surface = SecondaryGroupedLight,
    onSurface = Color(0xFF11181C),
    surfaceVariant = Color(0xFFE9E9EF),
    onSurfaceVariant = Color(0xFF6B6B70),
    error = Color(0xFFFF3B30),
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color.Black,
    background = GroupedBackgroundDark,
    onBackground = Color(0xFFECEDEE),
    surface = SecondaryGroupedDark,
    onSurface = Color(0xFFECEDEE),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF9E9EA3),
    error = Color(0xFFFF453A),
)

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val light = colors.background.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = light
                isAppearanceLightNavigationBars = light
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
