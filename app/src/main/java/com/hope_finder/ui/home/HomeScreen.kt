package com.hope_finder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hope_finder.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("HopeFinder", fontWeight = FontWeight.Bold)
                        Text("Disaster Monitoring Panel", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Profile/Settings */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dashboard Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Probes Online",
                        value = uiState.stats.probesOnline.toString(),
                        icon = Icons.Default.Wifi,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Zones",
                        value = uiState.stats.activeScanZones.toString(),
                        icon = Icons.Default.Map,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Life Signals",
                        value = uiState.stats.lifeSignalsDetected.toString(),
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Rescue Alerts",
                        value = uiState.stats.rescueAlerts.toString(),
                        icon = Icons.Default.Warning,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Active Radar Cells Section
            item {
                SectionHeader(title = "Active Radar Cells")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.radarCells) { cell ->
                        RadarCellItem(cell)
                    }
                }
            }

            // Detected Life Signals Section
            item {
                SectionHeader(title = "Detected Life Signals")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        uiState.lifeSignals.forEach { signal ->
                            LifeSignalRow(signal)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp), alpha = 0.5f)
                        }
                    }
                }
            }

            // Connected Probes Section
            item {
                SectionHeader(title = "Connected Probes")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.probes.forEach { probe ->
                        ProbeItem(probe)
                    }
                }
            }

            // System Alerts Section
            item {
                SectionHeader(title = "System Alerts")
                uiState.alerts.forEach { alert ->
                    AlertItem(alert)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun RadarCellItem(cell: RadarCell) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(cell.name, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                progress = { cell.signalStrength / 100f },
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("${cell.signalStrength}% Signal", style = MaterialTheme.typography.labelSmall)
            Text(cell.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LifeSignalRow(signal: LifeSignal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color.Red, shape = MaterialTheme.shapes.extraSmall)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(signal.location, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("Confidence: ${signal.confidence}%", style = MaterialTheme.typography.labelSmall)
        }
        Text("Active", color = Color.Red, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProbeItem(probe: ConnectedProbe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SettingsInputAntenna,
                contentDescription = null,
                tint = if (probe.status == "Online") Color.Green else Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(probe.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("Status: ${probe.status}", style = MaterialTheme.typography.labelSmall)
            }
            Text("Batt: ${probe.batteryLevel}%", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun AlertItem(alert: SystemAlert) {
    val containerColor = when(alert.severity) {
        "High" -> Color(0xFFFFEBEE)
        "Medium" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF1F8E9)
    }
    val contentColor = when(alert.severity) {
        "High" -> Color(0xFFD32F2F)
        "Medium" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(alert.message, style = MaterialTheme.typography.bodySmall, color = contentColor)
        }
    }
}
