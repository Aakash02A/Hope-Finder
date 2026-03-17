package com.hope_finder.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SaasLightColorScheme = lightColorScheme(
    primary = SaasPrimary,
    onPrimary = SaasWhite,
    primaryContainer = Color(0xFFE0EAFF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF606060),
    onSecondary = SaasWhite,
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF1C1C1C),
    tertiary = Color(0xFF7D5260),
    onTertiary = SaasWhite,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31101D),
    error = SaasError,
    onError = SaasWhite,
    errorContainer = Color(0xFFFEDAD6),
    onErrorContainer = Color(0xFF410E0B),
    outline = SaasStroke,
    background = SaasBgPrimary,
    onBackground = SaasText,
    surface = SaasWhite,
    onSurface = SaasText,
    surfaceVariant = SaasBgSecondary,
    onSurfaceVariant = SaasTextSecond,
    scrim = Color(0xFF000000),
)

@Composable
fun HopeFinderTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Use clean SaaS light theme
    val colorScheme = SaasLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SaasBgPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
