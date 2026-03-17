package com.hope_finder.ui.probe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.hope_finder.data.model.Probe
import com.hope_finder.ui.theme.SaasBgPrimary
import com.hope_finder.ui.theme.SaasError
import com.hope_finder.ui.theme.SaasSuccess
import com.hope_finder.ui.theme.SaasText
import com.hope_finder.ui.theme.SaasTextSecond
import com.hope_finder.ui.theme.SaasPrimary
import com.hope_finder.ui.theme.SaasWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProbeScreen(
    navController: NavHostController,
    isInBottomNav: Boolean = false
) {
    // Sample probes for display
    val sampleProbes = listOf(
        Probe(id = "probe_01", name = "Probe Alpha", status = "Online", batteryLevel = 95, signalStrength = 85),
        Probe(id = "probe_02", name = "Probe Beta", status = "Online", batteryLevel = 72, signalStrength = 65),
        Probe(id = "probe_03", name = "Probe Gamma", status = "Offline", batteryLevel = 5, signalStrength = 0)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Probes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaasText)
                        Text("Manage rescue probes", fontSize = 12.sp, color = SaasTextSecond)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaasWhite
                )
            )
        },
        containerColor = SaasBgPrimary,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = SaasPrimary,
                contentColor = SaasWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add probe")
            }
        }
    ) { padding ->
        if (sampleProbes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaasBgPrimary)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Default.SignalCellularNull, contentDescription = null, tint = SaasTextSecond, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No probes connected", color = SaasTextSecond, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Add a new probe to get started", color = SaasTextSecond, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SaasBgPrimary)
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sampleProbes, key = { it.id }) { probe ->
                    SaasProbeCard(probe = probe)
                }
            }
        }
    }
}

@Composable
private fun SaasProbeCard(probe: Probe) {
    val statusColor = if (probe.status == "Online") SaasSuccess else SaasError
    val statusText = probe.status

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SaasWhite)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = probe.name.ifBlank { "Probe ${probe.id}" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaasText
                    )
                    Text(
                        text = probe.id,
                        fontSize = 11.sp,
                        color = SaasTextSecond
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProbeStatBadge("Battery", "${probe.batteryLevel}%", SaasPrimary, Modifier.weight(1f))
                ProbeStatBadge("Signal", "${probe.signalStrength}%", SaasSuccess, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProbeStatBadge(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = SaasTextSecond, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
