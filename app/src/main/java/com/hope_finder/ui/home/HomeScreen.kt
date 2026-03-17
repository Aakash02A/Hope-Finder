package com.hope_finder.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.hope_finder.data.model.DashboardStats
import com.hope_finder.data.model.RadarCell
import com.hope_finder.ui.theme.CmdActive
import com.hope_finder.ui.theme.CmdAlert
import com.hope_finder.ui.theme.CmdBackground
import com.hope_finder.ui.theme.CmdDivider
import com.hope_finder.ui.theme.CmdOnBackground
import com.hope_finder.ui.theme.CmdPrimary
import com.hope_finder.ui.theme.CmdSurface
import com.hope_finder.ui.theme.CmdSurfaceVariant
import com.hope_finder.ui.theme.CmdWarning

// screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hexCells = rememberHexCells(uiState.radarCells)
    var selectedHex by remember { mutableStateOf<HexCellData?>(null) }

    Scaffold(
        topBar = {
            CommandTopBar(
                onlineProbes = uiState.stats.probesOnline,
                alertCount   = uiState.stats.rescueAlerts,
                unreadAlerts = uiState.alerts.count { it.severity == "High" }
            )
        },
        containerColor = CmdBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(CmdBackground),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { KpiRow(stats = uiState.stats) }
            item { HorizontalDivider(color = CmdDivider, thickness = 1.dp) }
            item {
                SectionLabel(title = "RADAR COVERAGE MAP")
                Spacer(modifier = Modifier.height(10.dp))
                HexGridRadarView(
                    cells = hexCells,
                    cols  = 7,
                    rows  = 5,
                    onCellClick = { cell -> selectedHex = cell }
                )
                selectedHex?.let { cell ->
                    Spacer(modifier = Modifier.height(8.dp))
                    SelectedHexInfo(cell = cell)
                }
            }
            item { HorizontalDivider(color = CmdDivider, thickness = 1.dp) }
            item { LifeSignalPanel(signals = uiState.lifeSignals) }
            item { HorizontalDivider(color = CmdDivider, thickness = 1.dp) }
            item { ProbeStatusList(probes = uiState.probes) }
            item { HorizontalDivider(color = CmdDivider, thickness = 1.dp) }
            item { AlertPanel(alerts = uiState.alerts) }
        }
    }
}

// top bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandTopBar(
    onlineProbes: Int,
    alertCount: Int,
    unreadAlerts: Int
) {
    val pulse = rememberInfiniteTransition(label = "top_pulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "top_dot"
    )

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CmdPrimary.copy(alpha = dotAlpha), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text  = "HOPE FINDER",
                        color = CmdPrimary,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text  = "DISASTER RESCUE MONITORING",
                        color = CmdOnBackground.copy(alpha = 0.55f),
                        fontSize   = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CmdActive.copy(alpha = 0.12f))
                    .border(1.dp, CmdActive.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CmdActive.copy(alpha = dotAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text  = "SYS ONLINE  P:$onlineProbes  A:$alertCount",
                        color = CmdActive,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = CmdOnBackground)
                }
                if (unreadAlerts > 0) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(CmdAlert, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = unreadAlerts.toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = CmdOnBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CmdSurface,
            scrolledContainerColor = CmdSurface
        )
    )
}

// kpi row

@Composable
private fun KpiRow(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard("PROBES",  stats.probesOnline.toString(),        Icons.Default.Wifi,          CmdActive,  Modifier.weight(1f))
        KpiCard("ZONES",   stats.activeScanZones.toString(),     Icons.Default.Map,            CmdPrimary, Modifier.weight(1f))
        KpiCard("SIGNALS", stats.lifeSignalsDetected.toString(), Icons.Default.Favorite,       CmdAlert,   Modifier.weight(1f))
        KpiCard("ALERTS",  stats.rescueAlerts.toString(),        Icons.Default.Notifications,  CmdWarning, Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CmdSurfaceVariant)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = value,
                color = color,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text  = label,
                color = CmdOnBackground.copy(alpha = 0.5f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}

// section label

@Composable
private fun SectionLabel(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CmdPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text  = title,
            color = CmdPrimary,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}

// selected hex info

@Composable
private fun SelectedHexInfo(cell: HexCellData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CmdSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = "CELL: ${cell.id.uppercase()}",
            color = CmdPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(cell.state.color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text  = cell.state.name.replace('_', ' '),
                color = cell.state.color,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// hex cell builder

@Composable
private fun rememberHexCells(radarCells: List<RadarCell>): List<HexCellData> {
    val cols = 7
    val rows = 5
    return remember(radarCells) {
        val mapped = radarCells.mapIndexed { idx, cell ->
            HexCellData(
                id    = cell.id.ifBlank { "cell_$idx" },
                col   = idx % cols,
                row   = idx / cols,
                state = cell.toHexCellState(),
                label = cell.name
            )
        }
        val occupied = mapped.map { it.col to it.row }.toSet()
        val placeholders = (0 until cols).flatMap { col ->
            (0 until rows).map { row ->
                HexCellData(id = "empty_${col}_${row}", col = col, row = row, state = HexCellState.OFFLINE)
            }
        }.filter { (it.col to it.row) !in occupied }
        mapped + placeholders
    }
}
