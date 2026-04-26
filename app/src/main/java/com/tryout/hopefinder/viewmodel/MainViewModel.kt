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
    var deviceIp: String = "192.168.4.1"
    
    fun initialize(ip: String) {
        deviceIp = ip
        apiClient = DeviceApiClient("http://$ip")
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

    private val _isUserScanning = MutableStateFlow(false)
    val isUserScanning: StateFlow<Boolean> = _isUserScanning.asStateFlow()

    fun setUserScanning(scanning: Boolean) {
        _isUserScanning.value = scanning
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
                    
                    // Also try to fetch radar data directly
                    val dataResult = apiClient.fetchEsp32Data()
                    
                    if (statusResult.isSuccess || dataResult.isSuccess) {
                        _deviceConnected.value = true
                        consecutiveFailures = 0
                        
                        val status = if (statusResult.isSuccess) {
                            val response = statusResult.getOrThrow()
                            DeviceStatusEntity(
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
                        } else {
                            // Synthesize status from /data presence
                            DeviceStatusEntity(
                                timestamp = System.currentTimeMillis(),
                                deviceId = "ESP32_RADAR",
                                wifiConnected = true,
                                rssi = -50,
                                radarHealthy = true,
                                calibrated = true,
                                scanning = _isUserScanning.value, // Follow user intent instead of forcing true
                                currentSector = 0,
                                currentAngle = 0,
                                uptime = 0,
                                cpuUsagePercent = 0,
                                memoryFreeBytes = 0
                            )
                        }
                        deviceStatusRepository.insertStatus(status)

                        dataResult.onSuccess { resp ->
                            // Helper to parse distance strings like "1m", ">4m"
                            fun parseDistance(dist: String): Float {
                                val numeric = dist.replace(Regex("[^0-9.]"), "")
                                val value = numeric.toFloatOrNull() ?: 5.0f
                                // Normalize 0-1: 1.0 is close (0m), 0.1 is far (5m+)
                                return (1.0f - (value / 5.0f)).coerceIn(0.1f, 1.0f)
                            }

                            val confidence = if (resp.motion_detected) {
                                (50 + (resp.change_left + resp.change_right)).coerceIn(50, 100)
                            } else {
                                (resp.change_left + resp.change_right).coerceIn(0, 49)
                            }

                            if (resp.motion_detected || resp.change_left > 5 || resp.change_right > 5) {
                                val isLeft = resp.change_left >= resp.change_right
                                val distStr = if (isLeft) resp.left_distance else resp.right_distance
                                
                                val detection = DetectionEntity(
                                    eventId = "esp32_${resp.timestamp}_${System.currentTimeMillis()}",
                                    timestamp = System.currentTimeMillis(),
                                    sector = if (isLeft) 10 else 2,
                                    sectorLabel = if (isLeft) "LEFT" else "RIGHT",
                                    angle = if (isLeft) 300 else 60,
                                    rawSignal = resp.change_left.toFloat(),
                                    filteredSignal = resp.change_right.toFloat(),
                                    baseline = 0f,
                                    signalStrength = parseDistance(distStr), // Use for distance mapping
                                    motionLevel = if (resp.motion_detected) 3 else 1,
                                    motionLabel = if (resp.motion_detected) "MOTION" else "STILL",
                                    confidence = confidence,
                                    confidenceLevel = when {
                                        confidence >= 75 -> "HIGH"
                                        confidence >= 50 -> "MEDIUM"
                                        else -> "LOW"
                                    },
                                    humanPresencePossible = resp.motion_detected,
                                    wifiRssi = 0,
                                    scanDurationMs = 0,
                                    deviceId = "ESP32_RADAR"
                                )
                                detectionRepository.insertDetection(detection)
                            }
                        }
                    } else {
                        _deviceConnected.value = false
                        consecutiveFailures++
                        
                        // Insert disconnected status so UI knows to stop
                        val disconnectedStatus = DeviceStatusEntity(
                            timestamp = System.currentTimeMillis(),
                            deviceId = "ESP32_RADAR",
                            wifiConnected = false,
                            rssi = -100,
                            radarHealthy = false,
                            calibrated = false,
                            scanning = false,
                            currentSector = 0,
                            currentAngle = 0,
                            uptime = 0
                        )
                        deviceStatusRepository.insertStatus(disconnectedStatus)
                    }
                    
                } catch (e: Exception) {
                    _deviceConnected.value = false
                    consecutiveFailures++
                    Log.e("MainViewModel", "Polling exception (attempt $consecutiveFailures)", e)
                    
                    // Insert disconnected status
                    val disconnectedStatus = DeviceStatusEntity(
                        timestamp = System.currentTimeMillis(),
                        deviceId = "ESP32_RADAR",
                        wifiConnected = false,
                        rssi = -100,
                        radarHealthy = false,
                        calibrated = false,
                        scanning = false,
                        currentSector = 0,
                        currentAngle = 0,
                        uptime = 0
                    )
                    deviceStatusRepository.insertStatus(disconnectedStatus)
                }
                
                if (consecutiveFailures < maxConsecutiveFailures) {
                    delay(1500)  // Poll faster (every 1.5s)
                    
                    // Periodically cleanup very old detections (older than 1 minute)
                    if (System.currentTimeMillis() % 10000 < 2000) {
                        detectionRepository.cleanupOldDetections(1) // Keep only very recent ones
                    }
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
            factory.deviceStatusRepository
        ) as T
    }
}
