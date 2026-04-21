package com.tryout.hopefinder.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

/**
 * Repository Pattern for Data Access
 * Abstracts database and API calls
 */

class DetectionRepository(
    private val dao: DetectionDao,
    private val alertRepository: AlertRepository? = null
) {

    companion object {
        private val insertMutex = Mutex()
    }
    
    fun getRecentDetections(limit: Int = 100): Flow<List<DetectionEntity>> {
        return dao.getRecentDetections(limit)
    }

    fun getDetectionsSince(startTime: Long): Flow<List<DetectionEntity>> {
        return dao.getDetectionsSince(startTime)
    }

    fun getDetectionsBySector(sector: Int, limit: Int = 50): Flow<List<DetectionEntity>> {
        return dao.getDetectionsBySector(sector, limit)
    }

    fun getDetectionsByConfidence(minConfidence: Int): Flow<List<DetectionEntity>> {
        return dao.getDetectionsByConfidence(minConfidence)
    }

    suspend fun insertDetection(detection: DetectionEntity): Boolean {
        return insertMutex.withLock {
            if (dao.getDetectionCountByEventId(detection.eventId) > 0) {
                return@withLock false
            }

            val rowId = dao.insertDetection(detection)

            // Auto-generate alert for high confidence detections
            if (detection.confidence >= 50 && alertRepository != null) {
                val alert = AlertEntity(
                    detectionId = rowId, // Note: dao.insertDetection returns Long ID
                    timestamp = detection.timestamp,
                    confidence = detection.confidence,
                    sector = detection.sector,
                    sectorLabel = detection.sectorLabel,
                    angle = detection.angle,
                    severity = if (detection.confidence >= 75) "HIGH" else "MEDIUM",
                    message = "Possible life detected in sector ${detection.sectorLabel} (${detection.confidence}%)"
                )
                alertRepository.insertAlert(alert)
            }

            true
        }
    }

    suspend fun cleanupOldDetections(ageHours: Int = 24) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(ageHours.toLong())
        dao.deleteOldDetections(cutoffTime)
    }
}

class AlertRepository(private val dao: AlertDao) {
    
    fun getUnacknowledgedAlerts(): Flow<List<AlertEntity>> {
        return dao.getUnacknowledgedAlerts()
    }

    fun getRecentAlerts(limit: Int = 100): Flow<List<AlertEntity>> {
        return dao.getRecentAlerts(limit)
    }

    suspend fun insertAlert(alert: AlertEntity) {
        dao.insertAlert(alert)
    }

    suspend fun acknowledgeAlert(alertId: Long) {
        dao.acknowledgeAlert(alertId, System.currentTimeMillis())
    }

    suspend fun updateAlert(alert: AlertEntity) {
        dao.updateAlert(alert)
    }

    suspend fun cleanupOldAlerts(ageHours: Int = 72) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(ageHours.toLong())
        dao.deleteOldAlerts(cutoffTime)
    }
}

class CalibrationRepository(private val dao: CalibrationDao) {
    
    fun getActiveProfile(): Flow<CalibrationProfileEntity?> {
        return dao.getActiveProfile()
    }

    fun getProfiles(limit: Int = 10): Flow<List<CalibrationProfileEntity>> {
        return dao.getProfiles(limit)
    }

    suspend fun insertAndActivateProfile(profile: CalibrationProfileEntity) {
        dao.deactivateAll()
        val newProfile = profile.copy(isActive = true)
        dao.insertProfile(newProfile)
    }
}

class ScanSessionRepository(private val dao: ScanSessionDao) {
    
    fun getCurrentSession(): Flow<ScanSessionEntity?> {
        return dao.getCurrentSession()
    }

    fun getRecentSessions(limit: Int = 20): Flow<List<ScanSessionEntity>> {
        return dao.getRecentSessions(limit)
    }

    suspend fun insertSession(session: ScanSessionEntity) {
        dao.insertSession(session)
    }

    suspend fun updateSession(session: ScanSessionEntity) {
        dao.updateSession(session)
    }

    fun getTotalEventsAfter(startTime: Long): Flow<Int> {
        return dao.getTotalEventsAfter(startTime)
    }
}

class DeviceStatusRepository(private val dao: DeviceStatusDao) {
    
    fun getLatestStatus(): Flow<DeviceStatusEntity?> {
        return dao.getLatestStatus()
    }

    fun getStatusHistory(limit: Int = 100): Flow<List<DeviceStatusEntity>> {
        return dao.getStatusHistory(limit)
    }

    suspend fun insertStatus(status: DeviceStatusEntity) {
        dao.insertStatus(status)
    }

    suspend fun cleanupOldStatus(ageHours: Int = 24) {
        val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(ageHours.toLong())
        dao.deleteOldStatus(cutoffTime)
    }
}

class ReportRepository(private val dao: ReportSummaryDao) {
    
    fun getSummaryBySession(sessionId: String): Flow<ReportSummaryEntity?> {
        return dao.getSummaryBySession(sessionId)
    }

    fun getRecentSummaries(limit: Int = 20): Flow<List<ReportSummaryEntity>> {
        return dao.getRecentSummaries(limit)
    }

    suspend fun insertSummary(summary: ReportSummaryEntity) {
        dao.insertSummary(summary)
    }

    suspend fun updateSummary(summary: ReportSummaryEntity) {
        dao.updateSummary(summary)
    }
}

/**
 * Unified repository factory
 */
class DataRepositoryFactory(private val context: Context) {
    private val database = HopeFinderDatabase.getInstance(context)
    
    val alertRepository by lazy { AlertRepository(database.alertDao()) }
    val detectionRepository by lazy { DetectionRepository(database.detectionDao(), alertRepository) }
    val calibrationRepository by lazy { CalibrationRepository(database.calibrationDao()) }
    val scanSessionRepository by lazy { ScanSessionRepository(database.scanSessionDao()) }
    val deviceStatusRepository by lazy { DeviceStatusRepository(database.deviceStatusDao()) }
    val reportRepository by lazy { ReportRepository(database.reportSummaryDao()) }
}
