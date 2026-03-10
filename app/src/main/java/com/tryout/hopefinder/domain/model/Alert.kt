package com.tryout.hopefinder.domain.model

/**
 * Represents an alert generated from a radar detection.
 */
data class Alert(
    val id: String = "",
    val detection: RadarDetection,
    val priority: AlertPriority,
    val status: AlertStatus = AlertStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val acknowledgedBy: String? = null
)

enum class AlertPriority {
    CRITICAL,   // Heartbeat detected - immediate rescue needed
    HIGH,       // Movement detected - possible survivor
    MEDIUM      // Breathing pattern - needs verification
}

enum class AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    FALSE_POSITIVE
}
