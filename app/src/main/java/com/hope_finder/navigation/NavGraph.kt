package com.hope_finder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hope_finder.ui.auth.ForgotPasswordScreen
import com.hope_finder.ui.auth.LoginScreen
import com.hope_finder.ui.auth.RegisterScreen
import com.hope_finder.ui.home.HomeScreen
import com.hope_finder.ui.radar.RadarScreen
import com.hope_finder.ui.probe.ProbeScreen
import com.hope_finder.ui.alerts.AlertsScreen
import com.hope_finder.ui.reports.ReportsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }

        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Radar.route) { RadarScreen(navController) }
        composable(Screen.Probe.route) { ProbeScreen(navController) }
        composable(Screen.Alerts.route) { AlertsScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
    }
}
