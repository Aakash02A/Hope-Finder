package com.hope_finder.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope_finder.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

data class RadarTarget(
    val angle: Float, // 0-360 degrees
    val distance: Float, // 0-1 (normalized)
    val type: RadarTargetType = RadarTargetType.LIFE_SIGNATURE,
    val signal: Float = 1f // 0-1 signal strength
)

enum class RadarTargetType {
    LIFE_SIGNATURE, // Red
    MOVEMENT,       // Yellow
    OBJECT,         // Blue
    WEAK_SIGNAL     // Gray
}

fun RadarTargetType.color(): Color = when (this) {
    RadarTargetType.LIFE_SIGNATURE -> SaasError        // Red
    RadarTargetType.MOVEMENT -> SaasWarning            // Yellow
    RadarTargetType.OBJECT -> SaasPrimary              // Blue
    RadarTargetType.WEAK_SIGNAL -> SaasTextSecond      // Gray
}

@Composable
fun CircularRadarView(
    targets: List<RadarTarget> = emptyList(),
    isScanning: Boolean = true,
    modifier: Modifier = Modifier,
    scannerColor: Color = SaasSuccess
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_scan")
    
    // Rotating sweep animation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    // Pulsing glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(SaasWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = minOf(size.width, size.height) / 2f - 20f

            // ── Background circles (range rings) ──────────────────────────
            val ringCount = 4
            for (i in 1..ringCount) {
                val radius = maxRadius * (i.toFloat() / ringCount)
                // Circle outline
                drawCircle(
                    color = SaasBgSecondary,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.5f)
                )
            }

            // ── Crosshair / grid lines ────────────────────────────────────
            val crosshairColor = SaasStroke.copy(alpha = 0.3f)
            val crosshairLength = maxRadius * 1.1f
            
            // Horizontal line
            drawLine(
                color = crosshairColor,
                start = Offset(centerX - crosshairLength, centerY),
                end = Offset(centerX + crosshairLength, centerY),
                strokeWidth = 1f
            )
            // Vertical line
            drawLine(
                color = crosshairColor,
                start = Offset(centerX, centerY - crosshairLength),
                end = Offset(centerX, centerY + crosshairLength),
                strokeWidth = 1f
            )

            // ── Diagonal lines (45 degree angles) ──────────────────────────
            val diagonalLength = maxRadius * 1.1f
            val angle45 = PI.toFloat() / 4f
            for (baseAngle in listOf(0f, 90f, 180f, 270f)) {
                val rad = Math.toRadians(baseAngle.toDouble()).toFloat() + angle45
                drawLine(
                    color = crosshairColor,
                    start = Offset(
                        centerX - diagonalLength * cos(rad),
                        centerY - diagonalLength * sin(rad)
                    ),
                    end = Offset(
                        centerX + diagonalLength * cos(rad),
                        centerY + diagonalLength * sin(rad)
                    ),
                    strokeWidth = 0.8f
                )
            }

            // ── Labels for cardinal directions ────────────────────────────
            val labelDistance = maxRadius * 1.15f
            // Cardinal direction labels (N, S, E, W) are optional and can be added via Text composables

            // ── Draw radar targets ─────────────────────────────────────────
            targets.forEach { target ->
                val angleRad = Math.toRadians(target.angle.toDouble()).toFloat()
                val radius = maxRadius * target.distance.coerceIn(0f, 1f)
                
                val x = centerX + radius * cos(angleRad)
                val y = centerY + radius * sin(angleRad)

                val targetColor = target.type.color()
                
                // Outer glow
                drawCircle(
                    color = targetColor.copy(alpha = glowAlpha * 0.4f),
                    radius = 12f,
                    center = Offset(x, y)
                )

                // Inner circle
                drawCircle(
                    color = targetColor,
                    radius = 6f,
                    center = Offset(x, y)
                )
                
                // Signal strength indicator (concentric circles)
                drawCircle(
                    color = targetColor.copy(alpha = 0.3f),
                    radius = 8f,
                    center = Offset(x, y),
                    style = Stroke(width = 1.5f)
                )
            }

            // ── Animated sweep line ────────────────────────────────────────
            if (isScanning) {
                val sweepRad = Math.toRadians(sweepAngle.toDouble()).toFloat()
                val sweepStartX = centerX
                val sweepStartY = centerY
                val sweepEndX = centerX + maxRadius * cos(sweepRad)
                val sweepEndY = centerY + maxRadius * sin(sweepRad)

                // Main sweep line
                drawLine(
                    color = scannerColor,
                    start = Offset(sweepStartX, sweepStartY),
                    end = Offset(sweepEndX, sweepEndY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Glow effect on sweep line
                drawLine(
                    color = scannerColor.copy(alpha = 0.3f),
                    start = Offset(sweepStartX, sweepStartY),
                    end = Offset(sweepEndX, sweepEndY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )

                // Trail effect
                for (i in 1..8) {
                    val trailAngle = sweepAngle - i * 3f
                    val trailRad = Math.toRadians(trailAngle.toDouble()).toFloat()
                    val trailEndX = centerX + maxRadius * cos(trailRad)
                    val trailEndY = centerY + maxRadius * sin(trailRad)
                    
                    drawLine(
                        color = scannerColor.copy(alpha = (0.25f - i * 0.03f).coerceAtLeast(0f)),
                        start = Offset(sweepStartX, sweepStartY),
                        end = Offset(trailEndX, trailEndY),
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round
                    )
                }

                // Center pulse
                drawCircle(
                    color = scannerColor.copy(alpha = glowAlpha * 0.5f),
                    radius = 8f,
                    center = Offset(centerX, centerY)
                )
                
                drawCircle(
                    color = scannerColor,
                    radius = 3f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}

@Composable
fun RadarSectorIndicator(
    sector: String,
    detectionCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SaasBgSecondary)
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Sector $sector",
                fontSize = 11.sp,
                color = SaasTextSecond,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = detectionCount.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SaasSuccess
            )
        }
    }
}
