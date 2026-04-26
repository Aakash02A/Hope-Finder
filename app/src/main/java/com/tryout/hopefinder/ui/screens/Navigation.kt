package com.tryout.hopefinder.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.tryout.hopefinder.ui.theme.DarkBackground
import com.tryout.hopefinder.ui.theme.DarkSurface

/**
 * Navigation structure for Hope-Finder app
 */
enum class Screen {
    WELCOME,
    LOGIN,
    REGISTER,
    DASHBOARD,
    RADAR,
    ALERTS,
    REPORTS,
    PROFILE
}

/**
 * Main navigation composable with bottom bar
 */
@Composable
fun MainNavigation(context: Context) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(if (isLoggedIn) Screen.DASHBOARD else Screen.WELCOME) }

    val showBottomBar = currentScreen !in listOf(Screen.WELCOME, Screen.LOGIN, Screen.REGISTER)

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        // Screen content with proper padding to avoid bottom nav overlap
        when (currentScreen) {
            Screen.WELCOME -> WelcomeScreen(
                onStartMission = { currentScreen = Screen.LOGIN }
            )
            Screen.LOGIN -> LoginScreen(
                onLoginSuccess = { 
                    isLoggedIn = true
                    currentScreen = Screen.DASHBOARD 
                },
                onNavigateToRegister = { currentScreen = Screen.REGISTER },
                onBack = { currentScreen = Screen.WELCOME }
            )
            Screen.REGISTER -> RegisterScreen(
                onRegisterSuccess = { 
                    isLoggedIn = true
                    currentScreen = Screen.DASHBOARD 
                },
                onNavigateToLogin = { currentScreen = Screen.LOGIN },
                onBack = { currentScreen = Screen.LOGIN }
            )
            Screen.DASHBOARD -> DashboardScreen(context, paddingValues = paddingValues)
            Screen.RADAR -> RadarScreen(context, paddingValues = paddingValues)
            Screen.ALERTS -> AlertsScreen(context, paddingValues = paddingValues)
            Screen.REPORTS -> ReportsScreen(context, paddingValues = paddingValues)
            Screen.PROFILE -> ProfileScreen(
                context = context,
                paddingValues = paddingValues,
                onLogout = {
                    // Stop polling and clean up connection on logout
                    (context as? com.tryout.hopefinder.MainActivity)?.let {
                        // We can't easily access mainViewModel here unless we pass it
                    }
                    isLoggedIn = false
                    currentScreen = Screen.WELCOME
                }
            )
        }
    }
}

/**
 * Bottom navigation bar
 */
@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dashboard
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.DASHBOARD)
                        Icons.Filled.Dashboard
                    else
                        Icons.Outlined.Dashboard,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard", fontSize = 10.sp) },
            selected = currentScreen == Screen.DASHBOARD,
            onClick = { onScreenSelected(Screen.DASHBOARD) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF6B00),
                selectedTextColor = Color(0xFFFF6B00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = DarkBackground
            )
        )

        // Radar
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.RADAR)
                        Icons.Filled.Radar
                    else
                        Icons.Outlined.Radar,
                    contentDescription = "Radar"
                )
            },
            label = { Text("Radar", fontSize = 10.sp) },
            selected = currentScreen == Screen.RADAR,
            onClick = { onScreenSelected(Screen.RADAR) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF6B00),
                selectedTextColor = Color(0xFFFF6B00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = DarkBackground
            )
        )

        // Alerts
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.ALERTS)
                        Icons.Filled.Notifications
                    else
                        Icons.Outlined.Notifications,
                    contentDescription = "Alerts"
                )
            },
            label = { Text("Alerts", fontSize = 10.sp) },
            selected = currentScreen == Screen.ALERTS,
            onClick = { onScreenSelected(Screen.ALERTS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF6B00),
                selectedTextColor = Color(0xFFFF6B00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = DarkBackground
            )
        )

        // Reports
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.REPORTS)
                        Icons.Filled.Assessment
                    else
                        Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = "Reports"
                )
            },
            label = { Text("Reports", fontSize = 10.sp) },
            selected = currentScreen == Screen.REPORTS,
            onClick = { onScreenSelected(Screen.REPORTS) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF6B00),
                selectedTextColor = Color(0xFFFF6B00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = DarkBackground
            )
        )

        // Profile
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.PROFILE)
                        Icons.Filled.Person
                    else
                        Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 10.sp) },
            selected = currentScreen == Screen.PROFILE,
            onClick = { onScreenSelected(Screen.PROFILE) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF6B00),
                selectedTextColor = Color(0xFFFF6B00),
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray,
                indicatorColor = DarkBackground
            )
        )
    }
}
