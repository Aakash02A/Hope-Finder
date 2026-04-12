/**
 * Sector Scanner
 * Manages directional scanning and sector-to-angle mapping
 */

#ifndef SECTOR_SCANNER_H
#define SECTOR_SCANNER_H

#include <Arduino.h>
#include <vector>

class SectorScanner {
public:
    // 12 sectors (30 degrees each)
    static const uint8_t NUM_SECTORS = 12;
    static const uint16_t SECTOR_WIDTH = 30;  // degrees
    
    struct Sector {
        uint8_t index;              // 0-11
        uint16_t start_angle;       // 0-330 (0° = North)
        uint16_t center_angle;      // Center of sector
        const char* label;          // "N", "NNE", "NE", etc.
    };

    struct ScanState {
        uint8_t current_sector;
        uint16_t current_angle;
        uint32_t sector_start_time;
        uint32_t sector_dwell_ms;   // Time per sector
        bool is_scanning;
        uint32_t scan_start_time;
        uint32_t completed_rotations;
    };

    SectorScanner();

    /**
     * Initialize scanner with scanning parameters
     */
    bool begin(uint32_t sector_dwell_ms = 3000, bool motor_enabled = false);

    /**
     * Get all sector definitions
     */
    static const Sector* getSectorDefinitions();

    /**
     * Get sector by index
     */
    static const Sector& getSector(uint8_t index);

    /**
     * Get sector from angle
     */
    static uint8_t angleToSector(uint16_t angle);

    /**
     * Get sector label
     */
    static const char* getSectorLabel(uint8_t sector);

    /**
     * Start continuous scanning
     */
    void startScan(uint16_t cycle_duration_ms = 36000);  // 12 sectors * 3s = 36s

    /**
     * Stop scanning
     */
    void stopScan();

    /**
     * Check if currently scanning
     */
    bool isScanning() const { return current_state.is_scanning; }

    /**
     * Update scanner state (call regularly)
     */
    void update();

    /**
     * Get current scan state
     */
    const ScanState& getScanState() const { return current_state; }

    /**
     * Get current sector
     */
    uint8_t getCurrentSector() const { return current_state.current_sector; }

    /**
     * Get current angle (0-359)
     */
    uint16_t getCurrentAngle() const { return current_state.current_angle; }

    /**
     * Get current sector label
     */
    const char* getCurrentSectorLabel() const;

    /**
     * Set sector dwell time
     */
    void setSectorDwellMs(uint32_t dwell_ms) { current_state.sector_dwell_ms = dwell_ms; }

    /**
     * Force sector advance (for testing)
     */
    void advanceSector();

    /**
     * Get diagnostics
     */
    struct Diagnostics {
        uint32_t total_rotations;
        uint32_t total_sectors_scanned;
        uint32_t scan_uptime_ms;
        bool motor_connected;
    } getDiagnostics() const;

private:
    // Static sector definitions
    static const Sector sectors[NUM_SECTORS];

    // Motor control
    void updateMotorPosition(uint16_t angle);
    void configureMotor();

    ScanState current_state;
    bool motor_enabled;
    uint32_t last_update_time;
    
    uint32_t total_rotations_completed;
};

#endif // SECTOR_SCANNER_H
