package com.hope_finder.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Radar : Screen("radar")
    object Probe : Screen("probe")
    object Alerts : Screen("alerts")
    object Reports : Screen("reports")
    
    // Legacy/Wrapper route if needed
    object Auth : Screen("auth")
}
