package com.hope_finder.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Alert(
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var priority: String = "LOW",
    var severity: String = "Low",
    var timestamp: Long = 0,
    @get:PropertyName("probeId") @set:PropertyName("probeId") var probeId: String = "",
    var type: String = "GENERAL",
    @get:PropertyName("isResolved") @set:PropertyName("isResolved") var isResolved: Boolean = false
)

enum class AlertPriority {
    HIGH, MEDIUM, LOW
}

enum class AlertType {
    HEARTBEAT_DETECTED,
    RESPIRATION_DETECTED,
    PROBE_FAILURE,
    SIGNAL_ANOMALY,
    LIFE_DETECTION,
    BATTERY_LOW,
    SYSTEM_ERROR
}
