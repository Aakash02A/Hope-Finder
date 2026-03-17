package com.hope_finder.data.model

data class RadarCell(
    val id: String = "",
    val name: String = "Cell A",
    val masterNodeId: String = "",
    val probes: List<String> = emptyList(), // List of probe IDs
    val status: String = "Inactive" // Active, Inactive, Alert
)
