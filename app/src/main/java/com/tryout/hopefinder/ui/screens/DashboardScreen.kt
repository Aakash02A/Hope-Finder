package com.tryout.hopefinder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tryout.hopefinder.data.AlertEntity
import com.tryout.hopefinder.data.DetectionEntity
import com.tryout.hopefinder.data.DeviceStatusEntity
import com.tryout.hopefinder.ui.components.*
import com.tryout.hopefinder.ui.theme.*
import com.tryout.hopefinder.viewmodel.*
import kotlinx.coroutines.flow.StateFlow

/**
 * Dashboard Screen
 * Shows device status, recent detections, and alerts
 */
@Composable
fun DashboardScreen(
    context: android.content.Context? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    mainViewModel: MainViewModel = viewModel(factory = context?.let { MainViewModelFactory(it) }),
    viewModel: DashboardViewModel = viewModel(
        factory = context?.let { DashboardViewModelFactory(it) }
    )
) {
    val deviceStatus by viewModel.deviceStatus.collectAsState(initial = null)
    val recentDetections by viewModel.recentDetections.collectAsState(initial = emptyList())
    val unacknowledgedAlerts by viewModel.unacknowledgedAlerts.collectAsState(initial = emptyList())
    val alertCount by viewModel.alertCount.collectAsState(initial = 0)
    val isScanning by viewModel.isScanning.collectAsState(initial = false)
    val scanStatus by viewModel.scanStatus.collectAsState(initial = null)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hope-Finder",
                        color = PrimaryDeepBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Radar Search & Rescue",
                        color = TextSecondaryGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SurfaceSlate, shape = CircleShape)
                        .border(2.dp, AccentCyan, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = "Radar",
                        tint = AccentCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Device Status Card
        item {
            deviceStatus?.let {
                DeviceInfoCard(
                    wifiConnected = it.wifiConnected,
                    radarHealthy = it.radarHealthy,
                    calibrated = it.calibrated,
                    scanning = it.scanning
                )
            }
        }

        // Connection Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Device Connection",
                            color = TextPrimaryWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        deviceStatus?.let {
                            Text(
                                text = "RSSI: ${it.rssi} dBm | Uptime: ${it.uptime}s",
                                color = TextTertiaryGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (deviceStatus?.wifiConnected == true)
                            Icons.Filled.SignalCellularAlt
                        else
                            Icons.Filled.SignalCellularNoSim,
                        contentDescription = null,
                        tint = if (deviceStatus?.wifiConnected == true) StatusSuccessEmerald else StatusCriticalRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Alerts Summary
        if (alertCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, StatusCriticalRed, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "⚠ UNACKNOWLEDGED ALERTS",
                                color = StatusCriticalRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$alertCount detection(s) require review",
                                color = TextSecondaryGray,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(StatusCriticalRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, StatusCriticalRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alertCount.toString(),
                                color = StatusCriticalRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Latest Detection
        item {
            recentDetections.lastOrNull()?.let { latest ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Latest Detection",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = latest.sectorLabel,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${latest.angle}° | ${latest.motionLabel}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            ConfidenceScore(latest.confidence)
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isScanning) {
                    EmergencyButton(
                        "Stop Scan",
                        onClick = { 
                            viewModel.stopScan()
                            mainViewModel.setUserScanning(false)
                        },
                        isDestructive = true
                    )
                } else {
                    EmergencyButton(
                        "Start Scan",
                        onClick = { 
                            viewModel.startScan()
                            mainViewModel.setUserScanning(true)
                        }
                    )
                }
                EmergencyButton(
                    "Calibrate Radar",
                    onClick = { viewModel.startCalibration() },
                    isDestructive = false
                )
            }
        }

        // Scan Status Message
        if (scanStatus != null) {
            item {
                Text(
                    text = scanStatus ?: "",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}
