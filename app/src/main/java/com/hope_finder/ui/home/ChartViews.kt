package com.hope_finder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.hope_finder.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class LineChartData(
    val label: String,
    val value: Float,
    val color: Color = SaasPrimary
)

data class PieChartSegment(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun LineChartView(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    title: String = "Chart",
    maxValue: Float = data.maxOfOrNull { it.value } ?: 100f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SaasWhite)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaasText
            )
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (data.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height
                val padding = 40f
                val graphWidth = chartWidth - padding * 2
                val graphHeight = chartHeight - padding

                // Y-axis line
                drawLine(
                    color = SaasStroke.copy(alpha = 0.3f),
                    start = Offset(padding, padding / 2),
                    end = Offset(padding, chartHeight),
                    strokeWidth = 1.5f
                )

                // X-axis line
                drawLine(
                    color = SaasStroke.copy(alpha = 0.3f),
                    start = Offset(padding, chartHeight),
                    end = Offset(chartWidth - padding / 2, chartHeight),
                    strokeWidth = 1.5f
                )

                // Grid lines
                repeat(5) { i ->
                    val yPos = chartHeight - (i * graphHeight / 5f)
                    drawLine(
                        color = SaasBgSecondary,
                        start = Offset(padding, yPos),
                        end = Offset(chartWidth - padding / 2, yPos),
                        strokeWidth = 0.8f
                    )
                }

                // Plot points and connect with lines
                val stepX = graphWidth / (data.size - 1f).coerceAtLeast(1f)
                val normalizedData = data.map { it.value / maxValue }

                for (i in 0 until data.size - 1) {
                    val x1 = padding + i * stepX
                    val y1 = chartHeight - (normalizedData[i] * graphHeight)
                    val x2 = padding + (i + 1) * stepX
                    val y2 = chartHeight - (normalizedData[i + 1] * graphHeight)

                    // Line connecting points
                    drawLine(
                        color = data[i].color,
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.5f
                    )

                    // Gradient fill under line
                    val path = Path().apply {
                        moveTo(x1, y1)
                        lineTo(x2, y2)
                        lineTo(x2, chartHeight)
                        lineTo(x1, chartHeight)
                        close()
                    }
                    drawPath(
                        path,
                        color = data[i].color.copy(alpha = 0.15f)
                    )
                }

                // Plot points
                for (i in data.indices) {
                    val x = padding + i * stepX
                    val y = chartHeight - (normalizedData[i] * graphHeight)

                    // Point glow
                    drawCircle(
                        color = data[i].color.copy(alpha = 0.2f),
                        radius = 8f,
                        center = Offset(x, y)
                    )

                    // Point
                    drawCircle(
                        color = data[i].color,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(item.color)
                            )
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                color = SaasText
                            )
                        }
                        Text(
                            text = "${item.value.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartView(
    segments: List<PieChartSegment>,
    modifier: Modifier = Modifier,
    title: String = "Distribution",
    centerContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SaasWhite)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SaasText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Chart
                Canvas(
                    modifier = Modifier
                        .size(150.dp)
                        .weight(0.4f)
                ) {
                    if (segments.isEmpty()) return@Canvas

                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val radius = minOf(size.width, size.height) / 2f

                    val total = segments.sumOf { it.value.toDouble() }.toFloat()
                    var currentStartAngle = -90f

                    segments.forEach { segment ->
                        val sweepAngle = (segment.value / total) * 360f

                        // Draw segment
                        drawArc(
                            color = segment.color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = androidx.compose.ui.geometry.Size(
                                radius * 2,
                                radius * 2
                            ),
                            topLeft = Offset(centerX - radius, centerY - radius)
                        )

                        // Draw border
                        drawArc(
                            color = SaasWhite,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = androidx.compose.ui.geometry.Size(
                                radius * 2,
                                radius * 2
                            ),
                            topLeft = Offset(centerX - radius, centerY - radius),
                            style = Stroke(width = 2f)
                        )

                        currentStartAngle += sweepAngle
                    }

                    // Center circle (donut effect)
                    drawCircle(
                        color = SaasWhite,
                        radius = radius * 0.55f,
                        center = Offset(centerX, centerY)
                    )
                }

                // Legend
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val total = segments.sumOf { it.value.toDouble() }.toFloat()
                    
                    segments.forEach { segment ->
                        val percentage = (segment.value / total * 100f).toInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(segment.color)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = segment.label,
                                    fontSize = 12.sp,
                                    color = SaasText,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${segment.value.toInt()} detections",
                                    fontSize = 10.sp,
                                    color = SaasTextSecond
                                )
                            }
                            Text(
                                text = "$percentage%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = segment.color
                            )
                        }
                    }
                }
            }
        }
    }
}
