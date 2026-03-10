package com.tryout.hopefinder.presentation.scan

import com.tryout.hopefinder.domain.model.RadarDetection
import com.tryout.hopefinder.domain.model.RadarScanState

/**
 * UI State for the Radar Scanning Screen.
 */
data class RadarScanUiState(
    val scanState: RadarScanState = RadarScanState(),
    val statusMessage: String = "Ready to scan",
    val isSystemOnline: Boolean = true,
    val probeStatus: Map<Int, Boolean> = mapOf(
        1 to true, 2 to true, 3 to true, 4 to true, 5 to true
    )
) {
    val isScanning: Boolean get() = scanState.isScanning
    val currentSector: Int get() = scanState.currentSector
    val detections: List<RadarDetection> get() = scanState.detections
    val scanProgress: Float get() = scanState.scanProgress
}

/**
 * Events from the Radar Scanning Screen.
 */
sealed interface RadarScanEvent {
    data object StartScan : RadarScanEvent
    data object StopScan : RadarScanEvent
    data object NavigateBack : RadarScanEvent
    data class RescanSector(val sectorId: Int) : RadarScanEvent
}
