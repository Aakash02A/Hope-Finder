package com.tryout.hopefinder.domain.repository

import com.tryout.hopefinder.domain.model.Alert
import com.tryout.hopefinder.domain.model.AlertStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for alert operations.
 */
interface AlertRepository {
    
    /**
     * Get all active alerts.
     * @return Flow of active alerts.
     */
    fun getActiveAlerts(): Flow<List<Alert>>
    
    /**
     * Get alerts by status.
     * @param status The alert status to filter by.
     * @return Flow of alerts matching the status.
     */
    fun getAlertsByStatus(status: AlertStatus): Flow<List<Alert>>
    
    /**
     * Create a new alert from a detection.
     * @param alert The alert to create.
     * @return Result containing the created alert ID.
     */
    suspend fun createAlert(alert: Alert): Result<String>
    
    /**
     * Update alert status.
     * @param alertId The alert ID to update.
     * @param status The new status.
     * @param userId The user making the update.
     * @return Result indicating success or failure.
     */
    suspend fun updateAlertStatus(
        alertId: String, 
        status: AlertStatus, 
        userId: String
    ): Result<Unit>
    
    /**
     * Get alert history (for Admin reports).
     * @param startDate Start of date range in milliseconds.
     * @param endDate End of date range in milliseconds.
     * @return Flow of alerts within the date range.
     */
    fun getAlertHistory(startDate: Long, endDate: Long): Flow<List<Alert>>
}
