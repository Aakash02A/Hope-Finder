package com.hope_finder.data.model

import com.google.firebase.firestore.PropertyName

data class DashboardStats(
    @get:PropertyName("probes_online") @set:PropertyName("probes_online") var probesOnline: Int = 0,
    @get:PropertyName("active_scan_zones") @set:PropertyName("active_scan_zones") var activeScanZones: Int = 0,
    @get:PropertyName("life_signals_detected") @set:PropertyName("life_signals_detected") var lifeSignalsDetected: Int = 0,
    @get:PropertyName("rescue_alerts") @set:PropertyName("rescue_alerts") var rescueAlerts: Int = 0
)

data class LifeSignal(
    val id: String = "",
    val location: String = "",
    val confidence: Int = 0, // 0-100
    val timestamp: Long = 0
)

data class ConnectedProbe(
    val id: String = "",
    val name: String = "",
    @get:PropertyName("battery_level") @set:PropertyName("battery_level") var batteryLevel: Int = 0,
    val status: String = "Offline", // Online, Offline, Busy
    val temperature: Float = 0f,
    @get:PropertyName("signal_strength") @set:PropertyName("signal_strength") var signalStrength: Int = 0,
    @get:PropertyName("is_master_node") @set:PropertyName("is_master_node") var isMasterNode: Boolean = false,
    @get:PropertyName("last_active") @set:PropertyName("last_active") var lastActive: Long = 0
)

data class SystemAlert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val severity: String = "Low", // Low, Medium, High
    val timestamp: Long = 0,
    @get:PropertyName("resolved") @set:PropertyName("resolved") var isResolved: Boolean = false,
    val type: String = "GENERAL",
    @get:PropertyName("probe_id") @set:PropertyName("probe_id") var probeId: String = ""
)
