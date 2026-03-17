package com.hope_finder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SignalWifiStatusbarConnectedNoInternet4
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope_finder.data.model.SystemAlert
import com.hope_finder.ui.theme.CmdAlert
import com.hope_finder.ui.theme.CmdDivider
import com.hope_finder.ui.theme.CmdOnBackground
import com.hope_finder.ui.theme.CmdSurface
import com.hope_finder.ui.theme.CmdSurfaceVariant
import com.hope_finder.ui.theme.CmdWarning
import com.hope_finder.ui.theme.CmdActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Priority mapping ──────────────────────────────────────────────────────────

private data class AlertStyle(
    val color: Color,
    val badgeLabel: String,
    val icon: ImageVector
)

private fun alertStyle(severity: String): AlertStyle = when (severity.lowercase()) {
    "high" -> AlertStyle(CmdAlert,   "HIGH",   Icons.Default.Error)
    "medium" -> AlertStyle(CmdWarning, "MED",  Icons.Default.Warning)
    else   -> AlertStyle(CmdActive,  "LOW",    Icons.Default.NotificationsActive)
}

private fun formatTimestamp(ts: Long): String {
    if (ts == 0L) return "--:--"
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ts))
}

// ── Alert Card ────────────────────────────────────────────────────────────────

@Composable
fun AlertCard(alert: SystemAlert) {
    val style = alertStyle(alert.severity)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(
            containerColor = style.color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(style.color)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Icon
            Icon(
                style.icon,
                contentDescription = null,
                tint = style.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Message
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = alert.message,
                    color = CmdOnBackground,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text  = formatTimestamp(alert.timestamp),
                    color = CmdOnBackground.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Priority badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(style.color.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = style.badgeLabel,
                    color = style.color,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ── Full Panel ────────────────────────────────────────────────────────────────

@Composable
fun AlertPanel(alerts: List<SystemAlert>) {
    Column {
        ConsoleSectionHeader(
            title = "ALERT CENTER",
            icon  = Icons.Default.NotificationsActive,
            tint  = CmdAlert
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (alerts.isEmpty()) {
            EmptyStateRow(label = "ALL SYSTEMS NOMINAL")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                alerts.forEach { alert ->
                    AlertCard(alert = alert)
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0B0F1A)
@Composable
private fun AlertPanelPreview() {
    AlertPanel(
        alerts = listOf(
            SystemAlert("a1", "Life signal detected at Sector 4-B — immediate response required", "High", System.currentTimeMillis()),
            SystemAlert("a2", "PROBE-BRAVO signal anomaly — intermittent packet loss", "Medium", System.currentTimeMillis() - 120_000),
            SystemAlert("a3", "PROBE-CHARLIE battery below 15%", "Low", System.currentTimeMillis() - 300_000)
        )
    )
}
