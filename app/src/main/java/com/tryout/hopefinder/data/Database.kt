package com.tryout.hopefinder.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects for Room Database
 */

@Dao
interface DetectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectionEntity): Long

    @Query("SELECT * FROM detections ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDetections(limit: Int = 100): Flow<List<DetectionEntity>>

    @Query("SELECT * FROM detections WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getDetectionsSince(startTime: Long): Flow<List<DetectionEntity>>

    @Query("SELECT * FROM detections WHERE sector = :sector ORDER BY timestamp DESC LIMIT :limit")
    fun getDetectionsBySector(sector: Int, limit: Int = 50): Flow<List<DetectionEntity>>

    @Query("SELECT * FROM detections WHERE confidence >= :minConfidence ORDER BY timestamp DESC")
    fun getDetectionsByConfidence(minConfidence: Int): Flow<List<DetectionEntity>>

    @Query("DELETE FROM detections WHERE timestamp < :cutoffTime")
    suspend fun deleteOldDetections(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM detections")
    suspend fun getDetectionCount(): Int
}

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("SELECT * FROM alerts WHERE acknowledged = 0 ORDER BY timestamp DESC")
    fun getUnacknowledgedAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAlerts(limit: Int = 100): Flow<List<AlertEntity>>

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET acknowledged = 1, acknowledgedAt = :timestamp WHERE id = :alertId")
    suspend fun acknowledgeAlert(alertId: Long, timestamp: Long)

    @Query("DELETE FROM alerts WHERE timestamp < :cutoffTime")
    suspend fun deleteOldAlerts(cutoffTime: Long)
}

@Dao
interface CalibrationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CalibrationProfileEntity)

    @Query("SELECT * FROM calibration_profiles WHERE isActive = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getActiveProfile(): Flow<CalibrationProfileEntity?>

    @Query("SELECT * FROM calibration_profiles ORDER BY timestamp DESC LIMIT :limit")
    fun getProfiles(limit: Int = 10): Flow<List<CalibrationProfileEntity>>

    @Query("UPDATE calibration_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE calibration_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun setActive(profileId: Long)
}

@Dao
interface ScanSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity)

    @Update
    suspend fun updateSession(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC LIMIT 1")
    fun getCurrentSession(): Flow<ScanSessionEntity?>

    @Query("SELECT * FROM scan_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<ScanSessionEntity>>

    @Query("SELECT SUM(totalEvents) FROM scan_sessions WHERE startTime >= :startTime")
    fun getTotalEventsAfter(startTime: Long): Flow<Int>
}

@Dao
interface DeviceStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: DeviceStatusEntity)

    @Query("SELECT * FROM device_status ORDER BY timestamp DESC LIMIT 1")
    fun getLatestStatus(): Flow<DeviceStatusEntity?>

    @Query("SELECT * FROM device_status ORDER BY timestamp DESC LIMIT :limit")
    fun getStatusHistory(limit: Int = 100): Flow<List<DeviceStatusEntity>>

    @Query("DELETE FROM device_status WHERE timestamp < :cutoffTime")
    suspend fun deleteOldStatus(cutoffTime: Long)
}

@Dao
interface ReportSummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: ReportSummaryEntity)

    @Query("SELECT * FROM report_summaries WHERE sessionId = :sessionId")
    fun getSummaryBySession(sessionId: String): Flow<ReportSummaryEntity?>

    @Query("SELECT * FROM report_summaries ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int = 20): Flow<List<ReportSummaryEntity>>

    @Update
    suspend fun updateSummary(summary: ReportSummaryEntity)
}

/**
 * Room Database instance
 */
@Database(
    entities = [
        DetectionEntity::class,
        AlertEntity::class,
        CalibrationProfileEntity::class,
        ScanSessionEntity::class,
        DeviceStatusEntity::class,
        ReportSummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HopeFinderDatabase : RoomDatabase() {
    abstract fun detectionDao(): DetectionDao
    abstract fun alertDao(): AlertDao
    abstract fun calibrationDao(): CalibrationDao
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun deviceStatusDao(): DeviceStatusDao
    abstract fun reportSummaryDao(): ReportSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: HopeFinderDatabase? = null

        fun getInstance(context: Context): HopeFinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HopeFinderDatabase::class.java,
                    "hopefinder_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
