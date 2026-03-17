package com.hope_finder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hope_finder.ui.auth.ForgotPasswordScreen
import com.hope_finder.ui.auth.LoginScreen
import com.hope_finder.ui.auth.RegisterScreen
import com.hope_finder.ui.main.MainScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }

        // Main app with bottom navigation
        composable(Screen.Home.route) { MainScreen(navController) }
    }
}
