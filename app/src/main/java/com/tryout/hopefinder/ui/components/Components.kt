package com.tryout.hopefinder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tryout.hopefinder.ui.theme.*

/**
 * Professional Status Badge for system status indicators
 * Shows connected/disconnected state with high visibility colors
 */
@Composable
fun StatusBadge(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) StatusSuccessEmerald else BorderSubtle
    val textColor = if (isActive) Color.White else TextSecondaryGray
    
    Row(
        modifier = modifier
            .background(backgroundColor.copy(alpha = if (isActive) 0.8f else 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.HighlightOff,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Motion level indicator with confidence-based color coding
 * Provides visual urgency indication for detections
 */
@Composable
fun MotionLevelIndicator(
    motionLevel: String,
    confidence: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence > 75 -> MotionIndicatorRed
        confidence > 40 -> MotionIndicatorOrange
        confidence > 15 -> MotionIndicatorYellow
        else -> StatusInfoBlue
    }
    
    val level = when {
        confidence > 75 -> "CRITICAL"
        confidence > 40 -> "HIGH"
        confidence > 15 -> "MEDIUM"
        else -> "LOW"
    }
    
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(2.dp, color, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = level,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$confidence% confidence",
                color = TextSecondaryGray,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Radar,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Professional signal strength meter/bar visualization
 */
@Composable
fun SignalStrengthMeter(
    strength: Float, // 0-1.0
    label: String = "Signal Strength",
    modifier: Modifier = Modifier
) {
    val barColor = when {
        strength > 0.7f -> StatusSuccessEmerald
        strength > 0.4f -> StatusWarningAmber
        else -> StatusCriticalRed
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimaryWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(strength * 100).toInt()}%",
                color = barColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(BorderSubtle, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(strength)
                    .background(barColor, RoundedCornerShape(3.dp))
            )
        }
    }
}

/**
 * Circular confidence score display with color-coded background
 */
@Composable
fun ConfidenceScore(
    confidence: Int, // 0-100
    label: String = "Confidence",
    modifier: Modifier = Modifier
) {
    val color = when {
        confidence >= 75 -> StatusCriticalRed
        confidence >= 41 -> MotionIndicatorOrange
        confidence >= 16 -> StatusWarningAmber
        else -> StatusInfoBlue
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color.copy(alpha = 0.1f), CircleShape)
                .border(3.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$confidence",
                    color = color,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = label,
            color = TextSecondaryGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Professional alert card for displaying detection alerts
 */
@Composable
fun AlertCard(
    confidence: Int,
    sector: String,
    angle: Int,
    timestamp: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severity = when {
        confidence >= 75 -> "CRITICAL"
        confidence >= 60 -> "HIGH"
        else -> "MEDIUM"
    }
    
    val severityColor = when (severity) {
        "CRITICAL" -> StatusCriticalRed
        "HIGH" -> MotionIndicatorOrange
        else -> StatusWarningAmber
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, severityColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(severityColor, CircleShape)
                    )
                    Text(
                        text = "ALERT - $sector ($angle°)",
                        color = TextPrimaryWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$confidence% Confidence • Severity: $severity",
                    color = TextSecondaryGray,
                    fontSize = 11.sp
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = TextSecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Professional device status summary card
 */
@Composable
fun DeviceInfoCard(
    wifiConnected: Boolean,
    radarHealthy: Boolean,
    calibrated: Boolean,
    scanning: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Device Status",
                color = TextPrimaryWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge("Wi-Fi", wifiConnected, Modifier.weight(1f))
                StatusBadge("Radar", radarHealthy, Modifier.weight(1f))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge("Calibrated", calibrated, Modifier.weight(1f))
                StatusBadge("Scanning", scanning, Modifier.weight(1f))
            }
        }
    }
}

/**
 * Professional action button with elevated design
 */
@Composable
fun EmergencyButton(
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    icon: androidx.compose.material.icons.Icons.Filled? = null
) {
    val backgroundColor = if (isDestructive) StatusCriticalRed else AccentCyan
    val disabledBackground = BorderSubtle
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) backgroundColor else disabledBackground,
            contentColor = if (enabled) Color.White else TextTertiaryGray,
            disabledContainerColor = disabledBackground
        ),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(10.dp),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextPrimaryWhite,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else TextTertiaryGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
