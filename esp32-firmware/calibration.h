/**
 * Calibration Module
 * Handles baseline establishment and threshold tuning
 */

#ifndef CALIBRATION_H
#define CALIBRATION_H

#include <Arduino.h>

class Calibration {
public:
    struct CalibrationProfile {
        float baseline_voltage;
        float motion_threshold;
        float sensitivity_multiplier;
        uint8_t confidence_floor;
        uint32_t calibration_timestamp;
        bool is_valid;
    };

    typedef void (*OnProgressCallback)(uint8_t percent, float baseline);

    Calibration();

    /**
     * Start calibration sequence (60 seconds recommended)
     */
    bool begin(uint32_t duration_ms = 60000);

    /**
     * Check if calibration is in progress
     */
    bool isInProgress() const { return in_progress; }

    /**
     * Get calibration progress (0-100%)
     */
    uint8_t getProgress() const;

    /**
     * Update calibration with new sensor reading
     */
    void processReading(float sensor_voltage);

    /**
     * Finalize calibration and store profile
     */
    bool finalize();

    /**
     * Get current calibration profile
     */
    const CalibrationProfile& getProfile() const { return profile; }

    /**
     * Manually set calibration profile
     */
    void setProfile(const CalibrationProfile& new_profile);

    /**
     * Load calibration from EEPROM/SPIFFS
     */
    bool loadFromStorage();

    /**
     * Save calibration to storage
     */
    bool saveToStorage();

    /**
     * Reset to defaults
     */
    void resetToDefaults();

    /**
     * Set progress callback
     */
    void setProgressCallback(OnProgressCallback callback) { on_progress = callback; }

    /**
     * Adjust motion threshold after calibration
     */
    void setMotionThreshold(float threshold);

    /**
     * Adjust sensitivity multiplier (1.0 = default)
     */
    void setSensitivityMultiplier(float multiplier);

    /**
     * Set minimum confidence to report
     */
    void setConfidenceFloor(uint8_t floor);

private:
    void updateEstimate(float reading);

    CalibrationProfile profile;
    bool in_progress;
    uint32_t calibration_start_time;
    uint32_t calibration_duration_ms;
    
    float min_reading;
    float max_reading;
    float sum_readings;
    uint32_t sample_count;
    
    OnProgressCallback on_progress;
    
    // Default calibration values
    static const float DEFAULT_BASELINE;
    static const float DEFAULT_THRESHOLD;
    static const float DEFAULT_SENSITIVITY;
    static const uint8_t DEFAULT_CONFIDENCE_FLOOR;
};

#endif // CALIBRATION_H
