package com.hope_finder.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hope_finder.data.model.Alert
import com.hope_finder.ui.theme.SaasBgPrimary
import com.hope_finder.ui.theme.SaasError
import com.hope_finder.ui.theme.SaasSuccess
import com.hope_finder.ui.theme.SaasText
import com.hope_finder.ui.theme.SaasTextSecond
import com.hope_finder.ui.theme.SaasWarning
import com.hope_finder.ui.theme.SaasWhite
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController,
    viewModel: AlertViewModel = hiltViewModel(),
    isInBottomNav: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SaasText)
                        Text("Active alerts and notifications", fontSize = 12.sp, color = SaasTextSecond)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaasWhite
                )
            )
        },
        containerColor = SaasBgPrimary
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SaasText)
            }
        } else if (uiState.alerts.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(SaasBgPrimary), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaasSuccess, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No active alerts", color = SaasTextSecond, fontSize = 14.sp)
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
                items(uiState.alerts, key = { it.id }) { alert ->
                    SaasAlertCard(
                        alert = alert,
                        onResolve = { viewModel.resolveAlert(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SaasAlertCard(alert: Alert, onResolve: () -> Unit) {
    val (contentColor, backgroundColor) = when {
        alert.isResolved -> SaasSuccess to SaasSuccess.copy(alpha = 0.1f)
        alert.priority.equals("HIGH", ignoreCase = true) -> SaasError to SaasError.copy(alpha = 0.1f)
        alert.priority.equals("MEDIUM", ignoreCase = true) -> SaasWarning to SaasWarning.copy(alpha = 0.1f)
        else -> SaasText to SaasBgPrimary
    }

    val icon = when (alert.type.uppercase()) {
        "HEARTBEAT_DETECTED" -> Icons.Default.Favorite
        "RESPIRATION_DETECTED" -> Icons.Default.Air
        "PROBE_FAILURE" -> Icons.Default.Warning
        "LIFE_DETECTION" -> Icons.Default.Favorite
        else -> Icons.Outlined.ErrorOutline
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SaasWhite)
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaasText
                        )
                        Text(
                            text = alert.message,
                            fontSize = 11.sp,
                            color = SaasTextSecond,
                            maxLines = 2
                        )
                    }
                }
                if (alert.isResolved) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Resolved", tint = SaasSuccess, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                Text(
                    text = format.format(Date(alert.timestamp)),
                    fontSize = 10.sp,
                    color = SaasTextSecond
                )

                if (!alert.isResolved) {
                    TextButton(
                        onClick = onResolve,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Resolve", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = contentColor)
                    }
                }
            }
        }
    }
}
