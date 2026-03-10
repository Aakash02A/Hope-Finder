package com.tryout.hopefinder.domain.model

/**
 * Represents a detection from the radar probes.
 */
data class RadarDetection(
    val id: String = "",
    val type: DetectionType,
    val zoneLocation: String,       // e.g., "Sector A-3"
    val distance: Float,            // Distance in meters (0-10m)
    val signalStrength: Int,        // Signal strength (0-100)
    val confidenceScore: Float,     // Confidence percentage (0-100)
    val probeId: Int,               // Which probe detected (1-5)
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Types of detections the radar can identify.
 */
enum class DetectionType {
    HEARTBEAT,  // Green/Red accents
    BREATHING,  // Yellow accents
    MOVEMENT    // Blue accents
}

/**
 * Represents the current state of a radar scan.
 */
data class RadarScanState(
    val isScanning: Boolean = false,
    val currentSector: Int = 0,
    val totalSectors: Int = 5,
    val scanProgress: Float = 0f,
    val detections: List<RadarDetection> = emptyList()
)
