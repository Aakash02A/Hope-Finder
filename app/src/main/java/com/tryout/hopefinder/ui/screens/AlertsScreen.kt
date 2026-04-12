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
import com.tryout.hopefinder.ui.components.AlertCard
import com.tryout.hopefinder.ui.theme.*
import com.tryout.hopefinder.viewmodel.AlertsViewModel
import com.tryout.hopefinder.viewmodel.AlertsViewModelFactory

/**
 * Alerts Screen
 * Displays detection alerts sorted by confidence and time
 */
@Composable
fun AlertsScreen(
    context: android.content.Context,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: AlertsViewModel = viewModel(factory = AlertsViewModelFactory(context))
) {
    val unacknowledgedAlerts by viewModel.unacknowledgedAlerts.collectAsState(initial = emptyList())
    val filteredAlerts by viewModel.filteredAlerts.collectAsState(initial = emptyList())
    var selectedSeverity by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "ALERTS",
                        color = TextPrimaryWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${unacknowledgedAlerts.size} unacknowledged",
                        color = if (unacknowledgedAlerts.isNotEmpty()) StatusCriticalRed else StatusSuccessEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (unacknowledgedAlerts.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.acknowledgeAllAlerts() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccessEmerald),
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark All Read", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                }
            }
        }

        // Severity Filter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceSlate, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("CRITICAL", "HIGH", "MEDIUM").forEach { severity ->
                    FilterChip(
                        selected = selectedSeverity == severity,
                        onClick = {
                            selectedSeverity = if (selectedSeverity == severity) null else severity
                            viewModel.setSeverityFilter(selectedSeverity)
                        },
                        label = { Text(severity, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (severity) {
                                "CRITICAL" -> StatusCriticalRed
                                "HIGH" -> MotionIndicatorOrange
                                else -> StatusWarningAmber
                            },
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }

        // Alert Statistics
        item {
            val stats = filteredAlerts.groupBy { it.severity }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("CRITICAL" to StatusCriticalRed, "HIGH" to MotionIndicatorOrange, "MEDIUM" to StatusWarningAmber).forEach { (severity, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (stats[severity]?.size ?: 0).toString(),
                                color = color,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(severity, color = TextTertiaryGray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Alert List
        if (filteredAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(SurfaceSlate, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccessEmerald,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "All alerts acknowledged",
                            color = TextSecondaryGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredAlerts) { alert ->
                AlertListItemCard(
                    alert = alert,
                    onAcknowledge = { viewModel.acknowledgeAlert(alert.id) }
                )
            }
        }
    }
}

/**
 * Alert list item card
 */
@Composable
fun AlertListItemCard(
    alert: AlertEntity,
    onAcknowledge: () -> Unit
) {
    val severityColor = when (alert.severity) {
        "CRITICAL" -> StatusCriticalRed
        "HIGH" -> MotionIndicatorOrange
        else -> StatusWarningAmber
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, severityColor, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(severityColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = alert.sectorLabel,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${alert.angle}°",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.message,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Confidence: ${alert.confidence}%",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Severity: ${alert.severity}",
                            color = severityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                IconButton(
                    onClick = onAcknowledge,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Acknowledge",
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
