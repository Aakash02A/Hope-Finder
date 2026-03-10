package com.tryout.hopefinder.domain.model

/**
 * Represents a scan report for analytics (Admin only).
 */
data class ScanReport(
    val id: String = "",
    val date: Long,
    val totalScans: Int,
    val alertsGenerated: Int,
    val heartbeatDetections: Int,
    val breathingDetections: Int,
    val movementDetections: Int,
    val successRate: Float
)

/**
 * Daily statistics for dashboard display.
 */
data class DashboardStats(
    val activeUsers: Int = 0,
    val scansToday: Int = 0,
    val alertsGenerated: Int = 0,
    val systemOnline: Boolean = true
)
