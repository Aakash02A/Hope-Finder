/**
 * HB100 Doppler Radar Sensor Handler
 * Handles ADC reading and signal conditioning from HB100 via LM358
 */

#ifndef HB100_SENSOR_H
#define HB100_SENSOR_H

#include <Arduino.h>
#include <deque>

class HB100Sensor {
public:
    // ADC Configuration
    static const uint8_t ADC_PIN = 34;          // GPIO34 - ADC1_CH6
    static const uint16_t ADC_MAX = 4095;       // 12-bit resolution
    static const float VREF = 3.3f;             // Reference voltage
    
    // Sampling Configuration
    static const uint16_t SAMPLE_RATE_HZ = 200; // Samples per second
    static const uint16_t FILTER_WINDOW = 50;   // Moving average window
    
    struct SensorReading {
        uint32_t timestamp;
        uint16_t raw_adc;           // Raw 12-bit ADC value (0-4095)
        float raw_voltage;          // Converted to voltage (0-3.3V)
        float filtered_voltage;     // After digital filtering
        float baseline_voltage;     // Ambient level
        float delta_from_baseline;  // Signal deviation
        bool healthy;               // Signal validity flag
    };

    HB100Sensor();
    ~HB100Sensor();

    /**
     * Initialize ADC and begin sampling
     */
    bool begin();

    /**
     * Read latest sensor value
     */
    SensorReading readSensor();

    /**
     * Get current baseline (ambient level)
     */
    float getBaseline() const { return current_baseline; }

    /**
     * Manual baseline set/update
     */
    void setBaseline(float baseline);

    /**
     * Get signal-to-noise ratio
     */
    float getSignalToNoise() const;

    /**
     * Get average signal strength over last window
     */
    float getAverageSignalStrength() const;

    /**
     * Check if signal is healthy
     */
    bool isSignalHealthy() const;

    /**
     * Clear and reinitialize baseline tracking
     */
    void resetBaseline();

    /**
     * Get diagnostic data
     */
    struct Diagnostics {
        uint16_t total_samples;
        float min_voltage;
        float max_voltage;
        float average_voltage;
        float noise_level;
        bool adc_stable;
    } getDiagnostics() const;

private:
    // Signal conditioning
    void applyLowPassFilter();
    void updateBaseline();
    void calculateMetrics();

    // State variables
    float current_baseline;
    float filtered_voltage_prev;
    std::deque<float> voltage_window;
    std::deque<uint32_t> sample_timestamps;
    
    uint32_t last_sample_time;
    uint16_t total_samples;
    bool is_initialized;
    
    // Diagnostics
    float min_voltage_seen;
    float max_voltage_seen;
};

#endif // HB100_SENSOR_H
