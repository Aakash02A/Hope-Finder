package com.tryout.hopefinder.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tryout.hopefinder.domain.repository.RadarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Radar Scanning Screen.
 * Handles radar scanning operations and state management.
 */
@HiltViewModel
class RadarScanViewModel @Inject constructor(
    private val radarRepository: RadarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarScanUiState())
    val uiState: StateFlow<RadarScanUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        checkSystemStatus()
    }

    /**
     * Handle UI events from the screen.
     */
    fun onEvent(event: RadarScanEvent) {
        when (event) {
            is RadarScanEvent.StartScan -> startScan()
            is RadarScanEvent.StopScan -> stopScan()
            is RadarScanEvent.NavigateBack -> stopScan()
            is RadarScanEvent.RescanSector -> rescanSector(event.sectorId)
        }
    }

    /**
     * Check the status of the radar system.
     */
    private fun checkSystemStatus() {
        viewModelScope.launch {
            try {
                val isOnline = radarRepository.isSystemOnline()
                val probeStatus = radarRepository.getProbeStatus()
                
                _uiState.update { state ->
                    state.copy(
                        isSystemOnline = isOnline,
                        probeStatus = probeStatus,
                        statusMessage = if (isOnline) "System online - Ready to scan" else "System offline"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSystemOnline = false,
                        statusMessage = "Error checking system status"
                    )
                }
            }
        }
    }

    /**
     * Start radar scanning.
     */
    private fun startScan() {
        scanJob?.cancel()
        
        _uiState.update { state ->
            state.copy(statusMessage = "Initializing scan...")
        }

        scanJob = viewModelScope.launch {
            radarRepository.startScan()
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            statusMessage = "Scan error: ${e.message}",
                            scanState = state.scanState.copy(isScanning = false)
                        )
                    }
                }
                .collect { scanState ->
                    val sectorNames = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    val sectorName = sectorNames.getOrElse(scanState.currentSector - 1) { "Unknown" }
                    
                    _uiState.update { state ->
                        state.copy(
                            scanState = scanState,
                            statusMessage = if (scanState.isScanning) {
                                "Scanning sector $sectorName..."
                            } else {
                                "Scan complete - ${scanState.detections.size} detections"
                            }
                        )
                    }
                }
        }
    }

    /**
     * Stop the current scan.
     */
    private fun stopScan() {
        viewModelScope.launch {
            scanJob?.cancel()
            radarRepository.stopScan()
            
            _uiState.update { state ->
                state.copy(
                    scanState = state.scanState.copy(isScanning = false),
                    statusMessage = "Scan stopped - ${state.detections.size} detections"
                )
            }
        }
    }

    /**
     * Rescan a specific sector.
     */
    private fun rescanSector(sectorId: Int) {
        viewModelScope.launch {
            val sectorNames = listOf("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
            val sectorName = sectorNames.getOrElse(sectorId - 1) { "Unknown" }
            
            _uiState.update { state ->
                state.copy(statusMessage = "Rescanning sector $sectorName...")
            }
            
            radarRepository.rescanSector(sectorId)
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(statusMessage = "Rescan error: ${e.message}")
                    }
                }
                .collect { detection ->
                    _uiState.update { state ->
                        val newDetections = state.scanState.detections + detection
                        state.copy(
                            scanState = state.scanState.copy(detections = newDetections),
                            statusMessage = "Found: ${detection.type.name} at ${detection.distance.format(1)}m"
                        )
                    }
                }
            
            _uiState.update { state ->
                state.copy(statusMessage = "Rescan complete")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}

/**
 * Format float to specified decimal places.
 */
private fun Float.format(decimals: Int): String = "%.${decimals}f".format(this)
