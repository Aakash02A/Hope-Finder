package com.tryout.hopefinder.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed class defining all navigation routes for type-safe navigation.
 */
sealed interface Screen {
    
    @Serializable
    data object Login : Screen
    
    @Serializable
    data object Dashboard : Screen
    
    @Serializable
    data object RadarScan : Screen
    
    @Serializable
    data object Alerts : Screen
    
    @Serializable
    data object Reports : Screen
}

/**
 * Bottom navigation items.
 */
enum class BottomNavItem(
    val route: Screen,
    val title: String,
    val icon: String
) {
    DASHBOARD(Screen.Dashboard, "Dashboard", "dashboard"),
    SCAN(Screen.RadarScan, "Scan", "radar"),
    ALERTS(Screen.Alerts, "Alerts", "notifications"),
    REPORTS(Screen.Reports, "Reports", "analytics")
}
