package com.hope_finder.data.model

data class DashboardStats(
    val probesOnline: Int = 0,
    val activeScanZones: Int = 0,
    val lifeSignalsDetected: Int = 0,
    val rescueAlerts: Int = 0
)

data class RadarCell(
    val id: String = "",
    val name: String = "",
    val status: String = "Idle", // Scanning, Active, Idle
    val masterNodeId: String = "",
    val probeIds: List<String> = emptyList(),
    val signalStrength: Int = 0
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
    val batteryLevel: Int = 0,
    val status: String = "Offline", // Online, Offline, Busy
    val temperature: Float = 0f,
    val signalStrength: Int = 0,
    val isMasterNode: Boolean = false
)

data class SystemAlert(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val severity: String = "Low", // Low, Medium, High
    val timestamp: Long = 0,
    val isResolved: Boolean = false,
    val type: String = "GENERAL",
    val probeId: String = ""
)
