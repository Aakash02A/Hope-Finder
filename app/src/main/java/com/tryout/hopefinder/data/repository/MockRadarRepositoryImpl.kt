package com.tryout.hopefinder.data.repository

import com.tryout.hopefinder.domain.model.DetectionType
import com.tryout.hopefinder.domain.model.RadarDetection
import com.tryout.hopefinder.domain.model.RadarScanState
import com.tryout.hopefinder.domain.repository.RadarRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Mock implementation of RadarRepository that simulates the 5-probe pentagon cell
 * returning heartbeat and respiration signals.
 */
@Singleton
class MockRadarRepositoryImpl @Inject constructor() : RadarRepository {

    private val isScanning = MutableStateFlow(false)
    private val probeStatus = mutableMapOf(
        1 to true, 2 to true, 3 to true, 4 to true, 5 to true
    )
    
    // Sector zones for pentagon configuration
    private val sectorZones = listOf(
        "Alpha-1", "Beta-2", "Gamma-3", "Delta-4", "Epsilon-5"
    )

    override fun startScan(): Flow<RadarScanState> = flow {
        isScanning.value = true
        var currentSector = 0
        val allDetections = mutableListOf<RadarDetection>()
        
        while (currentCoroutineContext().isActive && isScanning.value) {
            // Simulate scanning through each sector
            for (sector in 0 until 5) {
                if (!isScanning.value) break
                
                currentSector = sector + 1
                val progress = (sector + 1) / 5f
                
                // Emit current scan state
                emit(
                    RadarScanState(
                        isScanning = true,
                        currentSector = currentSector,
                        totalSectors = 5,
                        scanProgress = progress,
                        detections = allDetections.toList()
                    )
                )
                
                // Simulate probe detection delay (sweeping animation time)
                delay(SCAN_SECTOR_DELAY)
                
                // Random chance to detect something in this sector
                if (Random.nextFloat() < DETECTION_PROBABILITY) {
                    val detection = generateRandomDetection(currentSector)
                    allDetections.add(detection)
                    
                    emit(
                        RadarScanState(
                            isScanning = true,
                            currentSector = currentSector,
                            totalSectors = 5,
                            scanProgress = progress,
                            detections = allDetections.toList()
                        )
                    )
                }
            }
            
            // Full scan complete, pause before next sweep
            delay(SWEEP_PAUSE_DELAY)
        }
        
        // Scan stopped
        emit(
            RadarScanState(
                isScanning = false,
                currentSector = 0,
                totalSectors = 5,
                scanProgress = 0f,
                detections = allDetections.toList()
            )
        )
    }

    override suspend fun stopScan() {
        isScanning.value = false
    }

    override fun getDetectionStream(): Flow<RadarDetection> = flow {
        while (currentCoroutineContext().isActive && isScanning.value) {
            delay(Random.nextLong(500, 2000))
            if (Random.nextFloat() < STREAM_DETECTION_PROBABILITY) {
                emit(generateRandomDetection(Random.nextInt(1, 6)))
            }
        }
    }

    override fun rescanSector(sectorId: Int): Flow<RadarDetection> = flow {
        require(sectorId in 1..5) { "Sector ID must be between 1 and 5" }
        
        // Simulate focused rescan of a single sector
        repeat(RESCAN_ITERATIONS) {
            delay(RESCAN_DELAY)
            if (Random.nextFloat() < RESCAN_DETECTION_PROBABILITY) {
                emit(generateRandomDetection(sectorId))
            }
        }
    }

    override suspend fun isSystemOnline(): Boolean {
        // Simulate network check delay
        delay(100)
        return probeStatus.values.all { it }
    }

    override suspend fun getProbeStatus(): Map<Int, Boolean> {
        delay(50)
        return probeStatus.toMap()
    }
    
    /**
     * Generate a random detection for simulation purposes.
     */
    private fun generateRandomDetection(probeId: Int): RadarDetection {
        val type = when (Random.nextInt(100)) {
            in 0..39 -> DetectionType.HEARTBEAT   // 40% chance
            in 40..69 -> DetectionType.BREATHING  // 30% chance
            else -> DetectionType.MOVEMENT        // 30% chance
        }
        
        return RadarDetection(
            id = UUID.randomUUID().toString(),
            type = type,
            zoneLocation = sectorZones.getOrElse(probeId - 1) { "Unknown" },
            distance = Random.nextFloat() * 10f, // 0-10 meters
            signalStrength = Random.nextInt(20, 100),
            confidenceScore = Random.nextFloat() * 60f + 40f, // 40-100%
            probeId = probeId,
            timestamp = System.currentTimeMillis()
        )
    }
    
    companion object {
        private const val SCAN_SECTOR_DELAY = 1500L      // 1.5 seconds per sector
        private const val SWEEP_PAUSE_DELAY = 2000L      // 2 seconds between full sweeps
        private const val DETECTION_PROBABILITY = 0.4f   // 40% chance per sector
        private const val STREAM_DETECTION_PROBABILITY = 0.3f
        private const val RESCAN_DETECTION_PROBABILITY = 0.6f
        private const val RESCAN_ITERATIONS = 3
        private const val RESCAN_DELAY = 800L
    }
}
