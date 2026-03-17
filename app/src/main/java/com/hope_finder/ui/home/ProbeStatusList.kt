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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope_finder.data.model.ConnectedProbe
import com.hope_finder.ui.theme.CmdActive
import com.hope_finder.ui.theme.CmdAlert
import com.hope_finder.ui.theme.CmdBackground
import com.hope_finder.ui.theme.CmdDivider
import com.hope_finder.ui.theme.CmdOffline
import com.hope_finder.ui.theme.CmdOnBackground
import com.hope_finder.ui.theme.CmdPrimary
import com.hope_finder.ui.theme.CmdSurface
import com.hope_finder.ui.theme.CmdSurfaceVariant
import com.hope_finder.ui.theme.CmdWarning

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun probeStatusColor(status: String) = when (status.lowercase()) {
    "online"  -> CmdActive
    "busy"    -> CmdWarning
    "offline" -> CmdOffline
    else      -> CmdOffline
}

private fun batteryIcon(level: Int) = when {
    level > 60 -> Icons.Default.BatteryFull
    level > 20 -> Icons.Default.Battery4Bar
    else       -> Icons.Default.BatteryAlert
}

private fun batteryColor(level: Int) = when {
    level > 60 -> CmdActive
    level > 20 -> CmdWarning
    else       -> CmdAlert
}

// ── Signal Bars ───────────────────────────────────────────────────────────────

@Composable
private fun SignalBars(signalPercent: Int, modifier: Modifier = Modifier) {
    val bars = 4
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        (1..bars).forEach { bar ->
            val barH = (bar * 5 + 2).dp
            val filled = signalPercent >= bar * (100 / bars)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barH)
                    .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                    .background(
                        if (filled) CmdPrimary else CmdDivider
                    )
                    .align(Alignment.Bottom)
            )
        }
    }
}

// ── Probe Card ────────────────────────────────────────────────────────────────

@Composable
fun ProbeStatusCard(probe: ConnectedProbe) {
    val statusColor = probeStatusColor(probe.status)
    // Derive fake signal strength from batteryLevel for ConnectedProbe (no signalStrength field)
    val estimatedSignal = (probe.batteryLevel * 1.1f).toInt().coerceIn(0, 100)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = CmdSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier  = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Probe icon
            Icon(
                Icons.Default.SettingsInputAntenna,
                contentDescription = null,
                tint = CmdPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // ID + status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = probe.name,
                    color = CmdOnBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text  = probe.status.uppercase(),
                    color = statusColor,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Signal bars
            SignalBars(signalPercent = estimatedSignal)
            Spacer(modifier = Modifier.width(12.dp))

            // Battery
            Icon(
                batteryIcon(probe.batteryLevel),
                contentDescription = "Battery",
                tint = batteryColor(probe.batteryLevel),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text  = "${probe.batteryLevel}%",
                color = batteryColor(probe.batteryLevel),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Battery progress bar
        LinearProgressIndicator(
            progress = { probe.batteryLevel / 100f },
            modifier  = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color     = batteryColor(probe.batteryLevel),
            trackColor = CmdDivider
        )
    }
}

// ── Full Panel ────────────────────────────────────────────────────────────────

@Composable
fun ProbeStatusList(
    probes: List<ConnectedProbe>,
    maxVisible: Int = 6
) {
    Column {
        ConsoleSectionHeader(title = "ACTIVE PROBES", icon = Icons.Default.Wifi)
        Spacer(modifier = Modifier.height(10.dp))

        if (probes.isEmpty()) {
            EmptyStateRow(label = "NO PROBES CONNECTED")
        } else {
            val visible = probes.take(maxVisible)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                visible.forEach { probe ->
                    ProbeStatusCard(probe = probe)
                }
            }
            if (probes.size > maxVisible) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text  = "+${probes.size - maxVisible} more probes…",
                    color = CmdPrimary.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0B0F1A)
@Composable
private fun ProbeStatusListPreview() {
    ProbeStatusList(
        probes = listOf(
            ConnectedProbe("p01", "PROBE-ALPHA", 91, "Online"),
            ConnectedProbe("p02", "PROBE-BRAVO", 45, "Busy"),
            ConnectedProbe("p03", "PROBE-CHARLIE", 12, "Offline"),
            ConnectedProbe("p04", "PROBE-DELTA", 78, "Online")
        )
    )
}
