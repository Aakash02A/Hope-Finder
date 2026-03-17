package com.hope_finder.ui.radar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.data.model.RadarCell
import com.hope_finder.ui.home.CircularRadarView
import com.hope_finder.ui.home.RadarTarget
import com.hope_finder.ui.home.RadarTargetType
import com.hope_finder.ui.theme.SaasBgPrimary
import com.hope_finder.ui.theme.SaasBgSecondary
import com.hope_finder.ui.theme.SaasError
import com.hope_finder.ui.theme.SaasPrimary
import com.hope_finder.ui.theme.SaasStroke
import com.hope_finder.ui.theme.SaasSuccess
import com.hope_finder.ui.theme.SaasText
import com.hope_finder.ui.theme.SaasTextSecond
import com.hope_finder.ui.theme.SaasWhite
import com.hope_finder.ui.theme.SaasWarning
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    navController: NavController,
    probeId: String = "probe_01",
    viewModel: RadarViewModel = hiltViewModel(),
    isInBottomNav: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(probeId) {
        viewModel.startScanning(probeId)
    }

    // Generate mock targets for the radar (in real scenario, these would come from viewModel)
    val radarTargets = remember {
        listOf(
            RadarTarget(angle = 45f, distance = 0.6f, type = RadarTargetType.LIFE_SIGNATURE),
            RadarTarget(angle = 120f, distance = 0.8f, type = RadarTargetType.MOVEMENT),
            RadarTarget(angle = 200f, distance = 0.4f, type = RadarTargetType.OBJECT),
            RadarTarget(angle = 300f, distance = 0.7f, type = RadarTargetType.LIFE_SIGNATURE),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Radar Scanning", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaasText)
                        Text("Probe: $probeId", fontSize = 12.sp, color = SaasTextSecond)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaasText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaasWhite,
                    titleContentColor = SaasText,
                    navigationIconContentColor = SaasText
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scanning Status
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SaasSuccess.copy(alpha = 0.1f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "● SCANNING ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasSuccess
                    )
                }
            }

            // Circular Radar View
            item {
                CircularRadarView(
                    targets = radarTargets,
                    isScanning = true,
                    scannerColor = SaasSuccess
                )
            }

            // Scanning Sector & Statistics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Scanning Sector A3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaasWhite)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatRow("Distance", "5.7 meters", SaasPrimary)
                            StatRow("Signal Strength", "${uiState.radarData.signalStrength}%", SaasSuccess)
                            StatRow("Confidence Score", "92%", SaasWarning)
                            StatRow("Scan Depth", "${uiState.radarData.scanDepth}M", SaasError)
                        }
                    }
                }
            }

            // Bio Metrics
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Detected Signals",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCardSmall(
                            label = "Heartbeat",
                            value = if (uiState.radarData.heartbeatSignal.isNotEmpty()) "${uiState.radarData.heartbeatSignal.last().toInt()}" else "--",
                            unit = "BPM",
                            color = SaasError,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCardSmall(
                            label = "Respiration",
                            value = "${uiState.radarData.respirationRate}",
                            unit = "RPM",
                            color = SaasPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaasPrimary,
                            contentColor = SaasWhite
                        )
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { /* Rescan */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SaasBgSecondary,
                            contentColor = SaasText
                        )
                    ) {
                        Text("Rescan", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SaasTextSecond,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MetricCardSmall(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SaasWhite)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 11.sp, color = SaasTextSecond, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 10.sp, color = SaasTextSecond)
        }
    }
}
