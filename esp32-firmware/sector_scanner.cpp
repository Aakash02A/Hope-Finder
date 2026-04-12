/**
 * Sector Scanner Implementation
 */

#include "sector_scanner.h"

// Static sector definitions
const SectorScanner::Sector SectorScanner::sectors[NUM_SECTORS] = {
    {0,   0, 15, "N"},     // North
    {1,  30, 45, "NNE"},   // North-Northeast
    {2,  60, 75, "NE"},    // Northeast
    {3,  90, 105, "ENE"},  // East-Northeast
    {4, 120, 135, "E"},    // East
    {5, 150, 165, "ESE"},  // East-Southeast
    {6, 180, 195, "SE"},   // Southeast
    {7, 210, 225, "SSE"},  // South-Southeast
    {8, 240, 255, "S"},    // South
    {9, 270, 285, "SSW"},  // South-Southwest
    {10, 300, 315, "SW"},  // Southwest
    {11, 330, 345, "WSW"}  // West-Southwest
};

SectorScanner::SectorScanner()
    : motor_enabled(false),
      last_update_time(0),
      total_rotations_completed(0) {
    
    current_state = {
        0,                 // current_sector
        0,                 // current_angle
        0,                 // sector_start_time
        3000,              // sector_dwell_ms (3 seconds per sector)
        false,             // is_scanning
        0,                 // scan_start_time
        0                  // completed_rotations
    };
}

bool SectorScanner::begin(uint32_t sector_dwell_ms, bool motor_enabled_) {
    motor_enabled = motor_enabled_;
    current_state.sector_dwell_ms = sector_dwell_ms;
    
    if (motor_enabled) {
        configureMotor();
    }
    
    return true;
}

const SectorScanner::Sector* SectorScanner::getSectorDefinitions() {
    return sectors;
}

const SectorScanner::Sector& SectorScanner::getSector(uint8_t index) {
    if (index >= NUM_SECTORS) index = NUM_SECTORS - 1;
    return sectors[index];
}

uint8_t SectorScanner::angleToSector(uint16_t angle) {
    angle = angle % 360;  // Normalize to 0-359
    // Find sector that contains this angle
    for (uint8_t i = 0; i < NUM_SECTORS; i++) {
        uint16_t start = sectors[i].start_angle;
        uint16_t end = (i == NUM_SECTORS - 1) ? 360 : sectors[i + 1].start_angle;
        if (angle >= start && angle < end) {
            return i;
        }
    }
    return 0;
}

const char* SectorScanner::getSectorLabel(uint8_t sector) {
    if (sector >= NUM_SECTORS) sector = NUM_SECTORS - 1;
    return sectors[sector].label;
}

void SectorScanner::startScan(uint16_t cycle_duration_ms) {
    current_state.is_scanning = true;
    current_state.scan_start_time = millis();
    current_state.sector_start_time = millis();
    current_state.current_sector = 0;
    current_state.current_angle = sectors[0].center_angle;
    
    // Calculate dwell time based on cycle duration
    current_state.sector_dwell_ms = cycle_duration_ms / NUM_SECTORS;
}

void SectorScanner::stopScan() {
    current_state.is_scanning = false;
}

void SectorScanner::update() {
    if (!current_state.is_scanning) return;
    
    uint32_t now = millis();
    uint32_t time_in_sector = now - current_state.sector_start_time;
    
    // Check if it's time to advance to next sector
    if (time_in_sector >= current_state.sector_dwell_ms) {
        advanceSector();
    }
    
    // Update motor position if enabled
    if (motor_enabled) {
        updateMotorPosition(current_state.current_angle);
    }
    
    last_update_time = now;
}

const char* SectorScanner::getCurrentSectorLabel() const {
    return sectors[current_state.current_sector].label;
}

void SectorScanner::advanceSector() {
    current_state.current_sector = (current_state.current_sector + 1) % NUM_SECTORS;
    current_state.current_angle = sectors[current_state.current_sector].center_angle;
    current_state.sector_start_time = millis();
    
    // Track completed rotations
    if (current_state.current_sector == 0) {
        current_state.completed_rotations++;
        total_rotations_completed++;
    }
}

SectorScanner::Diagnostics SectorScanner::getDiagnostics() const {
    return {
        current_state.completed_rotations,
        current_state.completed_rotations * NUM_SECTORS + current_state.current_sector,
        millis() - current_state.scan_start_time,
        motor_enabled
    };
}

void SectorScanner::updateMotorPosition(uint16_t angle) {
    // Motor control logic (PWM servo or stepper)
    // GPIO5: PWM (servo control)
    // GPIO18: Direction (stepper direction)
    
    // For prototype without motor: no-op
    // In full implementation:
    // - Map angle (0-359) to PWM duty cycle (1000-2000 µs for servos)
    // - Or send step/direction pulses for stepper
}

void SectorScanner::configureMotor() {
    // Configure motor GPIO pins
    pinMode(5, OUTPUT);   // PWM output for servo
    pinMode(18, OUTPUT);  // Direction output for stepper
}
