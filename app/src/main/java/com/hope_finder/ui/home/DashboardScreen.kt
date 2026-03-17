package com.hope_finder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hope_finder.data.model.DashboardStats
import com.hope_finder.navigation.Screen
import com.hope_finder.ui.theme.SaasBgPrimary
import com.hope_finder.ui.theme.SaasBgSecondary
import com.hope_finder.ui.theme.SaasError
import com.hope_finder.ui.theme.SaasPrimary
import com.hope_finder.ui.theme.SaasStroke
import com.hope_finder.ui.theme.SaasSuccess
import com.hope_finder.ui.theme.SaasText
import com.hope_finder.ui.theme.SaasTextSecond
import com.hope_finder.ui.theme.SaasWarning
import com.hope_finder.ui.theme.SaasWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    isInBottomNav: Boolean = false,
    onNavigateToRadar: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaasText
                        )
                        Text(
                            text = "Disaster Rescue Monitoring System",
                            fontSize = 12.sp,
                            color = SaasTextSecond
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaasWhite,
                    titleContentColor = SaasText
                )
            )
        },
        containerColor = SaasBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SaasBgPrimary)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Section with Status
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SaasPrimary.copy(alpha = 0.08f))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "System Status",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaasText
                                )
                                Text(
                                    text = "All systems operational",
                                    fontSize = 12.sp,
                                    color = SaasTextSecond
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(SaasSuccess)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SaasBgSecondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SaasSuccess)
                            )
                        }
                        Text(
                            text = "Uptime: 99.8% • Last Sync: 2 min ago",
                            fontSize = 11.sp,
                            color = SaasTextSecond,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Key Metrics Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Key Metrics",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // 2x2 Grid of KPI Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactKpiCard(
                            label = "Active Probes",
                            value = uiState.stats.probesOnline.toString(),
                            icon = Icons.Default.Wifi,
                            color = SaasPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        CompactKpiCard(
                            label = "Life Signals",
                            value = uiState.stats.lifeSignalsDetected.toString(),
                            icon = Icons.Default.Favorite,
                            color = SaasError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactKpiCard(
                            label = "Active Scans",
                            value = uiState.stats.activeScanZones.toString(),
                            icon = Icons.Default.Map,
                            color = SaasSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        CompactKpiCard(
                            label = "Alerts",
                            value = uiState.stats.rescueAlerts.toString(),
                            icon = Icons.Default.Notifications,
                            color = SaasWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Column {
                    Text(
                        text = "Quick Actions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(
                            label = "Start Scan",
                            icon = Icons.Default.Map,
                            onClick = {
                                if (isInBottomNav) {
                                    onNavigateToRadar()
                                } else {
                                    navController.navigate(Screen.Radar.route)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            label = "View Alerts",
                            icon = Icons.Default.Notifications,
                            onClick = { navController.navigate(Screen.Alerts.route) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            label = "View Devices",
                            icon = Icons.Default.Wifi,
                            onClick = { navController.navigate(Screen.Probe.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Performance Chart Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Performance Analytics",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Scans Per Day Line Chart
                    LineChartView(
                        data = listOf(
                            LineChartData("Day 1", 12f, SaasPrimary),
                            LineChartData("Day 2", 19f, SaasPrimary),
                            LineChartData("Day 3", 15f, SaasPrimary),
                            LineChartData("Day 4", 25f, SaasPrimary),
                            LineChartData("Day 5", 22f, SaasPrimary),
                            LineChartData("Day 6", 28f, SaasPrimary),
                            LineChartData("Day 7", 32f, SaasPrimary)
                        ),
                        title = "Scans Per Day (Last 7 Days)",
                        maxValue = 40f
                    )
                }
            }

            // Detection Success Rate
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Detection Success Rate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    PieChartView(
                        segments = listOf(
                            PieChartSegment("Heartbeat", 45f, SaasError),
                            PieChartSegment("Movement", 28f, SaasWarning),
                            PieChartSegment("Breathing", 27f, SaasSuccess)
                        ),
                        title = "Detection Types"
                    )
                }
            }

            // Recent Activity
            item {
                Column {
                    Text(
                        text = "Recent Activity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (uiState.alerts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.alerts.take(3).forEach { alert ->
                                ActivityItemCard(
                                    title = alert.message,
                                    time = "Just now",
                                    severity = alert.severity
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SaasBgSecondary)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent activity",
                                fontSize = 13.sp,
                                color = SaasTextSecond
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun CompactKpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SaasWhite)
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = SaasTextSecond,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun KpiCardRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SaasWhite)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = SaasTextSecond,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = SaasPrimary,
            contentColor = SaasWhite
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActivityItemCard(
    title: String,
    time: String,
    severity: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SaasWhite)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SaasText
                )
                Text(
                    text = time,
                    fontSize = 11.sp,
                    color = SaasTextSecond
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (severity) {
                            "High" -> SaasError.copy(alpha = 0.1f)
                            "Medium" -> SaasWarning.copy(alpha = 0.1f)
                            else -> SaasSuccess.copy(alpha = 0.1f)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = severity,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (severity) {
                        "High" -> SaasError
                        "Medium" -> SaasWarning
                        else -> SaasSuccess
                    }
                )
            }
        }
    }
}
