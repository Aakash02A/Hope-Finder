package com.hope_finder.ui.probe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.data.model.Probe
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProbeStatusScreen(
    navController: NavController,
    viewModel: ProbeStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Probe Monitoring") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text("Search Probe ID or Name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.probes, key = { it.id }) { probe ->
                        ProbeCard(probe)
                    }
                }
            }
        }
    }
}

@Composable
fun ProbeCard(probe: Probe) {
    var expanded by remember { mutableStateOf(false) }
    val isOnline = probe.status.lowercase() == "online"
    val statusColor = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(statusColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(probe.name.ifEmpty { "Probe ${probe.id.takeLast(4)}" }, fontWeight = FontWeight.Bold)
                        Text("ID: ${probe.id}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalStrengthIndicator(probe.signalStrength)
                    Spacer(modifier = Modifier.width(16.dp))
                    BatteryIndicator(probe.batteryLevel)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DetailItem(label = "Temperature", value = "${probe.temperature}°C", icon = Icons.Default.Thermostat)
                        DetailItem(label = "Firmware", value = probe.firmwareVersion, icon = Icons.Default.Update)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val date = Date(probe.lastActive)
                    val format = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                    DetailItem(
                        label = "Last Active", 
                        value = format.format(date), 
                        icon = Icons.Default.History,
                        fullWidth = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { /* Navigate to Radar Scan */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text("Access Live Radar")
                    }
                }
            }
        }
    }
}

@Composable
fun SignalStrengthIndicator(strength: Int) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..4) {
            val barHeight = (i * 4).dp
            val isActive = strength >= (i * 25)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = MaterialTheme.shapes.extraSmall
                    )
            )
        }
    }
}

@Composable
fun BatteryIndicator(level: Int) {
    val batteryColor = when {
        level > 60 -> Color(0xFF4CAF50)
        level > 20 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when {
                level > 80 -> Icons.Default.BatteryFull
                level > 20 -> Icons.Default.BatteryChargingFull
                else -> Icons.Default.BatteryAlert
            },
            contentDescription = null,
            tint = batteryColor,
            modifier = Modifier.size(20.dp)
        )
        Text("$level%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: ImageVector, fullWidth: Boolean = false) {
    Row(
        modifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}
