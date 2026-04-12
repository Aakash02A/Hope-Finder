package com.tryout.hopefinder.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Professional Emergency Response Color Scheme
// Deep, sophisticated palette inspired by mission control design
// Optimized for high-visibility rescue operations

// ============= CORE PALETTE =============
// Primary - Professional Deep Blue (Authority & Trust)
val PrimaryDeepBlue = Color(0xFF1e40af)
val PrimaryMediumBlue = Color(0xFF3b82f6)
val PrimaryLightBlue = Color(0xFF60a5fa)

// Accent - Modern Cyan/Teal (High Visibility)
val AccentCyan = Color(0xFF06b6d4)
val AccentCyanLight = Color(0xFF22d3ee)
val AccentCyanDark = Color(0xFF0891b2)

// Status Colors
val StatusCriticalRed = Color(0xFFef4444)
val StatusWarningAmber = Color(0xFFF59e0b)
val StatusSuccessEmerald = Color(0xFF10b981)
val StatusInfoBlue = Color(0xFF3b82f6)

// Dark Theme Foundation
val BackgroundDeepNavy = Color(0xFF0f172a)
val SurfaceSlate = Color(0xFF1e293b)
val CardSurface = Color(0xFF334155)
val BorderSubtle = Color(0xFF475569)
val TextPrimaryWhite = Color(0xFFFFFFFF)
val TextSecondaryGray = Color(0xFFcbd5e1)
val TextTertiaryGray = Color(0xFF94a3b8)

// ============= SEMANTIC COLORS =============
// Motion Detection Confidence Levels
val MotionIndicatorGreen = Color(0xFF10b981)    // Weak Motion
val MotionIndicatorYellow = Color(0xFFF59e0b)   // Possible Human Movement
val MotionIndicatorOrange = Color(0xFFf97316)   // Strong Motion
val MotionIndicatorRed = Color(0xFFef4444)      // Critical Detection

// Signal Quality Indicators
val SignalStrengthStrong = Color(0xFF10b981)
val SignalStrengthMedium = Color(0xFFF59e0b)
val SignalStrengthWeak = Color(0xFFef4444)

// Legacy name compatibility
val ErrorRed = StatusCriticalRed
val WarningYellow = StatusWarningAmber
val SuccessGreen = StatusSuccessEmerald
val DarkBackground = BackgroundDeepNavy
val DarkSurface = SurfaceSlate
val DarkSurfaceVariant = CardSurface
val DarkOutline = BorderSubtle

// ============= MATERIAL 3 COLOR SCHEME =============
val HopeFinderColorScheme = darkColorScheme(
    // Primary colors - Deep professional blue
    primary = PrimaryDeepBlue,
    onPrimary = TextPrimaryWhite,
    primaryContainer = PrimaryMediumBlue,
    onPrimaryContainer = TextPrimaryWhite,
    
    // Secondary/Accent - Modern cyan
    secondary = AccentCyan,
    onSecondary = Color(0xFF000000),
    secondaryContainer = AccentCyanLight,
    onSecondaryContainer = Color(0xFF000000),
    
    // Tertiary - Status indicator
    tertiary = StatusWarningAmber,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFFcd34d),
    onTertiaryContainer = Color(0xFF000000),
    
    // Error/Alert
    error = StatusCriticalRed,
    onError = TextPrimaryWhite,
    errorContainer = Color(0xFFfecaca),
    onErrorContainer = Color(0xFF000000),
    
    // Backgrounds
    background = BackgroundDeepNavy,
    onBackground = TextPrimaryWhite,
    
    // Surfaces
    surface = SurfaceSlate,
    onSurface = TextPrimaryWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondaryGray,
    
    // Outlines
    outline = BorderSubtle,
    outlineVariant = Color(0xFF64748b)
)