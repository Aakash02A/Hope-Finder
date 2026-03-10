package com.tryout.hopefinder.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tryout.hopefinder.domain.model.DetectionType
import com.tryout.hopefinder.domain.model.RadarDetection
import com.tryout.hopefinder.presentation.components.RadarScanningCanvas
import com.tryout.hopefinder.ui.theme.AlertBlue
import com.tryout.hopefinder.ui.theme.AlertRed
import com.tryout.hopefinder.ui.theme.AlertYellow
import com.tryout.hopefinder.ui.theme.BackgroundDark
import com.tryout.hopefinder.ui.theme.RadarGreen
import com.tryout.hopefinder.ui.theme.RescueRadarTheme
import com.tryout.hopefinder.ui.theme.StatusOffline
import com.tryout.hopefinder.ui.theme.StatusOnline

/**
 * Radar Scanning Screen - The core visual component of the Rescue Radar System.
 */
@Composable
fun RadarScanScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RadarScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    RadarScanScreenContent(
        uiState = uiState,
        onStartScan = { viewModel.onEvent(RadarScanEvent.StartScan) },
        onStopScan = { viewModel.onEvent(RadarScanEvent.StopScan) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScanScreenContent(
    uiState: RadarScanUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RADAR SCAN",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RadarGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = RadarGreen
                ),
                actions = {
                    // System status indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isSystemOnline) StatusOnline else StatusOffline
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isSystemOnline) "ONLINE" else "OFFLINE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (uiState.isSystemOnline) StatusOnline else StatusOffline
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Probe Status Row
            ProbeStatusRow(probeStatus = uiState.probeStatus)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Radar Canvas
            RadarScanningCanvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isScanning = uiState.isScanning,
                currentSector = uiState.currentSector,
                detections = uiState.detections
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Text
            StatusDisplay(
                message = uiState.statusMessage,
                isScanning = uiState.isScanning,
                currentSector = uiState.currentSector
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detection Summary
            AnimatedVisibility(
                visible = uiState.detections.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DetectionSummaryRow(detections = uiState.detections)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Control Buttons
            ScanControlButtons(
                isScanning = uiState.isScanning,
                isSystemOnline = uiState.isSystemOnline,
                onStartScan = onStartScan,
                onStopScan = onStopScan
            )
        }
    }
}

/**
 * Row showing the status of all 5 radar probes.
 */
@Composable
private fun ProbeStatusRow(
    probeStatus: Map<Int, Boolean>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "PROBE STATUS",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                probeStatus.forEach { (probeId, isOnline) ->
                    ProbeIndicator(
                        probeId = probeId,
                        isOnline = isOnline
                    )
                }
            }
        }
    }
}

/**
 * Individual probe status indicator.
 */
@Composable
private fun ProbeIndicator(
    probeId: Int,
    isOnline: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isOnline) RadarGreen.copy(alpha = 0.2f) 
                    else StatusOffline.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) RadarGreen else StatusOffline)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "P$probeId",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isOnline) RadarGreen else StatusOffline
        )
    }
}

/**
 * Status display showing current scan status.
 */
@Composable
private fun StatusDisplay(
    message: String,
    isScanning: Boolean,
    currentSector: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121212)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isScanning) RadarGreen else Color.Gray,
                textAlign = TextAlign.Center
            )
            
            if (isScanning && currentSector > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                val sectorNames = listOf("ALPHA", "BETA", "GAMMA", "DELTA", "EPSILON")
                val sectorName = sectorNames.getOrElse(currentSector - 1) { "UNKNOWN" }
                
                Text(
                    text = "[ SECTOR: $sectorName ]",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RadarGreen.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

/**
 * Summary row showing detection counts by type.
 */
@Composable
private fun DetectionSummaryRow(
    detections: List<RadarDetection>
) {
    val heartbeatCount = detections.count { it.type == DetectionType.HEARTBEAT }
    val breathingCount = detections.count { it.type == DetectionType.BREATHING }
    val movementCount = detections.count { it.type == DetectionType.MOVEMENT }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DetectionCountCard(
            label = "HEARTBEAT",
            count = heartbeatCount,
            color = AlertRed
        )
        DetectionCountCard(
            label = "BREATHING",
            count = breathingCount,
            color = AlertYellow
        )
        DetectionCountCard(
            label = "MOVEMENT",
            count = movementCount,
            color = AlertBlue
        )
    }
}

/**
 * Card showing detection count for a specific type.
 */
@Composable
private fun DetectionCountCard(
    label: String,
    count: Int,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = color.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
    }
}

/**
 * Scan control buttons.
 */
@Composable
private fun ScanControlButtons(
    isScanning: Boolean,
    isSystemOnline: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        if (isScanning) {
            // Stop Scan Button
            Button(
                onClick = onStopScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertRed
                ),
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STOP SCAN",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        } else {
            // Start Scan Button
            Button(
                onClick = onStartScan,
                enabled = isSystemOnline,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadarGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = RadarGreen.copy(alpha = 0.3f),
                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.7f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START SCAN",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun RadarScanScreenPreview() {
    RescueRadarTheme {
        RadarScanScreenContent(
            uiState = RadarScanUiState(
                statusMessage = "Ready to scan",
                isSystemOnline = true
            ),
            onStartScan = {},
            onStopScan = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun RadarScanScreenScanningPreview() {
    val sampleDetections = listOf(
        RadarDetection(
            id = "1",
            type = DetectionType.HEARTBEAT,
            zoneLocation = "Alpha-1",
            distance = 4.5f,
            signalStrength = 85,
            confidenceScore = 92f,
            probeId = 1
        ),
        RadarDetection(
            id = "2",
            type = DetectionType.BREATHING,
            zoneLocation = "Gamma-3",
            distance = 7.2f,
            signalStrength = 60,
            confidenceScore = 78f,
            probeId = 3
        )
    )
    
    RescueRadarTheme {
        RadarScanScreenContent(
            uiState = RadarScanUiState(
                scanState = com.tryout.hopefinder.domain.model.RadarScanState(
                    isScanning = true,
                    currentSector = 2,
                    detections = sampleDetections
                ),
                statusMessage = "Scanning sector Beta...",
                isSystemOnline = true
            ),
            onStartScan = {},
            onStopScan = {},
            onNavigateBack = {}
        )
    }
}
