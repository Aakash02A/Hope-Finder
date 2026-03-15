package com.hope_finder.data.model

data class RadarProbeData(
    val probeId: String = "",
    val heartbeatSignal: List<Float> = emptyList(),
    val respirationRate: Int = 0,
    val signalStrength: Int = 0,
    val scanDepth: Float = 0f,
    val timestamp: Long = 0
)
