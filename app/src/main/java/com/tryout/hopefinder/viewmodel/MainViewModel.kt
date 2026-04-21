package com.tryout.hopefinder.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tryout.hopefinder.data.*
import com.tryout.hopefinder.network.DeviceApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Shared device connection manager
 */
object DeviceConnectionManager {
    var apiClient: DeviceApiClient? = null
    var deviceIp: String = "192.168.1.100"
    
    fun initialize(ip: String) {
        deviceIp = ip
        apiClient = DeviceApiClient("http://$ip:80/api/v1")
    }
}

/**
 * UiState for common patterns
 */
data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

/**
 * Main ViewModel - Coordinates global app state
 */
class MainViewModel(
    private val detectionRepository: DetectionRepository,
    private val alertRepository: AlertRepository,
    private val deviceStatusRepository: DeviceStatusRepository
) : ViewModel() {

    private val _appState = MutableStateFlow<UiState<Unit>>(UiState())
    val appState: StateFlow<UiState<Unit>> = _appState.asStateFlow()

    private val _deviceConnected = MutableStateFlow(false)
    val deviceConnected: StateFlow<Boolean> = _deviceConnected.asStateFlow()

    private val _pollingActive = MutableStateFlow(false)
    val pollingActive: StateFlow<Boolean> = _pollingActive.asStateFlow()

    private var pollingJob: Job? = null

    fun initializeDevice(deviceIp: String) {
        DeviceConnectionManager.initialize(deviceIp)
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            _pollingActive.value = true
            
            // Wait before first poll to allow UI to render
            delay(1000)
            
            Log.d("MainViewModel", "Starting polling at ${DeviceConnectionManager.deviceIp}")
            
            var consecutiveFailures = 0
            val maxConsecutiveFailures = 5  // Stop trying after 5 failures
            
            while (isActive && consecutiveFailures < maxConsecutiveFailures) {
                try {
                    val apiClient = DeviceConnectionManager.apiClient ?: return@launch
                    
                    // Fetch device status
                    val statusResult = apiClient.getDeviceStatus()
                    statusResult.onSuccess { response ->
                        _deviceConnected.value = true
                        consecutiveFailures = 0  // Reset on success
                        
                        // Store in database
                        val status = DeviceStatusEntity(
                            timestamp = System.currentTimeMillis(),
                            deviceId = response.data.device_id,
                            wifiConnected = response.data.wifi.connected,
                            rssi = response.data.wifi.rssi,
                            radarHealthy = response.data.radar.healthy,
                            calibrated = response.data.system.calibrated,
                            scanning = response.data.system.scanning,
                            currentSector = response.data.system.current_sector,
                            currentAngle = response.data.system.current_angle,
                            uptime = response.data.uptime_seconds,
                            cpuUsagePercent = response.data.system.cpu_usage_percent,
                            memoryFreeBytes = response.data.system.memory_free_bytes
                        )
                        deviceStatusRepository.insertStatus(status)
                    }
                    
                    // Fetch recent events
                    val eventsResult = apiClient.getEventHistory(limit = 100)
                    eventsResult.onSuccess { historyResponse ->
                        historyResponse.data.events.forEach { event ->
                            val detection = DetectionEntity(
                                eventId = event.event_id,
                                timestamp = parseDeviceTimestamp(event.timestamp),
                                sector = event.sector,
                                sectorLabel = event.sector_label,
                                angle = event.angle,
                                rawSignal = event.raw_signal,
                                filteredSignal = event.filtered_signal,
                                baseline = event.baseline,
                                signalStrength = event.signal_strength,
                                motionLevel = event.motion_level,
                                motionLabel = event.motion_label,
                                confidence = event.confidence,
                                confidenceLevel = event.confidence_level,
                                humanPresencePossible = event.human_presence_possible,
                                wifiRssi = event.wifi_rssi,
                                scanDurationMs = event.scan_duration_ms,
                                deviceId = event.device_id
                            )
                            val inserted = detectionRepository.insertDetection(detection)

                            // Create alert if high confidence
                            if (inserted && event.confidence >= 60) {
                                val alert = AlertEntity(
                                    detectionId = 0, // Set after insert
                                    timestamp = detection.timestamp,
                                    confidence = event.confidence,
                                    sector = event.sector,
                                    sectorLabel = event.sector_label,
                                    angle = event.angle,
                                    severity = when {
                                        event.confidence >= 80 -> "HIGH"
                                        event.confidence >= 60 -> "MEDIUM"
                                        else -> "LOW"
                                    },
                                    message = "Possible human presence detected at ${event.sector_label} (${event.confidence}% confidence)"
                                )
                                alertRepository.insertAlert(alert)
                            }
                        }
                    }

                    eventsResult.onFailure {
                        _deviceConnected.value = false
                        consecutiveFailures++
                        Log.w("MainViewModel", "Event poll attempt $consecutiveFailures failed: ${it.message}")
                    }
                    
                    statusResult.onFailure {
                        _deviceConnected.value = false
                        consecutiveFailures++
                        Log.w("MainViewModel", "Poll attempt $consecutiveFailures failed: ${it.message}")
                    }
                    
                } catch (e: Exception) {
                    _deviceConnected.value = false
                    consecutiveFailures++
                    Log.e("MainViewModel", "Polling exception (attempt $consecutiveFailures)", e)
                }
                
                if (consecutiveFailures < maxConsecutiveFailures) {
                    delay(3000)  // Poll every 3 seconds
                }
            }
            
            if (consecutiveFailures >= maxConsecutiveFailures) {
                Log.e("MainViewModel", "Polling stopped after $maxConsecutiveFailures consecutive failures")
                _pollingActive.value = false
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        _pollingActive.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    private val factory = DataRepositoryFactory(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            factory.detectionRepository,
            factory.alertRepository,
            factory.deviceStatusRepository
        ) as T
    }
}
