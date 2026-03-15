package com.hope_finder.ui.radar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    navController: NavController,
    probeId: String = "probe_01", // Default ID for demonstration
    viewModel: RadarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(probeId) {
        viewModel.startScanning(probeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Radar Scan: $probeId") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Radar Visualization
            RadarAnimation(modifier = Modifier.size(280.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Pulse & Bio Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BioMetricCard(
                    title = "Heartbeat",
                    value = if (uiState.radarData.heartbeatSignal.isNotEmpty()) "${uiState.radarData.heartbeatSignal.last().toInt()} BPM" else "--",
                    unit = "BPM",
                    icon = "❤️",
                    modifier = Modifier.weight(1f)
                )
                BioMetricCard(
                    title = "Respiration",
                    value = "${uiState.radarData.respirationRate}",
                    unit = "RPM",
                    icon = "🫁",
                    modifier = Modifier.weight(1f)
                )
            }

            // Signal Waveform
            WaveformGraph(
                signals = uiState.radarData.heartbeatSignal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Black, MaterialTheme.shapes.medium)
                    .padding(8.dp)
            )

            // Depth & Signal Strength
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Scan Depth", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text("${uiState.radarData.scanDepth} Meters", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Signal Strength", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        LinearProgressIndicator(
                            progress = { uiState.radarData.signalStrength / 100f },
                            modifier = Modifier.width(100.dp).height(8.dp),
                            color = if (uiState.radarData.signalStrength > 70) Color.Green else Color.Yellow,
                            trackColor = Color.DarkGray,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text("${uiState.radarData.signalStrength}%", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
            
            Text(
                "STATUS: ANALYZING SIGNALS",
                color = Color(0xFF00FF00),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RadarAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAngle"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        // Background Circles
        for (i in 1..4) {
            drawCircle(
                color = Color(0xFF00FF00).copy(alpha = 0.2f),
                radius = radius * (i / 4f),
                center = center,
                style = Stroke(width = 1.dp)
            )
        }

        // Radar Sweep
        val sweepAngle = angle * (PI / 180).toFloat()
        val endX = center.x + radius * cos(sweepAngle)
        val endY = center.y + radius * sin(sweepAngle)

        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, Color(0xFF00FF00).copy(alpha = 0.5f)),
                center = center
            ),
            startAngle = angle - 45f,
            sweepAngle = 45f,
            useCenter = true,
            size = size
        )

        drawLine(
            color = Color(0xFF00FF00),
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun WaveformGraph(signals: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (signals.size < 2) return@Canvas
        
        val path = Path()
        val width = size.width
        val height = size.height
        val maxPoints = 50
        val displaySignals = signals.takeLast(maxPoints)
        val dx = width / (maxPoints - 1)

        displaySignals.forEachIndexed { index, value ->
            // Normalize value assuming 0-100 range for heartbeat pulse
            val y = height - (value / 100f * height)
            val x = index * dx
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFF00FF00),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun BioMetricCard(title: String, value: String, unit: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
