/**
 * HB100 Sensor Implementation
 */

#include "hb100_sensor.h"

HB100Sensor::HB100Sensor() 
    : current_baseline(0.5f), 
      filtered_voltage_prev(0.5f),
      last_sample_time(0),
      total_samples(0),
      is_initialized(false),
      min_voltage_seen(3.3f),
      max_voltage_seen(0.0f) {
}

HB100Sensor::~HB100Sensor() {
}

bool HB100Sensor::begin() {
    // Configure ADC
    analogSetWidth(12);  // 12-bit resolution
    analogSetAttenuation(ADC_11db);  // 0-3.3V range
    
    // Initial baseline
    current_baseline = 1.65f;  // Mid-range for LM358 output
    
    is_initialized = true;
    last_sample_time = millis();
    
    return true;
}

HB100Sensor::SensorReading HB100Sensor::readSensor() {
    uint32_t current_time = millis();
    uint16_t raw = analogRead(ADC_PIN);
    
    // Convert to voltage (0-3.3V)
    float voltage = (float)raw / ADC_MAX * VREF;
    
    // Apply low-pass filter
    filtered_voltage_prev = 0.7f * filtered_voltage_prev + 0.3f * voltage;
    
    // Track voltage for diagnostics
    if (voltage < min_voltage_seen) min_voltage_seen = voltage;
    if (voltage > max_voltage_seen) max_voltage_seen = voltage;
    
    // Add to window
    voltage_window.push_back(filtered_voltage_prev);
    sample_timestamps.push_back(current_time);
    
    // Keep window size bounded
    if (voltage_window.size() > FILTER_WINDOW) {
        voltage_window.pop_front();
        sample_timestamps.pop_front();
    }
    
    // Update baseline
    updateBaseline();
    
    // Calculate metrics
    float delta = filtered_voltage_prev - current_baseline;
    
    // Check signal health
    bool healthy = (voltage >= 0.1f && voltage <= 3.2f);
    
    total_samples++;
    last_sample_time = current_time;
    
    return {
        current_time,
        raw,
        voltage,
        filtered_voltage_prev,
        current_baseline,
        delta,
        healthy
    };
}

void HB100Sensor::setBaseline(float baseline) {
    current_baseline = baseline;
}

float HB100Sensor::getSignalToNoise() const {
    if (voltage_window.empty()) return 0.0f;
    
    float avg = 0.0f;
    for (float v : voltage_window) {
        avg += v;
    }
    avg /= voltage_window.size();
    
    float noise = 0.0f;
    for (float v : voltage_window) {
        noise += (v - avg) * (v - avg);
    }
    noise = sqrt(noise / voltage_window.size());
    
    if (noise < 0.01f) noise = 0.01f;  // Avoid division by zero
    
    float signal = fabs(avg - current_baseline);
    return signal / noise;
}

float HB100Sensor::getAverageSignalStrength() const {
    if (voltage_window.empty()) return 0.0f;
    
    float sum = 0.0f;
    for (float v : voltage_window) {
        sum += fabs(v - current_baseline);
    }
    return sum / voltage_window.size();
}

bool HB100Sensor::isSignalHealthy() const {
    if (voltage_window.empty()) return false;
    
    // Check for stuck values
    float avg = 0.0f;
    for (float v : voltage_window) {
        avg += v;
    }
    avg /= voltage_window.size();
    
    // Variance should not be zero (signal stuck)
    float variance = 0.0f;
    for (float v : voltage_window) {
        variance += (v - avg) * (v - avg);
    }
    variance /= voltage_window.size();
    
    return variance > 0.0001f;  // Non-zero variance indicates active signal
}

void HB100Sensor::resetBaseline() {
    voltage_window.clear();
    sample_timestamps.clear();
    filtered_voltage_prev = current_baseline;
}

HB100Sensor::Diagnostics HB100Sensor::getDiagnostics() const {
    return {
        total_samples,
        min_voltage_seen,
        max_voltage_seen,
        (min_voltage_seen + max_voltage_seen) / 2.0f,
        getAverageSignalStrength(),
        isSignalHealthy()
    };
}

void HB100Sensor::updateBaseline() {
    // Slowly update baseline (exponential moving average)
    // This removes any DC offset drift
    if (voltage_window.size() > 10) {
        float window_avg = 0.0f;
        for (float v : voltage_window) {
            window_avg += v;
        }
        window_avg /= voltage_window.size();
        
        // Only update if not in motion
        float variance = 0.0f;
        for (float v : voltage_window) {
            variance += (v - window_avg) * (v - window_avg);
        }
        variance /= voltage_window.size();
        
        if (variance < 0.01f) {
            // Low variance = likely no motion, update baseline
            current_baseline = 0.95f * current_baseline + 0.05f * window_avg;
        }
    }
}

void HB100Sensor::calculateMetrics() {
    // Implemented in getDiagnostics
}
