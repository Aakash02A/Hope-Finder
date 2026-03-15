package com.hope_finder.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Radar : Screen("radar")
    object Probe : Screen("probe")
    object Alerts : Screen("alerts")
    object Reports : Screen("reports")
}
