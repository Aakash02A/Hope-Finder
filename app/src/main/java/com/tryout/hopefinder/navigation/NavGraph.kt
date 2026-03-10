package com.tryout.hopefinder.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tryout.hopefinder.domain.model.UserRole
import com.tryout.hopefinder.presentation.AppViewModel
import com.tryout.hopefinder.presentation.AuthState
import com.tryout.hopefinder.presentation.alerts.AlertsScreen
import com.tryout.hopefinder.presentation.auth.LoginScreen
import com.tryout.hopefinder.presentation.dashboard.DashboardScreen
import com.tryout.hopefinder.presentation.reports.ReportsScreen
import com.tryout.hopefinder.presentation.scan.RadarScanScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    appViewModel: AppViewModel = hiltViewModel()
) {
    val authState by appViewModel.authState.collectAsState()

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Dashboard
        else -> Screen.Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            val user = (authState as? AuthState.Authenticated)?.user
            DashboardScreen(
                onNavigateToScan = { navController.navigate(Screen.RadarScan) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts) },
                onNavigateToReports = {
                    if (user?.role == UserRole.ADMIN) {
                        navController.navigate(Screen.Reports)
                    } else {
                        // Show restricted access or Toast
                    }
                }
            )
        }

        composable<Screen.RadarScan> {
            RadarScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Alerts> {
            AlertsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Reports> {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
