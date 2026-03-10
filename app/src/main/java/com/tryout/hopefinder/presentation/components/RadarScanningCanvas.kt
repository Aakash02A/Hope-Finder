package com.tryout.hopefinder.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tryout.hopefinder.domain.model.DetectionType
import com.tryout.hopefinder.domain.model.RadarDetection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom Canvas-based radar UI showing concentric circles and a sweeping green scanning animation.
 * This is the core visual component of the Rescue Radar System.
 *
 * @param modifier Modifier for the composable.
 * @param isScanning Whether the radar is currently scanning.
 * @param currentSector The current sector being scanned (1-5).
 * @param detections List of current detections to display on the radar.
 * @param sweepDurationMs Duration of one full sweep rotation in milliseconds.
 */
@Composable
fun RadarScanningCanvas(
    modifier: Modifier = Modifier,
    isScanning: Boolean = false,
    currentSector: Int = 0,
    detections: List<RadarDetection> = emptyList(),
    sweepDurationMs: Int = 3000
) {
    // Infinite transition for the sweeping animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    
    // Sweep angle animation (0 to 360 degrees)
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = sweepDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )
    
    // Pulse animation for detections
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    // Glow intensity animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    // Colors
    val radarGreen = Color(0xFF00FF41)         // Neon green
    val radarGreenDark = Color(0xFF004D14)     // Dark green for background
    val radarGreenGlow = Color(0xFF00FF41)     // Glow color
    val gridColor = Color(0xFF003311)          // Grid lines
    val centerDotColor = Color(0xFF00FF41)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = min(size.width, size.height) / 2 * 0.9f
            
            // Draw outer glow effect
            if (isScanning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            radarGreenGlow.copy(alpha = glowAlpha * 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.1f
                    ),
                    radius = radius * 1.1f,
                    center = center
                )
            }
            
            // Draw concentric circles (range rings)
            val ringCount = 5
            for (i in 1..ringCount) {
                val ringRadius = radius * (i.toFloat() / ringCount)
                val ringAlpha = if (isScanning) 0.4f else 0.2f
                
                drawCircle(
                    color = gridColor.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                
                // Draw distance labels (2m, 4m, 6m, 8m, 10m)
                // Labels would be drawn with drawText in production
            }
            
            // Draw cross-hairs (cardinal directions)
            drawCrossHairs(
                center = center,
                radius = radius,
                color = gridColor.copy(alpha = 0.3f)
            )
            
            // Draw pentagon sector divisions (5 probes)
            drawPentagonSectors(
                center = center,
                radius = radius,
                color = gridColor.copy(alpha = 0.25f),
                highlightedSector = if (isScanning) currentSector else 0,
                highlightColor = radarGreen.copy(alpha = 0.15f)
            )
            
            // Draw sweep line with gradient trail (only when scanning)
            if (isScanning) {
                drawSweepLine(
                    center = center,
                    radius = radius,
                    angle = sweepAngle,
                    color = radarGreen,
                    glowColor = radarGreenGlow.copy(alpha = glowAlpha)
                )
            }
            
            // Draw detections
            detections.forEach { detection ->
                drawDetection(
                    detection = detection,
                    center = center,
                    maxRadius = radius,
                    pulseScale = if (isScanning) pulseScale else 1f
                )
            }
            
            // Draw center point
            drawCircle(
                color = centerDotColor,
                radius = 8f,
                center = center
            )
            
            // Draw center glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        radarGreen.copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = 30f
                ),
                radius = 30f,
                center = center
            )
        }
    }
}

/**
 * Draw cross-hairs on the radar.
 */
private fun DrawScope.drawCrossHairs(
    center: Offset,
    radius: Float,
    color: Color
) {
    // Horizontal line
    drawLine(
        color = color,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = 1f
    )
    
    // Vertical line
    drawLine(
        color = color,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = 1f
    )
    
    // Diagonal lines (45 degrees)
    val diagonalOffset = radius * 0.707f // cos(45°)
    
    drawLine(
        color = color.copy(alpha = color.alpha * 0.5f),
        start = Offset(center.x - diagonalOffset, center.y - diagonalOffset),
        end = Offset(center.x + diagonalOffset, center.y + diagonalOffset),
        strokeWidth = 0.5f
    )
    
    drawLine(
        color = color.copy(alpha = color.alpha * 0.5f),
        start = Offset(center.x + diagonalOffset, center.y - diagonalOffset),
        end = Offset(center.x - diagonalOffset, center.y + diagonalOffset),
        strokeWidth = 0.5f
    )
}

/**
 * Draw pentagon sectors representing the 5 radar probes.
 */
private fun DrawScope.drawPentagonSectors(
    center: Offset,
    radius: Float,
    color: Color,
    highlightedSector: Int,
    highlightColor: Color
) {
    val sectorAngle = 72f // 360° / 5 sectors
    val startAngle = -90f // Start from top
    
    for (i in 0 until 5) {
        val angle = startAngle + (i * sectorAngle)
        val angleRad = Math.toRadians(angle.toDouble())
        
        // Draw sector line
        val endX = center.x + (radius * cos(angleRad)).toFloat()
        val endY = center.y + (radius * sin(angleRad)).toFloat()
        
        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1f
        )
        
        // Highlight current sector being scanned
        if (highlightedSector == i + 1) {
            val nextAngle = startAngle + ((i + 1) * sectorAngle)
            drawSectorHighlight(
                center = center,
                radius = radius,
                startAngle = angle,
                sweepAngle = sectorAngle,
                color = highlightColor
            )
        }
    }
}

/**
 * Draw sector highlight arc.
 */
private fun DrawScope.drawSectorHighlight(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            ),
            startAngleDegrees = startAngle,
            sweepAngleDegrees = sweepAngle,
            forceMoveTo = false
        )
        close()
    }
    
    drawPath(
        path = path,
        color = color
    )
}

/**
 * Draw the sweeping radar line with a gradient trail.
 */
private fun DrawScope.drawSweepLine(
    center: Offset,
    radius: Float,
    angle: Float,
    color: Color,
    glowColor: Color
) {
    // Draw sweep trail (gradient arc behind the sweep line)
    val trailSweep = 45f // Trail spans 45 degrees
    
    rotate(degrees = angle - 90f, pivot = center) {
        // Draw gradient trail
        for (i in 0 until 30) {
            val trailAngle = -trailSweep * (i / 30f)
            val trailAlpha = (1f - (i / 30f)) * 0.3f
            
            rotate(degrees = trailAngle, pivot = center) {
                drawLine(
                    color = color.copy(alpha = trailAlpha),
                    start = center,
                    end = Offset(center.x, center.y - radius),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Draw main sweep line
        drawLine(
            color = color,
            start = center,
            end = Offset(center.x, center.y - radius),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        
        // Draw glow at the tip
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor,
                    Color.Transparent
                ),
                center = Offset(center.x, center.y - radius),
                radius = 20f
            ),
            radius = 20f,
            center = Offset(center.x, center.y - radius)
        )
    }
}

/**
 * Draw a detection blip on the radar.
 */
private fun DrawScope.drawDetection(
    detection: RadarDetection,
    center: Offset,
    maxRadius: Float,
    pulseScale: Float
) {
    // Calculate position based on probe ID (pentagon layout) and distance
    val sectorAngle = 72f
    val baseAngle = -90f + ((detection.probeId - 1) * sectorAngle) + (sectorAngle / 2)
    val angleRad = Math.toRadians(baseAngle.toDouble())
    
    // Distance determines how far from center (max 10 meters = full radius)
    val distanceRatio = (detection.distance / 10f).coerceIn(0f, 1f)
    val blipRadius = maxRadius * distanceRatio
    
    val blipX = center.x + (blipRadius * cos(angleRad)).toFloat()
    val blipY = center.y + (blipRadius * sin(angleRad)).toFloat()
    val blipCenter = Offset(blipX, blipY)
    
    // Color based on detection type
    val blipColor = when (detection.type) {
        DetectionType.HEARTBEAT -> Color(0xFFFF1744)  // Red for heartbeat
        DetectionType.BREATHING -> Color(0xFFFFEA00)   // Yellow for breathing
        DetectionType.MOVEMENT -> Color(0xFF2979FF)    // Blue for movement
    }
    
    // Size based on signal strength
    val baseSize = 8f + (detection.signalStrength / 100f * 8f)
    val animatedSize = baseSize * pulseScale
    
    // Draw outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                blipColor.copy(alpha = 0.6f),
                blipColor.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = blipCenter,
            radius = animatedSize * 2.5f
        ),
        radius = animatedSize * 2.5f,
        center = blipCenter
    )
    
    // Draw middle ring
    drawCircle(
        color = blipColor.copy(alpha = 0.5f),
        radius = animatedSize * 1.5f,
        center = blipCenter,
        style = Stroke(width = 2f)
    )
    
    // Draw center dot
    drawCircle(
        color = blipColor,
        radius = animatedSize * 0.6f,
        center = blipCenter
    )
}

/**
 * Preview of the radar scanning canvas.
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun RadarScanningCanvasPreview() {
    val sampleDetections = listOf(
        RadarDetection(
            id = "1",
            type = DetectionType.HEARTBEAT,
            zoneLocation = "Alpha-1",
            distance = 4.5f,
            signalStrength = 85,
            confidenceScore = 92f,
            probeId = 1
        ),
        RadarDetection(
            id = "2",
            type = DetectionType.BREATHING,
            zoneLocation = "Gamma-3",
            distance = 7.2f,
            signalStrength = 60,
            confidenceScore = 78f,
            probeId = 3
        ),
        RadarDetection(
            id = "3",
            type = DetectionType.MOVEMENT,
            zoneLocation = "Epsilon-5",
            distance = 2.1f,
            signalStrength = 95,
            confidenceScore = 88f,
            probeId = 5
        )
    )
    
    RadarScanningCanvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        isScanning = true,
        currentSector = 2,
        detections = sampleDetections
    )
}

/**
 * Preview of idle radar (not scanning).
 */
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun RadarScanningCanvasIdlePreview() {
    RadarScanningCanvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        isScanning = false,
        currentSector = 0,
        detections = emptyList()
    )
}
