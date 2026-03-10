package com.tryout.hopefinder.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Rescue Radar Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateToScan, modifier = Modifier.fillMaxWidth()) {
            Text("Start Radar Scan")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNavigateToAlerts, modifier = Modifier.fillMaxWidth()) {
            Text("View Alerts")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onNavigateToReports, modifier = Modifier.fillMaxWidth()) {
            Text("View Reports")
        }
    }
}
