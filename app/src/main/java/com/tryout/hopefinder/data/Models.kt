package com.tryout.hopefinder.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Room Database Entities for Hope-Finder System
 */

/**
 * Represents a detection event from the radar system
 */
@Entity(tableName = "detections")
data class DetectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val timestamp: Long,  // Unix timestamp in milliseconds
    val sector: Int,      // 0-11
    val sectorLabel: String,
    val angle: Int,       // 0-359 degrees
    val rawSignal: Float,
    val filteredSignal: Float,
    val baseline: Float,
    val signalStrength: Float,  // 0-1
    val motionLevel: Int,   // 0-3
    val motionLabel: String,
    val confidence: Int,    // 0-100%
    val confidenceLevel: String,  // "NONE", "LOW", "MEDIUM", "HIGH"
    val humanPresencePossible: Boolean,
    val wifiRssi: Int,
    val scanDurationMs: Long,
    val deviceId: String = "master_probe_01",
    val acknowledged: Boolean = false
)

/**
 * Represents an alert generated from a detection
 */
@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val detectionId: Long,  // FK to DetectionEntity
    val timestamp: Long,
    val confidence: Int,
    val sector: Int,
    val sectorLabel: String,
    val angle: Int,
    val severity: String,  // "LOW", "MEDIUM", "HIGH"
    val message: String,
    val acknowledged: Boolean = false,
    val acknowledgedAt: Long? = null,
    val notes: String = ""
)

/**
 * Represents a calibration profile snapshot
 */
@Entity(tableName = "calibration_profiles")
data class CalibrationProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val baseline: Float,
    val motionThreshold: Float,
    val sensitivityMultiplier: Float,
    val confidenceFloor: Int,
    val timestamp: Long,
    val deviceId: String = "master_probe_01",
    val isActive: Boolean = false
)

/**
 * Represents a scanning session
 */
@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val totalEvents: Int = 0,
    val highConfidenceEvents: Int = 0,
    val scanCoveragePercent: Int = 0,
    val deviceId: String = "master_probe_01"
)

/**
 * Device status snapshot
 */
@Entity(tableName = "device_status")
data class DeviceStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val deviceId: String,
    val wifiConnected: Boolean,
    val rssi: Int,
    val radarHealthy: Boolean,
    val calibrated: Boolean,
    val scanning: Boolean,
    val currentSector: Int,
    val currentAngle: Int,
    val uptime: Long,
    val cpuUsagePercent: Int = 0,
    val memoryFreeBytes: Long = 0
)

/**
 * Report summary for export
 */
@Entity(
    tableName = "report_summaries",
    indices = [androidx.room.Index("sessionId", unique = true)]
)
data class ReportSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val totalEvents: Int,
    val lowConfidenceCount: Int,
    val mediumConfidenceCount: Int,
    val highConfidenceCount: Int,
    val averageConfidence: Float,
    val peakActivityHour: Int,
    val coveragePercent: Int,
    val deviceId: String = "master_probe_01",
    val exportedAt: Long? = null,
    val exportFilePath: String = ""
)

/**
 * Raw API response model
 */
data class DetectionResponse(
    val device_id: String,
    val event_id: String,
    val timestamp: String,
    val sector: Int,
    val sector_label: String,
    val angle: Int,
    val raw_signal: Float,
    val filtered_signal: Float,
    val baseline: Float,
    val signal_delta: Float,
    val signal_strength: Float,
    val motion_level: Int,
    val motion_label: String,
    val confidence: Int,
    val confidence_level: String,
    val human_presence_possible: Boolean,
    val wifi_rssi: Int,
    val scan_duration_ms: Long
)

/**
 * Device status API response
 */
data class DeviceStatusResponse(
    val status: String,
    val data: DeviceStatusData
)

data class DeviceStatusData(
    val device_id: String,
    val device_name: String,
    val firmware_version: String,
    val uptime_seconds: Long,
    val timestamp: String,
    val wifi: WifiStatus,
    val radar: RadarStatus,
    val system: SystemStatus,
    val last_detection: LastDetection?,
    val errors: List<String>
)

data class WifiStatus(
    val connected: Boolean,
    val ssid: String,
    val rssi: Int,
    val ip: String
)

data class RadarStatus(
    val healthy: Boolean,
    val signal_strength: Float,
    val baseline: Float,
    val noise_level: Float
)

data class SystemStatus(
    val calibrated: Boolean,
    val scanning: Boolean,
    val current_sector: Int,
    val current_angle: Int,
    val memory_free_bytes: Long,
    val cpu_usage_percent: Int
)

data class LastDetection(
    val timestamp: String,
    val confidence: Int,
    val sector: Int
)

/**
 * Event history response
 */
data class EventHistoryResponse(
    val status: String,
    val data: EventHistoryData
)

data class EventHistoryData(
    val total_events: Int,
    val events: List<DetectionResponse>,
    val earliest: String,
    val latest: String
)

/**
 * Scan command response
 */
data class ScanCommandResponse(
    val status: String,
    val data: ScanData
)

data class ScanData(
    val scan_id: String,
    val started_at: String,
    val estimated_duration_seconds: Int? = null,
    val stopped_at: String? = null,
    val events_during_scan: Int? = null
)

/**
 * Health check response
 */
data class HealthCheckResponse(
    val status: String,
    val data: HealthData
)

data class HealthData(
    val device_healthy: Boolean,
    val checks: Map<String, Boolean>,
    val errors: List<String>,
    val warnings: List<String>,
    val last_check: String
)

/**
 * Session summary response
 */
data class SessionSummaryResponse(
    val status: String,
    val data: SessionSummaryData
)

data class SessionSummaryData(
    val session_id: String,
    val start_time: String,
    val end_time: String,
    val total_events: Int,
    val events_by_confidence: Map<String, Int>,
    val events_by_sector: Map<String, Int>,
    val peak_activity_hour: Int,
    val average_confidence: Float,
    val scan_coverage_percent: Int
)
