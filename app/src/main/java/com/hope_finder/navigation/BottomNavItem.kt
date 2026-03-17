package com.hope_finder.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Devices
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = "dashboard",
        label = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    object RadarScan : BottomNavItem(
        route = "radar_scan",
        label = "Scan",
        icon = Icons.Default.Map
    )

    object Alerts : BottomNavItem(
        route = "alerts",
        label = "Alerts",
        icon = Icons.Default.Notifications
    )

    object Reports : BottomNavItem(
        route = "reports",
        label = "Reports",
        icon = Icons.Default.BarChart
    )

    object Probes : BottomNavItem(
        route = "probes",
        label = "Devices",
        icon = Icons.Default.Devices
    )

    companion object {
        fun values() = listOf(Dashboard, RadarScan, Alerts, Reports, Probes)
    }
}
