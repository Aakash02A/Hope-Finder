package com.tryout.hopefinder.ui.theme

import android.app.Activity
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

/**
 * Rescue Radar System Dark Color Scheme
 * Tactical, high-contrast theme for emergency operations
 */
private val RescueRadarDarkScheme = darkColorScheme(
    primary = RadarGreen,
    onPrimary = BackgroundDark,
    primaryContainer = RadarGreenMuted,
    onPrimaryContainer = RadarGreenLight,
    
    secondary = TacticalBlue,
    onSecondary = Color.White,
    secondaryContainer = TacticalBlueDark,
    onSecondaryContainer = TacticalBlueLight,
    
    tertiary = AlertYellow,
    onTertiary = BackgroundDark,
    tertiaryContainer = AlertYellowDark,
    onTertiaryContainer = AlertYellow,
    
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedDark,
    onErrorContainer = Color.White,
    
    background = BackgroundDark,
    onBackground = TextPrimary,
    
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = TextMuted,
    outlineVariant = Color(0xFF2A2A2A)
)

/**
 * Light color scheme (for accessibility compliance)
 * Though the app defaults to dark mode for tactical visibility
 */
private val RescueRadarLightScheme = lightColorScheme(
    primary = RadarGreenDark,
    onPrimary = Color.White,
    primaryContainer = RadarGreenLight,
    onPrimaryContainer = RadarGreenMuted,
    
    secondary = TacticalBlueDark,
    onSecondary = Color.White,
    secondaryContainer = TacticalBlueLight,
    onSecondaryContainer = TacticalBlueDark,
    
    tertiary = AlertYellowDark,
    onTertiary = Color.Black,
    
    error = AlertRedDark,
    onError = Color.White,
    
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A)
)

/**
 * Rescue Radar System Theme
 * 
 * @param darkTheme Force dark theme (defaults to true for tactical visibility)
 * @param content The composable content
 */
@Composable
fun RescueRadarTheme(
    darkTheme: Boolean = true, // Default to dark theme for tactical UI
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RescueRadarDarkScheme else RescueRadarLightScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Legacy theme for backward compatibility
 */
@Composable
fun HopeFinderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RescueRadarTheme(
        darkTheme = darkTheme,
        content = content
    )
}