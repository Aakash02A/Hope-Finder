package com.tryout.hopefinder.domain.repository

import com.tryout.hopefinder.domain.model.DashboardStats
import com.tryout.hopefinder.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for report operations (Admin only).
 */
interface ReportRepository {
    
    /**
     * Get dashboard statistics.
     * @return Flow of current dashboard stats.
     */
    fun getDashboardStats(): Flow<DashboardStats>
    
    /**
     * Get scan reports for a date range.
     * @param startDate Start of date range in milliseconds.
     * @param endDate End of date range in milliseconds.
     * @return Flow of scan reports.
     */
    fun getScanReports(startDate: Long, endDate: Long): Flow<List<ScanReport>>
    
    /**
     * Log a completed scan.
     * @param detectionsCount Number of detections in the scan.
     * @param alertsCreated Number of alerts created.
     * @return Result indicating success or failure.
     */
    suspend fun logScan(detectionsCount: Int, alertsCreated: Int): Result<Unit>
    
    /**
     * Get detection success rate breakdown.
     * @param startDate Start of date range.
     * @param endDate End of date range.
     * @return Map of detection type to count.
     */
    suspend fun getDetectionBreakdown(
        startDate: Long, 
        endDate: Long
    ): Map<String, Int>
}
