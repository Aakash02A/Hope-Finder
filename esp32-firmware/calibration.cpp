/**
 * Calibration Module Implementation
 */

#include "calibration.h"
#include <SPIFFS.h>
#include <ArduinoJson.h>

// Static default values
const float Calibration::DEFAULT_BASELINE = 1.65f;
const float Calibration::DEFAULT_THRESHOLD = 0.5f;
const float Calibration::DEFAULT_SENSITIVITY = 1.0f;
const uint8_t Calibration::DEFAULT_CONFIDENCE_FLOOR = 40;

Calibration::Calibration()
    : in_progress(false),
      calibration_start_time(0),
      calibration_duration_ms(0),
      min_reading(3.3f),
      max_reading(0.0f),
      sum_readings(0.0f),
      sample_count(0),
      on_progress(nullptr) {
    
    resetToDefaults();
}

bool Calibration::begin(uint32_t duration_ms) {
    in_progress = true;
    calibration_start_time = millis();
    calibration_duration_ms = duration_ms;
    
    // Reset accumulators
    min_reading = 3.3f;
    max_reading = 0.0f;
    sum_readings = 0.0f;
    sample_count = 0;
    
    Serial.printf("[CALIB] Starting %lu ms calibration\n", duration_ms);
    
    return true;
}

uint8_t Calibration::getProgress() const {
    if (!in_progress) return 0;
    
    uint32_t elapsed = millis() - calibration_start_time;
    uint8_t progress = (uint8_t)((elapsed * 100) / calibration_duration_ms);
    
    return min((uint8_t)100, progress);
}

void Calibration::processReading(float sensor_voltage) {
    if (!in_progress) return;
    
    uint32_t elapsed = millis() - calibration_start_time;
    
    if (elapsed >= calibration_duration_ms) {
        in_progress = false;
        return;
    }
    
    updateEstimate(sensor_voltage);
}

bool Calibration::finalize() {
    if (sample_count == 0) {
        Serial.println("[CALIB] No samples collected!");
        return false;
    }
    
    // Calculate final baseline
    float final_baseline = sum_readings / sample_count;
    
    // Calculate noise level (standard deviation)
    float variance = 0.0f;
    float avg = final_baseline;
    for (uint32_t i = 0; i < sample_count; i++) {
        // We don't have history, so estimate from min/max
    }
    
    // Set profile
    profile.baseline_voltage = final_baseline;
    profile.motion_threshold = DEFAULT_THRESHOLD;
    profile.sensitivity_multiplier = DEFAULT_SENSITIVITY;
    profile.confidence_floor = DEFAULT_CONFIDENCE_FLOOR;
    profile.calibration_timestamp = millis();
    profile.is_valid = true;
    
    Serial.printf("[CALIB] Calibration complete: Baseline=%.3fV, Range=[%.3f-%.3f]V\n",
                 final_baseline, min_reading, max_reading);
    
    in_progress = false;
    return true;
}

const Calibration::CalibrationProfile& Calibration::getProfile() const {
    return profile;
}

void Calibration::setProfile(const CalibrationProfile& new_profile) {
    profile = new_profile;
}

bool Calibration::loadFromStorage() {
    // Try to read calibration from SPIFFS
    if (!SPIFFS.exists("/calibration.json")) {
        Serial.println("[CALIB] No calibration file found");
        return false;
    }
    
    File file = SPIFFS.open("/calibration.json", "r");
    if (!file) {
        Serial.println("[CALIB] Failed to open calibration file");
        return false;
    }
    
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, file);
    file.close();
    
    if (error) {
        Serial.printf("[CALIB] JSON parse error: %s\n", error.c_str());
        return false;
    }
    
    profile.baseline_voltage = doc["baseline"];
    profile.motion_threshold = doc["threshold"];
    profile.sensitivity_multiplier = doc["sensitivity"];
    profile.confidence_floor = doc["confidence_floor"];
    profile.calibration_timestamp = doc["timestamp"];
    profile.is_valid = true;
    
    Serial.printf("[CALIB] Loaded: Baseline=%.3fV, Threshold=%.3fV\n",
                 profile.baseline_voltage, profile.motion_threshold);
    
    return true;
}

bool Calibration::saveToStorage() {
    if (!profile.is_valid) {
        Serial.println("[CALIB] Cannot save invalid calibration");
        return false;
    }
    
    StaticJsonDocument<256> doc;
    doc["baseline"] = profile.baseline_voltage;
    doc["threshold"] = profile.motion_threshold;
    doc["sensitivity"] = profile.sensitivity_multiplier;
    doc["confidence_floor"] = profile.confidence_floor;
    doc["timestamp"] = profile.calibration_timestamp;
    
    File file = SPIFFS.open("/calibration.json", "w");
    if (!file) {
        Serial.println("[CALIB] Failed to open file for writing");
        return false;
    }
    
    serializeJson(doc, file);
    file.close();
    
    Serial.println("[CALIB] Calibration saved to storage");
    return true;
}

void Calibration::resetToDefaults() {
    profile = {
        DEFAULT_BASELINE,
        DEFAULT_THRESHOLD,
        DEFAULT_SENSITIVITY,
        DEFAULT_CONFIDENCE_FLOOR,
        0,
        false
    };
}

void Calibration::setMotionThreshold(float threshold) {
    profile.motion_threshold = threshold;
}

void Calibration::setSensitivityMultiplier(float multiplier) {
    profile.sensitivity_multiplier = multiplier;
}

void Calibration::setConfidenceFloor(uint8_t floor) {
    profile.confidence_floor = floor;
}

void Calibration::updateEstimate(float reading) {
    // Track min/max
    if (reading < min_reading) min_reading = reading;
    if (reading > max_reading) max_reading = reading;
    
    // Accumulate for average
    sum_readings += reading;
    sample_count++;
    
    // Call progress callback
    uint8_t progress = getProgress();
    if (on_progress && progress % 10 == 0) {  // Report every 10%
        float running_avg = sum_readings / sample_count;
        on_progress(progress, running_avg);
    }
}
