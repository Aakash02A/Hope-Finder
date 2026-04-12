package com.tryout.hopefinder.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tryout.hopefinder.ui.theme.*
import com.tryout.hopefinder.viewmodel.ReportsViewModel
import com.tryout.hopefinder.viewmodel.ReportsViewModelFactory

/**
 * Reports Screen
 * Displays analytics, statistics, and export options
 */
@Composable
fun ReportsScreen(
    context: Context,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(context))
) {
    val totalDetections by viewModel.totalDetections.collectAsState(0)
    val avgConfidence by viewModel.avgConfidence.collectAsState(0f)
    val detectionsByLevel by viewModel.detectionsByConfidenceLevel.collectAsState(mapOf())
    val detectionsBySector by viewModel.detectionsBySector.collectAsState(mapOf())
    val exportStatus by viewModel.exportStatus.collectAsState(initial = null)
    val isExporting by viewModel.isExporting.collectAsState(initial = false)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeepNavy)
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "REPORTS & ANALYTICS",
                color = TextPrimaryWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Summary Statistics
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Session Summary",
                        color = TextPrimaryWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticBlock("Total Events", totalDetections.toString(), AccentCyan)
                        StatisticBlock("Avg Confidence", String.format("%.0f%%", avgConfidence), StatusWarningAmber)
                        StatisticBlock("Time Range", "24h", TextSecondaryGray)
                    }
                }
            }
        }

        // Confidence Distribution
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Confidence Distribution",
                        color = TextPrimaryWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DistributionBar(
                        label = "HIGH (75-100%)",
                        value = detectionsByLevel["HIGH"] ?: 0,
                        total = totalDetections,
                        color = StatusCriticalRed
                    )
                    
                    DistributionBar(
                        label = "MEDIUM (41-74%)",
                        value = detectionsByLevel["MEDIUM"] ?: 0,
                        total = totalDetections,
                        color = MotionIndicatorOrange
                    )
                    
                    DistributionBar(
                        label = "LOW (1-40%)",
                        value = detectionsByLevel["LOW"] ?: 0,
                        total = totalDetections,
                        color = StatusWarningAmber
                    )
                }
            }
        }

        // Sector Activity
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Activity by Sector",
                        color = TextPrimaryWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (detectionsBySector.isEmpty()) {
                        Text(
                            text = "No sector data available",
                            color = TextSecondaryGray,
                            fontSize = 12.sp
                        )
                    } else {
                        val sortedSectors = detectionsBySector.toList().sortedByDescending { it.second }
                        sortedSectors.forEach { (sector, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sector,
                                    color = TextPrimaryWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(40.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .fillMaxWidth(fraction = if (totalDetections > 0) (count.toFloat() / totalDetections) else 0f)
                                        .background(AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = count.toString(),
                                    color = TextSecondaryGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(30.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Options
        item {
            Text(
                text = "Export Report",
                color = TextPrimaryWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.exportReportAsCSV() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                    border = BorderStroke(1.dp, AccentCyan),
                    enabled = !isExporting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileOpen,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as CSV", color = TextPrimaryWhite, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { viewModel.exportReportAsPDF() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                    border = BorderStroke(1.dp, PrimaryDeepBlue),
                    enabled = !isExporting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = PrimaryDeepBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export as Report", color = TextPrimaryWhite, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { viewModel.shareReport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccessEmerald),
                    enabled = !isExporting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Report", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Export Status Message
        if (exportStatus != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = exportStatus ?: "",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Statistic block
 */
@Composable
fun StatisticBlock(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(CardSurface, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = value,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.LightGray,
            fontSize = 10.sp
        )
    }
}

/**
 * Distribution bar
 */
@Composable
fun DistributionBar(
    label: String,
    value: Int,
    total: Int,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp
            )
            Text(
                text = "$value (${if (total > 0) (value * 100 / total) else 0}%)",
                color = color,
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
            if (total > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = value.toFloat() / total)
                        .background(color, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
