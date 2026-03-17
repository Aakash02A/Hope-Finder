package com.hope_finder.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.model.RadarProbeData
import com.hope_finder.data.model.SystemAlert
import com.hope_finder.data.model.RescueReport
import com.hope_finder.data.repository.RadarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val repository: RadarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun toggleScanning(probeId: String) {
        if (_uiState.value.isScanning) {
            stopScanning()
        } else {
            startScanning(probeId)
        }
    }

    private fun startScanning(probeId: String) {
        scanJob?.cancel()
        _uiState.value = _uiState.value.copy(isScanning = true, isLoading = true)
        
        scanJob = viewModelScope.launch {
            // In a real app, we'd listen to Firebase. For simulation, we'll generate data.
            // But first, try to get existing data
            repository.getRadarData(probeId).collectLatest { data ->
                if (data != null) {
                    _uiState.value = _uiState.value.copy(radarData = data, isLoading = false)
                }
            }
        }

        // Simulating real-time data generation and pushing to Firebase
        viewModelScope.launch {
            while (_uiState.value.isScanning) {
                val mockData = generateMockRadarData(probeId)
                repository.updateRadarData(probeId, mockData)
                
                // Check for "life detection" logic (simulation)
                if (mockData.respirationRate > 12 && mockData.heartbeatSignal.last() > 60) {
                    triggerLifeDetectedAlert(probeId, mockData)
                }
                
                delay(2000) // Update every 2 seconds
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    private fun generateMockRadarData(probeId: String): RadarProbeData {
        return RadarProbeData(
            probeId = probeId,
            heartbeatSignal = listOf(Random.nextInt(60, 100).toFloat()),
            respirationRate = Random.nextInt(12, 20),
            signalStrength = Random.nextInt(70, 99),
            scanDepth = Random.nextDouble(1.0, 10.0).toFloat(),
            timestamp = System.currentTimeMillis()
        )
    }

    private fun triggerLifeDetectedAlert(probeId: String, data: RadarProbeData) {
        viewModelScope.launch {
            val alert = SystemAlert(
                id = UUID.randomUUID().toString(),
                title = "Life Signal Detected!",
                message = "Probe $probeId detected vital signs at ${data.scanDepth}m depth.",
                severity = "High",
                timestamp = System.currentTimeMillis(),
                type = "LIFE_DETECTION"
            )
            repository.triggerAlert(alert)
        }
    }

    fun saveRescueReport(probeId: String) {
        viewModelScope.launch {
            val data = _uiState.value.radarData
            val report = RescueReport(
                id = UUID.randomUUID().toString(),
                title = "Rescue Mission - $probeId",
                location = "Sector A-${Random.nextInt(1, 10)}",
                timestamp = System.currentTimeMillis(),
                heartbeatBpm = data.heartbeatSignal.lastOrNull()?.toInt() ?: 0,
                respirationRpm = data.respirationRate,
                confidence = data.signalStrength,
                probeIds = listOf(probeId)
            )
            repository.saveReport(report)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }
}

data class RadarUiState(
    val radarData: RadarProbeData = RadarProbeData(),
    val isScanning: Boolean = false,
    val isLoading: Boolean = false
)
