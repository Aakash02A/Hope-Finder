package com.hope_finder.data.model

data class RescueReport(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val timestamp: Long = 0,
    val probeIds: List<String> = emptyList(),
    val confidence: Int = 0, // 0-100
    val heartbeatBpm: Int = 0,
    val respirationRpm: Int = 0,
    val summary: String = "",
    val detections: List<DetectionEvent> = emptyList()
)

data class DetectionEvent(
    val time: Long = 0,
    val description: String = "",
    val signalStrength: Int = 0
)
