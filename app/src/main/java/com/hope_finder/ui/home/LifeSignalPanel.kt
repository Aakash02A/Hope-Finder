package com.hope_finder.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope_finder.data.model.LifeSignal
import com.hope_finder.data.model.RadarProbeData
import com.hope_finder.ui.theme.CmdAlert
import com.hope_finder.ui.theme.CmdBackground
import com.hope_finder.ui.theme.CmdDivider
import com.hope_finder.ui.theme.CmdOnBackground
import com.hope_finder.ui.theme.CmdPrimary
import com.hope_finder.ui.theme.CmdSurface
import com.hope_finder.ui.theme.CmdSurfaceVariant
import com.hope_finder.ui.theme.CmdWarning
import kotlin.math.sin

// waveform

@Composable
fun WaveformGraph(
    samples: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue  = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val wavePoints: List<Float> = if (samples.isNotEmpty()) samples
    else (0 until 80).map { i ->
        val t = i / 80.0
        val v = sin(t * 6 * Math.PI + phase) * 0.4 +
                sin(t * 18 * Math.PI + phase * 2) * 0.15 +
                if (i % 20 == 0) 0.9 else 0.0
        v.toFloat()
    }

    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (wavePoints.size < 2) return@Canvas

        val maxVal = wavePoints.maxOrNull()?.coerceAtLeast(0.01f) ?: 1f
        val minVal = wavePoints.minOrNull() ?: 0f
        val range  = (maxVal - minVal).coerceAtLeast(0.01f)
        val stepX  = size.width / (wavePoints.size - 1).toFloat()

        val path = Path()
        wavePoints.forEachIndexed { index, v ->
            val x = index * stepX
            val normalised = (v - minVal) / range
            val y = size.height - (normalised * size.height * 0.9f + size.height * 0.05f)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color = color.copy(alpha = 0.25f),
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(path, color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// telemetry metric

@Composable
private fun TelemetryMetric(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.Text(
            text  = value,
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(text = unit,  color = color.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = label, color = CmdOnBackground.copy(alpha = 0.5f), fontSize = 9.sp)
    }
}

// life signal card

@Composable
fun LifeSignalCard(
    signal: LifeSignal,
    radarData: RadarProbeData? = null
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CmdSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingDot(color = CmdAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "LIFE SIGNAL", color = CmdAlert,
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = CmdPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = signal.location, color = CmdPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            WaveformGraph(
                samples  = radarData?.heartbeatSignal ?: emptyList(),
                color    = CmdAlert,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CmdBackground.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TelemetryMetric(
                    label = "HEARTBEAT",
                    value = radarData?.heartbeatSignal?.lastOrNull()
                        ?.let { (it * 80 + 60).toInt().toString() } ?: "--",
                    unit  = "BPM",
                    color = CmdAlert
                )
                VerticalDivider()
                TelemetryMetric(
                    label = "RESPIRATION",
                    value = radarData?.respirationRate?.toInt()?.toString() ?: "--",
                    unit  = "RPM",
                    color = CmdPrimary
                )
                VerticalDivider()
                TelemetryMetric(
                    label = "SIGNAL STR",
                    value = "${radarData?.signalStrength?.toInt() ?: signal.confidence}",
                    unit  = "%",
                    color = CmdWarning
                )
                VerticalDivider()
                TelemetryMetric(
                    label = "DEPTH",
                    value = radarData?.scanDepth?.let { "${it.toInt()}m" } ?: "--",
                    unit  = "m",
                    color = CmdPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "CONF: ${signal.confidence}%", color = CmdOnBackground.copy(alpha = 0.5f),
                    fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(text = "DETECTING", color = CmdAlert, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// full panel

@Composable
fun LifeSignalPanel(
    signals: List<LifeSignal>,
    radarData: RadarProbeData? = null
) {
    Column {
        ConsoleSectionHeader(title = "LIVE LIFE SIGNALS", icon = Icons.Default.Favorite, tint = CmdAlert)
        Spacer(modifier = Modifier.height(10.dp))
        if (signals.isEmpty()) {
            EmptyStateRow(label = "NO LIFE SIGNALS DETECTED")
        } else {
            signals.forEach { signal ->
                LifeSignalCard(signal = signal, radarData = radarData)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// shared sub-components

@Composable
fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(modifier = Modifier.size(8.dp).background(color.copy(alpha = alpha), CircleShape))
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(CmdDivider))
}

@Composable
fun ConsoleSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = CmdPrimary
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text  = title,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun EmptyStateRow(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CmdSurface),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = CmdOnBackground.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

// preview

@Preview(showBackground = true, backgroundColor = 0xFF0B0F1A)
@Composable
private fun LifeSignalPanelPreview() {
    LifeSignalPanel(
        signals = listOf(
            LifeSignal("s1", "Sector 4-B, Depth 3m", 87, System.currentTimeMillis()),
            LifeSignal("s2", "Sector 2-A, Depth 5m", 62, System.currentTimeMillis())
        ),
        radarData = RadarProbeData(
            probeId           = "probe_01",
            heartbeatSignal   = (0..40).map { i -> (sin(i.toDouble() * 0.4) * 0.5 + 0.5).toFloat() },
            respirationRate   = 18,
            signalStrength    = 84,
            scanDepth         = 3.5f,
            timestamp         = 0L
        )
    )
}
