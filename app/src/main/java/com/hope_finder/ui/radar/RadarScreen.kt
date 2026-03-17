package com.hope_finder.ui.radar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.ui.home.CircularRadarView
import com.hope_finder.ui.home.RadarTarget
import com.hope_finder.ui.home.RadarTargetType
import com.hope_finder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    navController: NavController,
    probeId: String = "probe_01",
    viewModel: RadarViewModel = hiltViewModel(),
    isInBottomNav: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    // Mock targets that appear when scanning
    val radarTargets = remember(uiState.isScanning, uiState.radarData.timestamp) {
        if (uiState.isScanning) {
            listOf(
                RadarTarget(angle = 45f, distance = 0.6f, type = RadarTargetType.LIFE_SIGNATURE),
                RadarTarget(angle = 120f, distance = 0.8f, type = RadarTargetType.MOVEMENT)
            )
        } else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Radar Scanning", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaasText)
                        Text("Probe: $probeId | Master Node: MN-01", fontSize = 12.sp, color = SaasTextSecond)
                    }
                },
                navigationIcon = {
                    if (!isInBottomNav) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaasText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaasWhite)
            )
        },
        containerColor = SaasBgPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Scanning Status Indicator
            item {
                StatusIndicator(isScanning = uiState.isScanning)
            }

            // Radar View
            item {
                CircularRadarView(
                    targets = radarTargets,
                    isScanning = uiState.isScanning,
                    scannerColor = if (uiState.isScanning) SaasSuccess else SaasTextSecond
                )
            }

            // Control Button
            item {
                Button(
                    onClick = { viewModel.toggleScanning(probeId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isScanning) SaasError else SaasPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isScanning) "STOP SCANNING" else "START RADAR SCAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Metrics Grid
            if (uiState.isScanning || uiState.radarData.timestamp > 0) {
                item {
                    MetricsGrid(uiState)
                }

                item {
                    Button(
                        onClick = { viewModel.saveRescueReport(probeId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SaasSuccess)
                    ) {
                        Text("GENERATE RESCUE REPORT")
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Press 'Start' to begin scanning for life signs.",
                            color = SaasTextSecond,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun StatusIndicator(isScanning: Boolean) {
    val color = if (isScanning) SaasSuccess else SaasTextSecond
    val text = if (isScanning) "SCANNING ACTIVE" else "RADAR STANDBY"
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun MetricsGrid(uiState: RadarUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Real-time Telemetry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Heartbeat",
                value = if (uiState.radarData.heartbeatSignal.isNotEmpty()) "${uiState.radarData.heartbeatSignal.last().toInt()}" else "--",
                unit = "BPM",
                color = SaasError,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Respiration",
                value = "${uiState.radarData.respirationRate}",
                unit = "RPM",
                color = SaasPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Scan Depth",
                value = String.format("%.1f", uiState.radarData.scanDepth),
                unit = "Meters",
                color = SaasWarning,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Signal",
                value = "${uiState.radarData.signalStrength}",
                unit = "%",
                color = SaasSuccess,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SaasWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = SaasTextSecond)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 10.sp, color = SaasTextSecond)
        }
    }
}
