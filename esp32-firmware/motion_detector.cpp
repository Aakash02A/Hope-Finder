/**
 * Motion Detector Implementation
 */

#include "motion_detector.h"

MotionDetector::MotionDetector()
    : motion_threshold(0.5f),
      sensitivity_multiplier(1.0f),
      confidence_floor(40),
      current_baseline(0.5f),
      event_start_time(0),
      event_peak_signal(0.0f),
      total_detections(0),
      last_peak_time(0) {
}

bool MotionDetector::begin(float baseline_volts, float motion_threshold_volts) {
    current_baseline = baseline_volts;
    motion_threshold = motion_threshold_volts;
    return true;
}

MotionDetector::Detection MotionDetector::processReading(
    float filtered_signal,
    float baseline,
    uint8_t sector,
    uint16_t angle) {
    
    current_baseline = baseline;
    
    // Calculate signal deviation
    float signal_delta = filtered_signal - baseline;
    float abs_delta = fabs(signal_delta);
    
    // Normalize signal (0-1 scale)
    float signal_strength = min(1.0f, abs_delta / motion_threshold);
    
    // Add to window for analysis
    signal_window.push_back(signal_strength);
    if (signal_window.size() > MAX_WINDOW_SIZE) {
        signal_window.pop_front();
    }
    
    // Analyze for peaks
    analyzeSignalPeaks(signal_strength);
    
    // Calculate consistency (how steady the signal is)
    float consistency = 0.0f;
    if (!signal_window.empty()) {
        float avg_strength = 0.0f;
        for (float s : signal_window) {
            avg_strength += s;
        }
        avg_strength /= signal_window.size();
        
        float variance = 0.0f;
        for (float s : signal_window) {
            variance += (s - avg_strength) * (s - avg_strength);
        }
        variance /= signal_window.size();
        
        // Consistency: high for steady signals
        consistency = max(0.0f, 1.0f - (variance * 2.0f));
    }
    
    // Calculate confidence
    uint8_t confidence = calculateConfidence(signal_strength, consistency);
    
    // Classify motion
    MotionLevel motion_level = classifyMotion(confidence);
    
    // Determine human presence
    bool human_possible = isPossibleHumanPresence(signal_strength, (uint8_t)(consistency * 100));
    
    Detection detection = {
        (uint32_t)millis(),
        sector,
        angle,
        filtered_signal,
        filtered_signal,
        baseline,
        signal_strength,
        motion_level,
        confidence,
        human_possible
    };
    
    if (confidence > confidence_floor) {
        total_detections++;
    }
    
    return detection;
}

void MotionDetector::updateBaseline(float new_baseline) {
    current_baseline = 0.95f * current_baseline + 0.05f * new_baseline;
}

void MotionDetector::setMotionThreshold(float threshold) {
    motion_threshold = threshold;
}

void MotionDetector::setSensitivityMultiplier(float multiplier) {
    sensitivity_multiplier = multiplier;
}

void MotionDetector::setConfidenceFloor(uint8_t floor) {
    confidence_floor = floor;
}

MotionDetector::MotionStats MotionDetector::getRecentStats(uint32_t time_window_ms) {
    MotionStats stats = {0.0f, 0.0f, 0, 0, 0.0f};
    
    if (!signal_window.empty()) {
        float sum = 0.0f;
        float max_signal = 0.0f;
        
        for (float s : signal_window) {
            sum += s;
            if (s > max_signal) max_signal = s;
        }
        
        stats.average_signal = sum / signal_window.size();
        stats.peak_signal = max_signal;
        stats.peak_count = peak_times.size();
        
        // Calculate consistency
        float variance = 0.0f;
        for (float s : signal_window) {
            variance += (s - stats.average_signal) * (s - stats.average_signal);
        }
        variance /= signal_window.size();
        stats.consistency = max(0.0f, 1.0f - (variance * 2.0f));
    }
    
    return stats;
}

bool MotionDetector::isPossibleHumanPresence(float signal_strength, uint8_t consistency) {
    // Heuristic: possible human if signal strength is moderate-to-high
    // AND consistency shows repeated peaks (typical of breathing/movement)
    
    // Humans typically generate signals in 0.5-1.0 range
    bool strong_enough = signal_strength > 0.3f;
    
    // Movement pattern should be somewhat consistent
    bool pattern_good = consistency > 40;
    
    // Peak count should indicate repeated motion
    bool repeated_motion = peak_times.size() > 2 ||
                          (millis() - last_peak_time < 5000);
    
    return strong_enough && pattern_good && repeated_motion;
}

void MotionDetector::resetEventWindow() {
    signal_window.clear();
    peak_times.clear();
    event_start_time = millis();
    event_peak_signal = 0.0f;
}

MotionDetector::Diagnostics MotionDetector::getDiagnostics() const {
    float recent_peak = 0.0f;
    if (!signal_window.empty()) {
        for (float s : signal_window) {
            if (s > recent_peak) recent_peak = s;
        }
    }
    
    return {
        total_detections,
        motion_threshold,
        sensitivity_multiplier,
        (uint16_t)signal_window.size(),
        recent_peak
    };
}

// Private methods

void MotionDetector::analyzeSignalPeaks(float signal) {
    // Simple peak detection: look for local maxima
    if (signal_window.size() < 2) return;
    
    float prev_signal = signal_window.size() > 1 ? 
                       signal_window[signal_window.size() - 2] : 0.0f;
    
    // If current is higher than previous and above threshold, mark as peak
    if (signal > prev_signal && signal > motion_threshold / 2) {
        uint32_t now = millis();
        
        // Only count peaks that are separated by at least 100ms
        if (now - last_peak_time > 100) {
            peak_times.push_back(now);
            event_peak_signal = max(event_peak_signal, signal);
            last_peak_time = now;
            
            // Limit peak history
            while (peak_times.size() > 20) {
                peak_times.pop_front();
            }
        }
    }
    
    // Clean up old peaks
    uint32_t cutoff_time = millis() - 5000;  // 5 second window
    while (!peak_times.empty() && peak_times.front() < cutoff_time) {
        peak_times.pop_front();
    }
}

uint8_t MotionDetector::calculateConfidence(float signal_strength, float consistency) {
    // Apply sensitivity multiplier
    float adjusted_strength = signal_strength * sensitivity_multiplier;
    
    // Base confidence from signal strength (0-70%)
    float strength_confidence = min(70.0f, adjusted_strength * 70.0f);
    
    // Boost from consistency (0-30%)
    float consistency_confidence = consistency * 30.0f;
    
    // Total confidence
    float total_confidence = strength_confidence + consistency_confidence;
    
    // Peaks add credibility
    if (peak_times.size() > 2) {
        total_confidence += 5.0f;
    }
    
    return min(100, (uint8_t)total_confidence);
}

MotionDetector::MotionLevel MotionDetector::classifyMotion(uint8_t confidence) {
    if (confidence < 16) return MOTION_NONE;
    if (confidence < 41) return MOTION_WEAK;
    if (confidence < 76) return MOTION_POSSIBLE_HUMAN;
    return MOTION_STRONG;
}
