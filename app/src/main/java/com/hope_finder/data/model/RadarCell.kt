package com.hope_finder.data.model

data class RadarCell(
    val id: String = "",
    val name: String = "",
    val status: String = "Idle", // Scanning, Active, Idle
    val masterNodeId: String = "",
    val probeIds: List<String> = emptyList(),
    val signalStrength: Int = 0
)
