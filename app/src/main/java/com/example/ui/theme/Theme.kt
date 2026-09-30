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

private val BlackBookColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = TextInverse,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = GoldAccent,
    secondary = EmeraldGreen,
    onSecondary = TextInverse,
    secondaryContainer = EmeraldDark,
    onSecondaryContainer = EmeraldGreen,
    tertiary = CrimsonRed,
    onTertiary = TextPrimary,
    tertiaryContainer = CrimsonDark,
    onTertiaryContainer = CrimsonRed,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderLight
)

@Composable
fun BlackBookTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkBackground.toArgb()
                window.navigationBarColor = DarkBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = BlackBookColorScheme,
        typography = Typography,
        content = content
    )
}
