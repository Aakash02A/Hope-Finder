package com.hope_finder.ui.main

import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hope_finder.navigation.BottomNavItem
import com.hope_finder.ui.alerts.AlertsScreen
import com.hope_finder.ui.home.DashboardScreen
import com.hope_finder.ui.probe.ProbeScreen
import com.hope_finder.ui.radar.RadarScreen
import com.hope_finder.ui.reports.ReportsScreen
import com.hope_finder.ui.theme.SaasBgPrimary
import com.hope_finder.ui.theme.SaasPrimary
import com.hope_finder.ui.theme.SaasText
import com.hope_finder.ui.theme.SaasWhite

@Composable
fun MainScreen(navController: NavHostController) {
    val currentSelectedItem = remember { mutableStateOf(BottomNavItem.Dashboard.route) }
    val innerNavController = rememberNavController()

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        containerColor = SaasBgPrimary,
        bottomBar = {
            NavigationBar(
                containerColor = SaasWhite,
                contentColor = SaasText,
                modifier = Modifier.background(SaasWhite)
            ) {
                BottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        label = {
                            Text(
                                text = item.label,
                                fontSize = 10.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        selected = currentSelectedItem.value == item.route,
                        onClick = {
                            currentSelectedItem.value = item.route
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaasPrimary,
                            selectedTextColor = SaasPrimary,
                            indicatorColor = SaasBgPrimary,
                            unselectedIconColor = SaasText,
                            unselectedTextColor = SaasText
                        )
                    )
                }
            }
        }
    ) { _ ->
        NavHost(
            navController = innerNavController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.background(SaasBgPrimary)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    isInBottomNav = true,
                    onNavigateToRadar = {
                        innerNavController.navigate(BottomNavItem.RadarScan.route)
                        currentSelectedItem.value = BottomNavItem.RadarScan.route
                    }
                )
            }
            composable(BottomNavItem.RadarScan.route) {
                RadarScreen(
                    navController = navController,
                    isInBottomNav = true
                )
            }
            composable(BottomNavItem.Alerts.route) {
                AlertsScreen(
                    navController = navController,
                    isInBottomNav = true
                )
            }
            composable(BottomNavItem.Reports.route) {
                ReportsScreen(
                    navController = navController,
                    isInBottomNav = true
                )
            }
            composable(BottomNavItem.Probes.route) {
                ProbeScreen(
                    navController = navController,
                    isInBottomNav = true
                )
            }
        }
    }
}
