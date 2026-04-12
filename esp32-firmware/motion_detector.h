/**
 * Motion Detection Engine
 * Processes sensor data to detect and classify motion patterns
 */

#ifndef MOTION_DETECTOR_H
#define MOTION_DETECTOR_H

#include <Arduino.h>
#include <deque>

class MotionDetector {
public:
    // Motion classification levels
    enum MotionLevel {
        MOTION_NONE = 0,                  // No motion detected
        MOTION_WEAK = 1,                  // Weak motion
        MOTION_POSSIBLE_HUMAN = 2,        // Possible human movement
        MOTION_STRONG = 3                 // Strong/high confidence motion
    };

    struct Detection {
        uint32_t timestamp;
        uint8_t sector;
        uint16_t angle;
        float raw_signal;                 // Raw signal amplitude
        float filtered_signal;            // Smoothed signal
        float baseline;                   // Baseline at detection time
        float signal_strength;            // Normalized strength (0-1)
        uint8_t motion_level;             // MotionLevel enum
        uint8_t confidence;               // 0-100%
        bool human_presence_possible;     // Flag for reporting
    };

    struct MotionStats {
        float peak_signal;
        float average_signal;
        uint16_t peak_count;              // Number of peaks detected
        uint32_t duration_ms;             // Duration of motion event
        float consistency;                // Consistency score (0-1)
    };

    MotionDetector();

    /**
     * Initialize motion detector with calibration data
     */
    bool begin(float baseline_volts, float motion_threshold);

    /**
     * Process single sensor reading
     */
    Detection processReading(float filtered_signal, float baseline, 
                            uint8_t sector, uint16_t angle);

    /**
     * Update baseline estimation
     */
    void updateBaseline(float new_baseline);

    /**
     * Get current motion threshold
     */
    float getMotionThreshold() const { return motion_threshold; }

    /**
     * Adjust motion threshold (and sensitivity)
     */
    void setMotionThreshold(float threshold);

    /**
     * Adjust sensitivity multiplier (1.0 = default)
     */
    void setSensitivityMultiplier(float multiplier);

    /**
     * Get current confidence floor (minimum to report)
     */
    uint8_t getConfidenceFloor() const { return confidence_floor; }

    /**
     * Set minimum confidence to report
     */
    void setConfidenceFloor(uint8_t floor);

    /**
     * Get motion statistics for recent detections
     */
    MotionStats getRecentStats(uint32_t time_window_ms = 5000);

    /**
     * Check if signal indicates possible human presence
     */
    bool isPossibleHumanPresence(float signal_strength, uint8_t consistency);

    /**
     * Reset motion tracking (start new event window)
     */
    void resetEventWindow();

    /**
     * Get diagnostic info
     */
    struct Diagnostics {
        uint32_t events_detected;
        float current_threshold;
        float sensitivity_mult;
        uint16_t samples_in_window;
        float peak_signal_recent;
    } getDiagnostics() const;

private:
    // Signal analysis
    void analyzeSignalPeaks(float signal);
    uint8_t calculateConfidence(float signal_strength, float consistency);
    MotionLevel classifyMotion(uint8_t confidence);
    
    // State variables
    float motion_threshold;
    float sensitivity_multiplier;
    uint8_t confidence_floor;
    float current_baseline;
    
    // Event tracking
    std::deque<float> signal_window;
    std::deque<uint32_t> peak_times;
    uint32_t event_start_time;
    float event_peak_signal;
    
    // Diagnostics
    uint32_t total_detections;
    uint32_t last_peak_time;
    
    static const uint16_t MAX_WINDOW_SIZE = 500;
    static const uint32_t EVENT_TIMEOUT_MS = 2000;
};

#endif // MOTION_DETECTOR_H
