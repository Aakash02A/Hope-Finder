package com.hope_finder.data.model

data class Alert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val priority: AlertPriority = AlertPriority.LOW,
    val timestamp: Long = 0,
    val probeId: String = "",
    val type: AlertType = AlertType.SIGNAL_ANOMALY,
    val isResolved: Boolean = false
)

enum class AlertPriority {
    HIGH, MEDIUM, LOW
}

enum class AlertType {
    HEARTBEAT_DETECTED,
    RESPIRATION_DETECTED,
    PROBE_FAILURE,
    SIGNAL_ANOMALY
}
