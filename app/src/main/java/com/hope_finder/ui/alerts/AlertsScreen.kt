package com.hope_finder.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.data.model.Alert
import com.hope_finder.data.model.AlertPriority
import com.hope_finder.data.model.AlertType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Center", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active alerts", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.alerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        onResolve = { viewModel.resolveAlert(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert, onResolve: () -> Unit) {
    val backgroundColor = when {
        alert.isResolved -> Color(0xFFE8F5E9)
        alert.priority == AlertPriority.HIGH -> Color(0xFFFFEBEE)
        alert.priority == AlertPriority.MEDIUM -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
    }

    val contentColor = when {
        alert.isResolved -> Color(0xFF2E7D32)
        alert.priority == AlertPriority.HIGH -> Color(0xFFC62828)
        alert.priority == AlertPriority.MEDIUM -> Color(0xFFEF6C00)
        else -> Color(0xFF616161)
    }

    val icon = when (alert.type) {
        AlertType.HEARTBEAT_DETECTED -> Icons.Default.Favorite
        AlertType.RESPIRATION_DETECTED -> Icons.Default.Air
        AlertType.PROBE_FAILURE -> Icons.Default.Warning
        AlertType.SIGNAL_ANOMALY -> Icons.Default.ErrorOutline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = contentColor)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                if (alert.isResolved) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Resolved", tint = contentColor)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val format = SimpleDateFormat("HH:mm:ss | MMM dd", Locale.getDefault())
                Text(
                    text = format.format(Date(alert.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )

                if (!alert.isResolved) {
                    TextButton(onClick = onResolve) {
                        Text("MARK AS RESOLVED", color = contentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
