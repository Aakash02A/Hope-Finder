package com.hope_finder.data.model

data class Probe(
    val id: String = "",
    val name: String = "",
    val status: String = "Offline", // Online, Offline
    val batteryLevel: Int = 0,
    val signalStrength: Int = 0,
    val temperature: Float = 0f,
    val lastActive: Long = 0,
    val firmwareVersion: String = "1.0.0"
)
