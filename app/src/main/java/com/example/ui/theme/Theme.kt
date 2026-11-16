package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ApexDarkColorScheme = darkColorScheme(
    primary = ApexEmerald,
    onPrimary = ApexBackground,
    primaryContainer = ApexCard,
    onPrimaryContainer = ApexTextPrimary,
    secondary = ApexCoral,
    onSecondary = ApexTextPrimary,
    tertiary = ApexCyan,
    background = ApexBackground,
    onBackground = ApexTextPrimary,
    surface = ApexCard,
    onSurface = ApexTextPrimary,
    surfaceVariant = ApexInput,
    onSurfaceVariant = ApexTextMuted,
    outline = ApexCardBorder
)

@Composable
fun ApexFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = ApexDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ApexBackground.toArgb()
            window.navigationBarColor = ApexBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
