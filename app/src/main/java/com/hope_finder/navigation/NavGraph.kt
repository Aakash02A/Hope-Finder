package com.hope_finder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hope_finder.ui.auth.AuthScreen
import com.hope_finder.ui.home.HomeScreen
import com.hope_finder.ui.radar.RadarScreen
import com.hope_finder.ui.probe.ProbeScreen
import com.hope_finder.ui.alerts.AlertsScreen
import com.hope_finder.ui.reports.ReportsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) { AuthScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Radar.route) { RadarScreen(navController) }
        composable(Screen.Probe.route) { ProbeScreen(navController) }
        composable(Screen.Alerts.route) { AlertsScreen(navController) }
        composable(Screen.Reports.route) { ReportsScreen(navController) }
    }
}
