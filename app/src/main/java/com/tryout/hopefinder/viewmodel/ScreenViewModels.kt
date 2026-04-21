package com.tryout.hopefinder.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tryout.hopefinder.data.*
import com.tryout.hopefinder.network.DeviceApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard ViewModel
 * Manages dashboard screen state: device status, connection, latest detection
 */
class DashboardViewModel(
    private val deviceStatusRepository: DeviceStatusRepository,
    private val detectionRepository: DetectionRepository,
    private val alertRepository: AlertRepository
) : ViewModel() {

    val deviceStatus: StateFlow<DeviceStatusEntity?> = deviceStatusRepository.getLatestStatus()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val recentDetections: StateFlow<List<DetectionEntity>> = detectionRepository.getRecentDetections(5)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unacknowledgedAlerts: StateFlow<List<AlertEntity>> = alertRepository.getUnacknowledgedAlerts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val latestDetection: StateFlow<DetectionEntity?> = recentDetections.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val alertCount: StateFlow<Int> = unacknowledgedAlerts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanStatus = MutableStateFlow<String?>(null)
    val scanStatus: StateFlow<String?> = _scanStatus.asStateFlow()

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            alertRepository.acknowledgeAlert(alertId)
        }
    }

    fun startScan(durationSeconds: Int = 300) {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                _isScanning.value = false
                _scanStatus.value = "Starting scan..."
                val result = apiClient.startScan(durationSeconds)
                result.onSuccess { response ->
                    _isScanning.value = true
                    _scanStatus.value = "Scan started successfully"
                    refreshDeviceStatus()
                }
                result.onFailure { error ->
                    _isScanning.value = false
                    _scanStatus.value = "Failed to start scan: ${error.message}"
                }
            } catch (e: Exception) {
                _isScanning.value = false
                _scanStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                _scanStatus.value = "Stopping scan..."
                val result = apiClient.stopScan()
                result.onSuccess { response ->
                    _isScanning.value = false
                    _scanStatus.value = "Scan stopped"
                    refreshDeviceStatus()
                }
                result.onFailure { error ->
                    _scanStatus.value = "Failed to stop scan: ${error.message}"
                }
            } catch (e: Exception) {
                _scanStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun startCalibration() {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                _scanStatus.value = "Starting calibration..."
                val result = apiClient.startCalibration()
                result.onSuccess { response ->
                    _scanStatus.value = "Calibration started successfully"
                    refreshDeviceStatus()
                }
                result.onFailure { error ->
                    _scanStatus.value = "Failed to start calibration: ${error.message}"
                }
            } catch (e: Exception) {
                _scanStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun refreshDeviceStatus() {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                val result = apiClient.getDeviceStatus()
                result.onSuccess { response ->
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
                    _isScanning.value = response.data.system.scanning
                }
            } catch (e: Exception) {
                // Error handled locally
            }
        }
    }

    fun startStatusPolling() {
        viewModelScope.launch {
            while (isActive) {
                refreshDeviceStatus()
                delay(1000) // Poll every 1s instead of 5s
            }
        }
    }
}

/**
 * Radar ViewModel
 * Manages radar screen state: detections, animation state, scanning
 */
class RadarViewModel(
    private val detectionRepository: DetectionRepository,
    private val sessionRepository: ScanSessionRepository
) : ViewModel() {

    private val _currentSector = MutableStateFlow(0)
    val currentSector: StateFlow<Int> = _currentSector.asStateFlow()

    private val _currentAngle = MutableStateFlow(0)
    val currentAngle: StateFlow<Int> = _currentAngle.asStateFlow()

    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress.asStateFlow()

    val recentDetections: StateFlow<List<DetectionEntity>> = detectionRepository.getRecentDetections(100)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _scanStatus = MutableStateFlow<String?>(null)
    val scanStatus: StateFlow<String?> = _scanStatus.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Start sweep animation
        startSweepAnimation()
        // Monitor scan state to start/stop polling
        monitorScanState()
    }

    private fun monitorScanState() {
        viewModelScope.launch {
            // This is a simplified check; in a real app, you'd observe DashboardViewModel's scan state
            while (isActive) {
                val isDeviceScanning = DeviceConnectionManager.apiClient?.getDeviceStatus()?.getOrNull()?.data?.system?.scanning ?: false
                if (isDeviceScanning && pollingJob == null) {
                    startPollingEvents()
                } else if (!isDeviceScanning && pollingJob != null) {
                    pollingJob?.cancel()
                    pollingJob = null
                }
                delay(1000)
            }
        }
    }

    private fun startPollingEvents() {
        pollingJob = viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            while (isActive) {
                val result = apiClient.getLiveEvents()
                result.onSuccess { events ->
                    events.forEach { resp ->
                        val entity = DetectionEntity(
                            eventId = resp.event_id,
                            timestamp = parseDeviceTimestamp(resp.timestamp),
                            sector = resp.sector,
                            sectorLabel = resp.sector_label,
                            angle = resp.angle,
                            rawSignal = resp.raw_signal,
                            filteredSignal = resp.filtered_signal,
                            baseline = resp.baseline,
                            signalStrength = resp.signal_strength,
                            motionLevel = resp.motion_level,
                            motionLabel = resp.motion_label,
                            confidence = resp.confidence,
                            confidenceLevel = resp.confidence_level,
                            humanPresencePossible = resp.human_presence_possible,
                            wifiRssi = resp.wifi_rssi,
                            scanDurationMs = resp.scan_duration_ms
                        )
                        detectionRepository.insertDetection(entity)
                        
                        // Auto-generate alert for high confidence
                        if (resp.confidence >= 50) {
                            // This would ideally be done in a Background Service or Repository
                            // but for parity we'll trigger it here or via Repository trigger
                        }
                    }
                }
                delay(250) // Poll events every 250ms for real-time
            }
        }
    }

    private fun startSweepAnimation() {
        viewModelScope.launch {
            while (isActive) {
                for (angle in 0..359 step 3) {
                    _currentAngle.value = angle
                    _currentSector.value = angle / 30  // 12 sectors
                    _animationProgress.value = angle / 360f
                    delay(30)  // ~12 FPS
                }
            }
        }
    }

    fun getDetectionsInSector(sector: Int): Flow<List<DetectionEntity>> {
        return detectionRepository.getDetectionsBySector(sector, limit = 50)
    }

    fun getHighConfidenceDetections(): Flow<List<DetectionEntity>> {
        return detectionRepository.getDetectionsByConfidence(60)
    }

    fun startScan(durationSeconds: Int = 300) {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                _scanStatus.value = "Starting scan..."
                val result = apiClient.startScan(durationSeconds)
                result.onSuccess { response ->
                    _scanStatus.value = "Scan started successfully"
                }
                result.onFailure { error ->
                    _scanStatus.value = "Failed to start scan: ${error.message}"
                }
            } catch (e: Exception) {
                _scanStatus.value = "Error: ${e.message}"
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            val apiClient = DeviceConnectionManager.apiClient ?: return@launch
            try {
                _scanStatus.value = "Stopping scan..."
                val result = apiClient.stopScan()
                result.onSuccess { response ->
                    _scanStatus.value = "Scan stopped"
                    // Close session
                    viewModelScope.launch {
                        sessionRepository.getCurrentSession().firstOrNull()?.let { session ->
                            val updated = session.copy(endTime = System.currentTimeMillis())
                            sessionRepository.updateSession(updated)
                        }
                    }
                }
                result.onFailure { error ->
                    _scanStatus.value = "Failed to stop scan: ${error.message}"
                }
            } catch (e: Exception) {
                _scanStatus.value = "Error: ${e.message}"
            }
        }
    }
}

/**
 * Alerts ViewModel
 * Manages alerts screen state: filtering, sorting, acknowledgment
 */
class AlertsViewModel(
    private val alertRepository: AlertRepository,
    private val detectionRepository: DetectionRepository
) : ViewModel() {

    private val _filterBySeverity = MutableStateFlow<String?>(null)
    private val _filterByConfidence = MutableStateFlow(0)
    private val _sortBy = MutableStateFlow("timestamp")  // "timestamp", "confidence"

    val unacknowledgedAlerts: StateFlow<List<AlertEntity>> = alertRepository.getUnacknowledgedAlerts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAlerts: StateFlow<List<AlertEntity>> = alertRepository.getRecentAlerts(200)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredAlerts: StateFlow<List<AlertEntity>> = combine(
        allAlerts,
        _filterBySeverity,
        _filterByConfidence,
        _sortBy
    ) { alerts, severity, minConfidence, sortBy ->
        var filtered = alerts
        
        if (severity != null) {
            filtered = filtered.filter { it.severity == severity }
        }
        
        filtered = filtered.filter { it.confidence >= minConfidence }
        
        when (sortBy) {
            "confidence" -> filtered.sortedByDescending { it.confidence }
            else -> filtered.sortedByDescending { it.timestamp }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSeverityFilter(severity: String?) {
        _filterBySeverity.value = severity
    }

    fun setConfidenceFilter(minConfidence: Int) {
        _filterByConfidence.value = minConfidence
    }

    fun setSortBy(sortKey: String) {
        _sortBy.value = sortKey
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            alertRepository.acknowledgeAlert(alertId)
        }
    }

    fun acknowledgeAllAlerts() {
        viewModelScope.launch {
            unacknowledgedAlerts.value.forEach { alert ->
                alertRepository.acknowledgeAlert(alert.id)
            }
        }
    }
}

/**
 * Reports ViewModel
 * Manages reports screen state: statistics, charts, export
 */
class ReportsViewModel(
    private val detectionRepository: DetectionRepository,
    private val reportRepository: ReportRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val _dateRangeStart = MutableStateFlow(System.currentTimeMillis() - (24 * 60 * 60 * 1000))
    private val _dateRangeEnd = MutableStateFlow(System.currentTimeMillis())

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    val reportsInRange: StateFlow<List<ReportSummaryEntity>> = combine(
        _dateRangeStart,
        _dateRangeEnd
    ) { start, end ->
        // Filter reports by date range
        emptyList<ReportSummaryEntity>()  // Implement backend query
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalDetections: StateFlow<Int> = detectionRepository.getRecentDetections(10000)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val avgConfidence: StateFlow<Float> = detectionRepository.getRecentDetections(10000)
        .map { detections ->
            if (detections.isEmpty()) 0f else detections.map { it.confidence }.average().toFloat()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val detectionsByConfidenceLevel: StateFlow<Map<String, Int>> = 
        detectionRepository.getRecentDetections(10000)
        .map { detections ->
            mapOf(
                "HIGH" to detections.count { it.confidence >= 75 },
                "MEDIUM" to detections.count { it.confidence in 41..74 },
                "LOW" to detections.count { it.confidence in 1..40 }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, mapOf())

    val detectionsBySector: StateFlow<Map<String, Int>> =
        detectionRepository.getRecentDetections(10000)
        .map { detections ->
            val sectorMap = mutableMapOf<String, Int>()
            detections.groupBy { it.sectorLabel }
                .forEach { (sector, events) ->
                    sectorMap[sector] = events.size
                }
            sectorMap
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, mapOf())

    fun setDateRange(startMs: Long, endMs: Long) {
        _dateRangeStart.value = startMs
        _dateRangeEnd.value = endMs
    }

    fun exportReportAsCSV() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportStatus.value = "Generating CSV..."

                val detections = detectionRepository.getRecentDetections(10000).first()
                val csv = generateCSV(detections)
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "hope_finder_report_$timestamp.csv"
                val file = File(context.filesDir, fileName)
                
                file.writeText(csv)
                
                _isExporting.value = false
                _exportStatus.value = "CSV exported successfully: $fileName"
                
                // Clear message after 3 seconds
                delay(3000)
                _exportStatus.value = null
            } catch (e: Exception) {
                _isExporting.value = false
                _exportStatus.value = "Failed to export CSV: ${e.message}"
            }
        }
    }

    fun exportReportAsPDF() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportStatus.value = "Generating PDF..."

                val detections = detectionRepository.getRecentDetections(10000).first()
                val csv = generateCSV(detections)
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "hope_finder_report_$timestamp.txt"  // Using TXT as PDF requires external library
                val file = File(context.filesDir, fileName)
                
                val pdfContent = """
                    HOPE-FINDER RADAR DETECTION REPORT
                    Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}
                    
                    SUMMARY
                    Total Detections: ${detections.size}
                    Average Confidence: ${String.format("%.1f%%", detections.map { it.confidence }.average())}
                    
                    DETECTIONS
                    $csv
                """.trimIndent()
                
                file.writeText(pdfContent)
                
                _isExporting.value = false
                _exportStatus.value = "Report exported successfully: $fileName"
                
                // Clear message after 3 seconds
                delay(3000)
                _exportStatus.value = null
            } catch (e: Exception) {
                _isExporting.value = false
                _exportStatus.value = "Failed to export report: ${e.message}"
            }
        }
    }

    fun shareReport() {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _exportStatus.value = "Preparing report to share..."

                val detections = detectionRepository.getRecentDetections(10000).first()
                val csv = generateCSV(detections)
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "hope_finder_report_$timestamp.csv"
                val file = File(context.filesDir, fileName)
                
                file.writeText(csv)
                
                _isExporting.value = false
                _exportStatus.value = "Report ready to share: $fileName"
                
                // Clear message after 3 seconds
                delay(3000)
                _exportStatus.value = null
            } catch (e: Exception) {
                _isExporting.value = false
                _exportStatus.value = "Failed to prepare report: ${e.message}"
            }
        }
    }

    private fun generateCSV(detections: List<DetectionEntity>): String {
        val sb = StringBuilder()
        sb.append("Sector,Angle,Confidence,Motion Level,Timestamp\n")
        
        detections.forEach { detection ->
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(detection.timestamp))
            sb.append("${detection.sectorLabel},${detection.angle},${detection.confidence},${detection.motionLabel},$timestamp\n")
        }
        
        return sb.toString()
    }
}

// ViewModel Factories

class DashboardViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val factory = DataRepositoryFactory(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(
            factory.deviceStatusRepository,
            factory.detectionRepository,
            factory.alertRepository
        ) as T
    }
}

class RadarViewModelFactory(context: android.content.Context) : ViewModelProvider.Factory {
    private val factory = DataRepositoryFactory(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RadarViewModel(
            factory.detectionRepository,
            factory.scanSessionRepository
        ) as T
    }
}

class AlertsViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val factory = DataRepositoryFactory(context)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlertsViewModel(
            factory.alertRepository,
            factory.detectionRepository
        ) as T
    }
}

class ReportsViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val factory = DataRepositoryFactory(context)
    private val appContext = context

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReportsViewModel(
            factory.detectionRepository,
            factory.reportRepository,
            appContext
        ) as T
    }
}
