package com.hope_finder.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.data.model.RescueReport
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rescue Reports", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Global Export All */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Export All")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rescue reports available", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        onExport = { viewModel.exportReport(report.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(report: RescueReport, onExport: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title.ifEmpty { "Rescue Mission ${report.id.take(6)}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = report.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Surface(
                    color = if (report.confidence > 75) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "${report.confidence}% Conf.",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (report.confidence > 75) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem(label = "Heartbeat", value = "${report.heartbeatBpm} BPM", icon = Icons.Default.Favorite)
                MetricItem(label = "Respiration", value = "${report.respirationRpm} RPM", icon = Icons.Default.Air)
                MetricItem(label = "Probes", value = "${report.probeIds.size}", icon = Icons.Default.SettingsInputAntenna)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            val format = SimpleDateFormat("MMM dd, yyyy | HH:mm", Locale.getDefault())
            Text(
                text = "Recorded: ${format.format(Date(report.timestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Scan Timeline", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                report.detections.forEach { event ->
                    TimelineItem(event)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF")
                    }
                    Button(
                        onClick = { expanded = false },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Close Details")
                    }
                }
            } else {
                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("VIEW FULL TIMELINE")
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun TimelineItem(event: com.hope_finder.data.model.DetectionEvent) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Box(modifier = Modifier.width(2.dp).height(24.dp).background(Color.LightGray))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(timeFormat.format(Date(event.time)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(event.description, style = MaterialTheme.typography.bodySmall)
            Text("Signal Strength: ${event.signalStrength}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
