package com.tryout.hopefinder.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tryout.hopefinder.data.DetectionEntity
import com.tryout.hopefinder.ui.theme.*
import com.tryout.hopefinder.viewmodel.*
import kotlin.math.*

/**
 * Professional Radar Screen - Real Radar Look
 */
@Composable
fun RadarScreen(
    context: android.content.Context? = null,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: RadarViewModel = viewModel(
        factory = context?.let { RadarViewModelFactory(it) }
    )
) {
    val currentAngle by viewModel.currentAngle.collectAsState(initial = 0)
    val currentSector by viewModel.currentSector.collectAsState(initial = 0)
    val recentDetections by viewModel.recentDetections.collectAsState(initial = emptyList())
    val scanStatus by viewModel.scanStatus.collectAsState(initial = null)
    
    var showHeatMap by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(paddingValues)
    ) {
        // Header
        RadarHeaderBar(
            showHeatMap = showHeatMap,
            onHeatMapToggle = { showHeatMap = it },
            onStartScan = { viewModel.startScan() },
            onStopScan = { viewModel.stopScan() }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Radar Display
            item {
                ProfessionalRadarDisplay(
                    currentAngle = currentAngle,
                    detections = recentDetections,
                    showHeatMap = showHeatMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .shadow(8.dp, CircleShape)
                )
            }

            // Statistics Section
            item {
                RadarStatsSection(detections = recentDetections)
            }

            // Signal Strength
            item {
                SignalStrengthDisplay(detections = recentDetections)
            }

            // Scan Info
            item {
                ScanInfoDisplay(
                    currentAngle = currentAngle,
                    sector = getSectorLabel(currentSector),
                    targetCount = recentDetections.size
                )
            }

            // Detection List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETECTED TARGETS",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "(${recentDetections.size})",
                        color = MotionIndicatorOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scan Status Message
            if (scanStatus != null) {
                item {
                    Surface(
                        color = SurfaceSlate.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = scanStatus ?: "",
                            color = AccentCyan,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Detection Items
            if (recentDetections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No targets detected",
                            color = Color.LightGray.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(
                    recentDetections.take(10),
                    key = { it.id }
                ) { detection ->
                    DetectionCard(detection = detection)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Header Bar with Controls
 */
@Composable
fun RadarHeaderBar(
    showHeatMap: Boolean,
    onHeatMapToggle: (Boolean) -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceSlate.copy(alpha = 0.95f),
                        BackgroundDeepNavy
                    )
                )
            )
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = BorderSubtle,
                    start = Offset(0f, size.height - strokeWidth / 2),
                    end = Offset(size.width, size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AccentCyan.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "RADAR SYSTEM",
                    color = TextPrimaryWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onHeatMapToggle(!showHeatMap) },
                modifier = Modifier
                    .weight(0.5f)
                    .height(40.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (showHeatMap) AccentCyan else TextSecondaryGray
                ),
                border = BorderStroke(
                    1.dp,
                    if (showHeatMap) AccentCyan else BorderSubtle
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("HEAT MAP", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onStartScan,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("START MISSION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onStopScan,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusCriticalRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TERMINATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Professional Military-Style Radar Display
 * Real radar look with dramatic sweep beam and enhanced visibility
 */
@Composable
fun ProfessionalRadarDisplay(
    currentAngle: Int,
    detections: List<DetectionEntity>,
    @Suppress("UNUSED_PARAMETER") showHeatMap: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(BackgroundDeepNavy, CircleShape)
            .border(3.dp, AccentCyan, CircleShape)
            .drawBehind {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = size.width / 2

                // Draw concentric circles (distance rings)
                for (i in 1..4) {
                    val radius = maxRadius * (i / 4f)
                    drawCircle(
                        color = AccentCyan.copy(alpha = 0.15f),
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                }

                // Crosshairs
                drawLine(
                    color = AccentCyan.copy(alpha = 0.25f),
                    start = Offset(centerX - maxRadius, centerY),
                    end = Offset(centerX + maxRadius, centerY),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = AccentCyan.copy(alpha = 0.25f),
                    start = Offset(centerX, centerY - maxRadius),
                    end = Offset(centerX, centerY + maxRadius),
                    strokeWidth = 1.5f
                )

                // Subtle diagonals
                drawLine(
                    color = AccentCyan.copy(alpha = 0.1f),
                    start = Offset(centerX - maxRadius * 0.7f, centerY - maxRadius * 0.7f),
                    end = Offset(centerX + maxRadius * 0.7f, centerY + maxRadius * 0.7f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = AccentCyan.copy(alpha = 0.1f),
                    start = Offset(centerX + maxRadius * 0.7f, centerY - maxRadius * 0.7f),
                    end = Offset(centerX - maxRadius * 0.7f, centerY + maxRadius * 0.7f),
                    strokeWidth = 1f
                )

                // SWEEP BEAM - 30 degree sweep width
                val sweepAngleRad = (currentAngle * PI) / 180.0
                
                // Wide sweep arc with strong gradient
                drawArc(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentCyan.copy(alpha = 0.6f),
                            AccentCyan.copy(alpha = 0.3f),
                            AccentCyan.copy(alpha = 0f)
                        ),
                        center = Offset(centerX, centerY),
                        radius = maxRadius * 0.95f
                    ),
                    startAngle = currentAngle.toFloat() - 15f,
                    sweepAngle = 30f,
                    useCenter = true,
                    size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2),
                    topLeft = Offset(centerX - maxRadius, centerY - maxRadius)
                )

                // Bright sweep line edges for dramatic effect
                val sweepLeftAngleRad = ((currentAngle - 15.0) * PI) / 180.0
                val sweepRightAngleRad = ((currentAngle + 15.0) * PI) / 180.0
                
                val sweepLeftEndX = centerX + maxRadius * 0.95f * cos(sweepLeftAngleRad).toFloat()
                val sweepLeftEndY = centerY + maxRadius * 0.95f * sin(sweepLeftAngleRad).toFloat()
                val sweepRightEndX = centerX + maxRadius * 0.95f * cos(sweepRightAngleRad).toFloat()
                val sweepRightEndY = centerY + maxRadius * 0.95f * sin(sweepRightAngleRad).toFloat()

                drawLine(
                    color = AccentCyan.copy(alpha = 0.4f),
                    start = Offset(centerX, centerY),
                    end = Offset(sweepLeftEndX, sweepLeftEndY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = AccentCyan.copy(alpha = 0.4f),
                    start = Offset(centerX, centerY),
                    end = Offset(sweepRightEndX, sweepRightEndY),
                    strokeWidth = 1f
                )

                // Main sweep beam line (center)
                val beamEndX = centerX + maxRadius * 0.98f * cos(sweepAngleRad).toFloat()
                val beamEndY = centerY + maxRadius * 0.98f * sin(sweepAngleRad).toFloat()
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentCyan, AccentCyan.copy(alpha = 0.5f)),
                        start = Offset(centerX, centerY),
                        end = Offset(beamEndX, beamEndY)
                    ),
                    start = Offset(centerX, centerY),
                    end = Offset(beamEndX, beamEndY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Beam head glow
                drawCircle(
                    color = AccentCyan,
                    radius = 4f,
                    center = Offset(beamEndX, beamEndY)
                )
            }
    ) {
        // Targets (Detections)
        detections.forEach { detection ->
            RadarTarget(
                detection = detection,
                currentAngle = currentAngle
            )
        }
    }
}

@Composable
fun RadarTarget(
    detection: DetectionEntity,
    currentAngle: Int
) {
    // Logic to determine if target should be visible (was recently swept)
    val angleDiff = abs(currentAngle - detection.angle)
    val alpha = when {
        angleDiff < 10 -> 1f
        angleDiff < 60 -> 1f - (angleDiff - 10) / 50f
        else -> 0f
    }

    if (alpha > 0) {
        BoxWithConstraints {
            val radius = min(maxWidth.value, maxHeight.value).dp / 2
            val centerX = maxWidth / 2
            val centerY = maxHeight / 2
            
            // Convert polar to cartesian
            val rad = (detection.angle * PI / 180.0).toFloat()
            // Approximate distance from signal strength (0-1)
            val distFactor = (1f - detection.signalStrength).coerceIn(0.1f, 0.9f)
            
            val targetX = centerX + radius * distFactor * cos(rad)
            val targetY = centerY + radius * distFactor * sin(rad)

            Box(
                modifier = Modifier
                    .offset(targetX - 6.dp, targetY - 6.dp)
                    .size(12.dp)
                    .drawBehind {
                        // Pulsing effect
                        drawCircle(
                            color = AccentCyan.copy(alpha = alpha * 0.3f),
                            radius = size.width * 1.5f
                        )
                        drawCircle(
                            color = AccentCyan.copy(alpha = alpha),
                            radius = size.width / 2.5f
                        )
                    }
            )
        }
    }
}

@Composable
fun RadarStatsSection(detections: List<DetectionEntity>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "ACTIVE TARGETS",
            value = detections.size.toString(),
            icon = Icons.Default.Radar,
            color = AccentCyan,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "THREAT LEVEL",
            value = if (detections.size > 5) "HIGH" else if (detections.size > 2) "MED" else "LOW",
            icon = Icons.Default.Warning,
            color = if (detections.size > 5) StatusCriticalRed else if (detections.size > 2) WarningYellow else SuccessGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = TextSecondaryGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun SignalStrengthDisplay(detections: List<DetectionEntity>) {
    val strength = if (detections.isEmpty()) 0.1f else 0.85f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SIGNAL STRENGTH",
                    color = TextSecondaryGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { strength },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (strength > 0.7f) SuccessGreen else WarningYellow,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${(strength * 100).toInt()}%",
                color = if (strength > 0.7f) SuccessGreen else WarningYellow,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ScanInfoDisplay(currentAngle: Int, sector: String, targetCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(label = "AZIMUTH", value = "$currentAngle\u00B0")
            InfoItem(label = "SECTOR", value = sector)
            InfoItem(label = "TARGETS", value = targetCount.toString())
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextSecondaryGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetectionCard(detection: DetectionEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DarkSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, AccentCyan.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Target ID: ${detection.id}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Pos: ${detection.angle}\u00B0 | Confidence: ${detection.confidence}%",
                    color = TextSecondaryGray,
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = detection.confidenceLevel,
                    color = if (detection.confidence > 70) SuccessGreen else if (detection.confidence > 40) WarningYellow else TextSecondaryGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tracked",
                    color = TextSecondaryGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun getSectorLabel(sector: Int): String {
    return when (sector) {
        0 -> "NORTH"
        1 -> "EAST"
        2 -> "SOUTH"
        3 -> "WEST"
        else -> "UNKNOWN"
    }
}
