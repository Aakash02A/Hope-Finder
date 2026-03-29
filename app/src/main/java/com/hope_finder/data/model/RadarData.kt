package com.hope_finder.data.model

data class RadarProbeData(
    val probeId: String = "",
    val heartbeatSignal: List<Float> = emptyList(),
    val respirationRate: Int = 0,
    val signalStrength: Int = 0,
    val scanDepth: Float = 0f,
    val timestamp: Long = 0,
    val detections: List<RadarDetection> = emptyList()
)

data class RadarDetection(
    val id: String = "",
    val angle: Float = 0f,
    val distance: Float = 0f,
    val type: String = "OBJECT", // LIFE_SIGNATURE, MOVEMENT, OBJECT
    val confidence: Int = 0
)
