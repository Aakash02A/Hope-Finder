package com.hope_finder.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.model.RadarDetection
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
    private var dataGenJob: Job? = null

    fun toggleScanning(probeId: String) {
        if (_uiState.value.isScanning) {
            stopScanning()
        } else {
            startScanning(probeId)
        }
    }

    private fun startScanning(probeId: String) {
        _uiState.value = _uiState.value.copy(isScanning = true, isLoading = true)
        
        scanJob = viewModelScope.launch {
            repository.getRadarData(probeId).collectLatest { data ->
                if (data != null) {
                    _uiState.value = _uiState.value.copy(radarData = data, isLoading = false)
                }
            }
        }

        // Simulating real-time data generation and pushing to Firebase
        dataGenJob = viewModelScope.launch {
            var currentDetections = mutableListOf<RadarDetection>()
            
            while (_uiState.value.isScanning) {
                // Randomly add a new detection or update existing ones
                if (Random.nextFloat() > 0.7f && currentDetections.size < 5) {
                    currentDetections.add(
                        RadarDetection(
                            id = UUID.randomUUID().toString().take(8),
                            angle = Random.nextFloat() * 360f,
                            distance = 0.2f + Random.nextFloat() * 0.7f,
                            type = if (Random.nextFloat() > 0.6f) "LIFE_SIGNATURE" else "MOVEMENT",
                            confidence = Random.nextInt(50, 99)
                        )
                    )
                }
                
                // Slowly age out or move detections
                currentDetections = currentDetections.map { 
                    it.copy(distance = (it.distance + (Random.nextFloat() - 0.5f) * 0.05f).coerceIn(0.1f, 0.9f))
                }.toMutableList()

                val mockData = generateMockRadarData(probeId, currentDetections)
                repository.updateRadarData(probeId, mockData)
                
                // Check for "life detection" logic (simulation)
                val lifeSign = currentDetections.firstOrNull { it.type == "LIFE_SIGNATURE" && it.confidence > 80 }
                if (lifeSign != null && Random.nextFloat() > 0.8f) {
                    triggerLifeDetectedAlert(probeId, mockData, lifeSign)
                }
                
                delay(3000) // Update every 3 seconds
            }
        }
    }

    private fun stopScanning() {
        scanJob?.cancel()
        dataGenJob?.cancel()
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    private fun generateMockRadarData(probeId: String, detections: List<RadarDetection>): RadarProbeData {
        return RadarProbeData(
            probeId = probeId,
            heartbeatSignal = listOf(Random.nextInt(60, 100).toFloat()),
            respirationRate = Random.nextInt(12, 20),
            signalStrength = Random.nextInt(70, 99),
            scanDepth = Random.nextDouble(1.0, 10.0).toFloat(),
            timestamp = System.currentTimeMillis(),
            detections = detections
        )
    }

    private fun triggerLifeDetectedAlert(probeId: String, data: RadarProbeData, detection: RadarDetection) {
        viewModelScope.launch {
            val alert = SystemAlert(
                id = UUID.randomUUID().toString(),
                title = "LIFE SIGNATURE DETECTED",
                message = "Critical: Life signature identified at ${String.format("%.1f", data.scanDepth)}m depth in sector ${detection.angle.toInt()}°.",
                severity = "High",
                timestamp = System.currentTimeMillis(),
                type = "LIFE_DETECTION",
                probeId = probeId
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
                location = "Sector Alpha",
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
