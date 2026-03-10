package com.tryout.hopefinder.domain.repository

import com.tryout.hopefinder.domain.model.RadarDetection
import com.tryout.hopefinder.domain.model.RadarScanState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for radar operations.
 * This abstracts the hardware simulation layer from the rest of the app.
 */
interface RadarRepository {
    
    /**
     * Start a radar scan across all 5 probes in the pentagon cell.
     * @return Flow of scan states as the scan progresses.
     */
    fun startScan(): Flow<RadarScanState>
    
    /**
     * Stop the current radar scan.
     */
    suspend fun stopScan()
    
    /**
     * Get real-time detection stream from all probes.
     * @return Flow of detections as they occur.
     */
    fun getDetectionStream(): Flow<RadarDetection>
    
    /**
     * Rescan a specific sector/zone.
     * @param sectorId The sector to rescan (1-5).
     * @return Flow of detections from that sector.
     */
    fun rescanSector(sectorId: Int): Flow<RadarDetection>
    
    /**
     * Check if the radar system is online.
     * @return true if all probes are responding.
     */
    suspend fun isSystemOnline(): Boolean
    
    /**
     * Get the current probe status for all 5 probes.
     * @return Map of probe ID to online status.
     */
    suspend fun getProbeStatus(): Map<Int, Boolean>
}
