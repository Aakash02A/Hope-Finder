/**
 * ESP32 Master Probe Firmware
 * Radar-Based Living Person Detection System
 * 
 * Main application controller and state machine
 */

#include <Arduino.h>
#include <SPIFFS.h>
#include <ArduinoJson.h>
#include "hb100_sensor.h"
#include "motion_detector.h"
#include "sector_scanner.h"
#include "wifi_client.h"
#include "display_manager.h"
#include "calibration.h"

// ============================================================================
// COMPILE-TIME CONFIGURATION
// ============================================================================

// Wi-Fi Configuration
#define WIFI_SSID "RescueNet-5G"
#define WIFI_PASSWORD "changeme123"
#define API_SERVER_IP "192.168.1.1"
#define API_SERVER_PORT 80

// Hardware Configuration
#define ADC_PIN 34
#define MOTOR_PWM_PIN 5
#define MOTOR_DIR_PIN 18
#define OLED_SDA_PIN 21
#define OLED_SCL_PIN 22

// Operational Parameters
#define SECTOR_DWELL_MS 3000        // Time to sample each sector
#define BASELINE_REFRESH_INTERVAL_S 3600  // Re-calibrate baseline hourly
#define MOTION_THRESHOLD 0.5        // Default motion detection threshold (volts)
#define API_SEND_INTERVAL_MS 100    // Send detections to API every 100ms
#define DISPLAY_UPDATE_INTERVAL_MS 100
#define HEALTH_CHECK_INTERVAL_MS 5000
#define CALIBRATION_DURATION_MS 60000    // 60 second calibration

// ============================================================================
// GLOBAL OBJECTS
// ============================================================================

HB100Sensor radar_sensor;
MotionDetector motion_detector;
SectorScanner sector_scanner;
WiFiClient wifi_client;
DisplayManager display;
Calibration calibration;

// ============================================================================
// SYSTEM STATE MACHINE
// ============================================================================

enum SystemState {
    STATE_STARTUP,
    STATE_SELF_CHECK,
    STATE_CALIBRATION_WAITING,
    STATE_CALIBRATING,
    STATE_IDLE,
    STATE_SCANNING,
    STATE_ERROR
};

SystemState current_state = STATE_STARTUP;
SystemState previous_state = STATE_STARTUP;

// ============================================================================
// GLOBAL STATE VARIABLES
// ============================================================================

struct {
    uint32_t system_uptime_ms;
    uint32_t last_detection_time;
    uint32_t last_api_send_time;
    uint32_t last_display_update_time;
    uint32_t last_health_check_time;
    uint32_t last_baseline_refresh_time;
    
    bool radar_healthy;
    bool wifi_connected;
    bool calibration_valid;
    bool scanning_active;
    
    uint16_t detection_count;
    uint8_t last_confidence;
    uint8_t last_sector;
    
    char error_code[32];
    char error_message[128];
} system_state;

// ============================================================================
// FUNCTION DECLARATIONS
// ============================================================================

void setup();
void loop();
void handleStateTransition();
void updateSensors();
void updateMotionDetection();
void updateDisplay();
void sendDetectionToApi(const MotionDetector::Detection& detection);
void performHealthCheck();
void doCalibration();
void startScanning();
void stopScanning();
void handleError(const char* code, const char* message);
void logEvent(const char* message);
void initializeStorage();

// ============================================================================
// SETUP
// ============================================================================

void setup() {
    // Initialize serial for debugging
    Serial.begin(115200);
    delay(100);
    
    Serial.println("\n\n=== Hope-Finder: Radar Detection System ===");
    Serial.println("Starting Master Probe Firmware v1.0.0");
    
    // Initialize storage
    initializeStorage();
    
    // Initialize hardware
    Serial.println("[SETUP] Initializing HB100 radar sensor...");
    if (!radar_sensor.begin()) {
        handleError("RADAR_INIT_FAIL", "ADC initialization failed");
        return;
    }
    
    Serial.println("[SETUP] Initializing sector scanner...");
    if (!sector_scanner.begin(SECTOR_DWELL_MS, false)) {  // No motor for prototype
        handleError("SCANNER_INIT_FAIL", "Sector scanner initialization failed");
        return;
    }
    
    Serial.println("[SETUP] Initializing OLED display...");
    if (!display.begin()) {
        Serial.println("[WARNING] Display initialization failed (non-critical)");
    }
    
    Serial.println("[SETUP] Initializing Wi-Fi...");
    wifi_client.setApiUrl(API_SERVER_IP ":" API_SERVER_PORT);
    if (!wifi_client.begin(WIFI_SSID, WIFI_PASSWORD, 15000)) {
        handleError("WIFI_INIT_FAIL", "Failed to connect to Wi-Fi");
    }
    
    // Check for existing calibration
    Serial.println("[SETUP] Loading calibration profile...");
    if (!calibration.loadFromStorage()) {
        Serial.println("[INFO] No calibration found, waiting for user to start calibration");
        system_state.calibration_valid = false;
    } else {
        Serial.println("[INFO] Calibration loaded successfully");
        system_state.calibration_valid = true;
        motion_detector.begin(
            calibration.getProfile().baseline_voltage,
            calibration.getProfile().motion_threshold
        );
    }
    
    // Initialize motion detector with defaults
    motion_detector.begin(0.5f, MOTION_THRESHOLD);
    
    // Start state machine
    current_state = STATE_SELF_CHECK;
    Serial.println("[SETUP] Initialization complete, entering self-check...");
}

// ============================================================================
// MAIN LOOP
// ============================================================================

void loop() {
    // Update system uptime
    system_state.system_uptime_ms = millis();
    
    // State machine
    handleStateTransition();
    
    // Update components
    wifi_client.update();
    
    if (sector_scanner.isScanning()) {
        sector_scanner.update();
        updateSensors();
        updateMotionDetection();
    }
    
    // Periodic updates
    if (millis() - system_state.last_display_update_time > DISPLAY_UPDATE_INTERVAL_MS) {
        updateDisplay();
        system_state.last_display_update_time = millis();
    }
    
    if (millis() - system_state.last_health_check_time > HEALTH_CHECK_INTERVAL_MS) {
        performHealthCheck();
        system_state.last_health_check_time = millis();
    }
    
    // Check for baseline refresh
    if (millis() - system_state.last_baseline_refresh_time > 
        (BASELINE_REFRESH_INTERVAL_S * 1000)) {
        Serial.println("[INFO] Refreshing baseline estimation...");
        radar_sensor.resetBaseline();
        system_state.last_baseline_refresh_time = millis();
    }
    
    // Small delay to prevent watchdog trigger
    delay(5);
}

// ============================================================================
// STATE MACHINE HANDLER
// ============================================================================

void handleStateTransition() {
    // Check for state changes
    if (current_state != previous_state) {
        Serial.printf("[STATE] Transitioning from %d to %d\n", previous_state, current_state);
        
        switch (current_state) {
            case STATE_STARTUP:
                // Brief initialization state
                display.showStatus("--", 0, "Init", 0.0f, false, false);
                current_state = STATE_SELF_CHECK;
                break;
                
            case STATE_SELF_CHECK:
                // Verify hardware and connectivity
                performHealthCheck();
                if (system_state.radar_healthy && wifi_client.isConnected()) {
                    if (system_state.calibration_valid) {
                        current_state = STATE_SCANNING;
                    } else {
                        current_state = STATE_CALIBRATION_WAITING;
                    }
                } else {
                    current_state = STATE_ERROR;
                }
                break;
                
            case STATE_CALIBRATION_WAITING:
                // Waiting for user to trigger calibration
                Serial.println("[CALIB] Waiting for calibration trigger...");
                display.showStatus("CAL", 0, "Wait", 0.0f, 
                                 system_state.wifi_connected, false);
                break;
                
            case STATE_CALIBRATING:
                // Running calibration
                doCalibration();
                if (calibration.finalize()) {
                    motion_detector.begin(
                        calibration.getProfile().baseline_voltage,
                        calibration.getProfile().motion_threshold
                    );
                    system_state.calibration_valid = true;
                    current_state = STATE_SCANNING;
                }
                break;
                
            case STATE_SCANNING:
                // Normal operation
                Serial.println("[SCAN] Starting continuous scanning...");
                startScanning();
                break;
                
            case STATE_IDLE:
                // Paused but ready
                stopScanning();
                display.showStatus("--", 0, "Ready", 0.0f, 
                                 system_state.wifi_connected, false);
                break;
                
            case STATE_ERROR:
                Serial.printf("[ERROR] %s: %s\n", system_state.error_code, 
                            system_state.error_message);
                display.showError(system_state.error_code, system_state.error_message);
                break;
        }
        
        previous_state = current_state;
    }
    
    // Handle state-specific operations
    switch (current_state) {
        case STATE_SCANNING:
            // Already handled in main loop
            break;
            
        case STATE_CALIBRATING:
            if (calibration.isInProgress()) {
                // Continue calibration
                HB100Sensor::SensorReading reading = radar_sensor.readSensor();
                calibration.processReading(reading.raw_voltage);
                
                uint8_t progress = calibration.getProgress();
                display.showCalibration(progress, calibration.getProfile().baseline_voltage);
                
                if (progress >= 100) {
                    current_state = STATE_SCANNING;
                }
            }
            break;
            
        case STATE_ERROR:
            // Stay in error state until cleared
            if (millis() % 2000 < 1000) {
                display.showError(system_state.error_code, system_state.error_message);
            }
            break;
            
        default:
            break;
    }
}

// ============================================================================
// SENSOR UPDATE
// ============================================================================

void updateSensors() {
    HB100Sensor::SensorReading reading = radar_sensor.readSensor();
    
    // Update motion detector with latest reading
    // (Detection object returned but not used here; used in motion detection handler)
}

// ============================================================================
// MOTION DETECTION UPDATE
// ============================================================================

void updateMotionDetection() {
    HB100Sensor::SensorReading reading = radar_sensor.readSensor();
    
    uint8_t current_sector = sector_scanner.getCurrentSector();
    uint16_t current_angle = sector_scanner.getCurrentAngle();
    
    // Process reading
    MotionDetector::Detection detection = motion_detector.processReading(
        reading.filtered_voltage,
        reading.baseline_voltage,
        current_sector,
        current_angle
    );
    
    // Check if confidence meets floor
    if (detection.confidence >= motion_detector.getConfidenceFloor()) {
        system_state.last_detection_time = millis();
        system_state.detection_count++;
        system_state.last_confidence = detection.confidence;
        system_state.last_sector = detection.sector;
        
        // Send to mobile app
        if (millis() - system_state.last_api_send_time > API_SEND_INTERVAL_MS) {
            sendDetectionToApi(detection);
            system_state.last_api_send_time = millis();
        }
        
        // Log to console
        Serial.printf("[DETECT] Sector %d (%s), Angle %d°, Confidence %d%%, "
                     "Signal %.2fV, Motion: %s\n",
                     detection.sector,
                     sector_scanner.getSectorLabel(detection.sector),
                     detection.angle,
                     detection.confidence,
                     detection.filtered_signal,
                     detection.human_presence_possible ? "POSSIBLE_HUMAN" : "MOTION");
    }
}

// ============================================================================
// DISPLAY UPDATE
// ============================================================================

void updateDisplay() {
    if (!display.isHealthy()) {
        return;
    }
    
    if (current_state == STATE_SCANNING && sector_scanner.isScanning()) {
        display.showScanning(
            sector_scanner.getCurrentSector(),
            sector_scanner.getCurrentAngle(),
            system_state.last_confidence
        );
    } else if (current_state == STATE_CALIBRATION_WAITING) {
        display.showStatus("CAL", 0, "Ready", 0.0f, 
                         system_state.wifi_connected, false);
    } else {
        // Status screen
        HB100Sensor::SensorReading reading = radar_sensor.readSensor();
        display.showStatus(
            sector_scanner.getSectorLabel(sector_scanner.getCurrentSector()),
            sector_scanner.getCurrentAngle(),
            "Ready",
            reading.signal_strength,
            system_state.wifi_connected,
            true
        );
    }
    
    display.update();
}

// ============================================================================
// API TRANSMISSION
// ============================================================================

void sendDetectionToApi(const MotionDetector::Detection& detection) {
    if (!wifi_client.isConnected()) {
        return;
    }
    
    // Build JSON payload
    StaticJsonDocument<512> doc;
    doc["device_id"] = "master_probe_01";
    doc["event_id"] = String("evt_") + String(millis()) + "_" + String(detection.sector);
    doc["timestamp"] = String("2026-03-29T10:20:30Z");  // TODO: Get real timestamp
    doc["sector"] = detection.sector;
    doc["sector_label"] = sector_scanner.getSectorLabel(detection.sector);
    doc["angle"] = detection.angle;
    doc["raw_signal"] = detection.raw_signal;
    doc["filtered_signal"] = detection.filtered_signal;
    doc["baseline"] = detection.baseline;
    doc["signal_delta"] = detection.filtered_signal - detection.baseline;
    doc["signal_strength"] = detection.signal_strength;
    doc["motion_level"] = detection.motion_level;
    
    const char* motion_labels[] = {"No Motion", "Weak Motion", 
                                   "Possible Human Movement", "Strong Movement"};
    doc["motion_label"] = motion_labels[detection.motion_level];
    
    doc["confidence"] = detection.confidence;
    
    const char* confidence_levels[] = {"NONE", "LOW", "MEDIUM", "HIGH"};
    doc["confidence_level"] = confidence_levels[detection.confidence > 75 ? 3 :
                                               (detection.confidence > 40 ? 2 :
                                                (detection.confidence > 15 ? 1 : 0))];
    
    doc["human_presence_possible"] = detection.human_presence_possible;
    doc["wifi_rssi"] = wifi_client.getConnection().rssi;
    doc["scan_duration_ms"] = SECTOR_DWELL_MS;
    
    // Serialize and send
    char json_buffer[512];
    serializeJson(doc, json_buffer, sizeof(json_buffer));
    
    wifi_client.sendDetectionEvent(json_buffer);
}

// ============================================================================
// HEALTH CHECK
// ============================================================================

void performHealthCheck() {
    HB100Sensor::SensorReading reading = radar_sensor.readSensor();
    system_state.radar_healthy = reading.healthy;
    system_state.wifi_connected = wifi_client.isConnected();
    
    if (!system_state.radar_healthy) {
        handleError("RADAR_ERROR", "Radar signal unhealthy");
    }
    
    if (!system_state.wifi_connected) {
        Serial.println("[WARNING] Wi-Fi disconnected, attempting reconnect...");
        wifi_client.connect();
    }
    
    Serial.printf("[HEALTH] Radar: %s, Wi-Fi: %s, Calibrated: %s, Scanning: %s\n",
                 system_state.radar_healthy ? "OK" : "FAIL",
                 system_state.wifi_connected ? "OK" : "FAIL",
                 system_state.calibration_valid ? "YES" : "NO",
                 sector_scanner.isScanning() ? "YES" : "NO");
}

// ============================================================================
// CALIBRATION
// ============================================================================

void doCalibration() {
    Serial.println("[CALIB] Starting 60-second calibration...");
    calibration.begin(CALIBRATION_DURATION_MS);
    
    while (calibration.isInProgress()) {
        HB100Sensor::SensorReading reading = radar_sensor.readSensor();
        calibration.processReading(reading.raw_voltage);
        
        uint8_t progress = calibration.getProgress();
        Serial.printf("[CALIB] Progress: %d%%, Baseline: %.2fV\n", 
                     progress, calibration.getProfile().baseline_voltage);
        
        display.showCalibration(progress, calibration.getProfile().baseline_voltage);
        delay(100);
    }
    
    calibration.finalize();
    calibration.saveToStorage();
    Serial.println("[CALIB] Calibration complete!");
}

// ============================================================================
// SCANNING CONTROL
// ============================================================================

void startScanning() {
    sector_scanner.startScan(36000);  // Full rotation: 12 sectors * 3s = 36s
    system_state.scanning_active = true;
    Serial.println("[SCAN] Scanning started");
}

void stopScanning() {
    sector_scanner.stopScan();
    system_state.scanning_active = false;
    Serial.println("[SCAN] Scanning stopped");
}

// ============================================================================
// ERROR HANDLING
// ============================================================================

void handleError(const char* code, const char* message) {
    strncpy(system_state.error_code, code, sizeof(system_state.error_code) - 1);
    strncpy(system_state.error_message, message, sizeof(system_state.error_message) - 1);
    current_state = STATE_ERROR;
    Serial.printf("[ERROR] %s: %s\n", code, message);
}

// ============================================================================
// LOGGING
// ============================================================================

void logEvent(const char* message) {
    Serial.printf("[LOG] %s\n", message);
    // TODO: Write to SPIFFS event log
}

// ============================================================================
// STORAGE INITIALIZATION
// ============================================================================

void initializeStorage() {
    if (!SPIFFS.begin(true)) {
        Serial.println("[ERROR] SPIFFS initialization failed!");
        return;
    }
    Serial.println("[STORAGE] SPIFFS initialized");
}
