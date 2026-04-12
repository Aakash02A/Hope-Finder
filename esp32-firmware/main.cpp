#include <Arduino.h>
#include <SPIFFS.h>
#include <ArduinoJson.h>
#include <WebServer.h>
#include "hb100_sensor.h"
#include "motion_detector.h"
#include "sector_scanner.h"
#include "wifi_client.h"
#include "display_manager.h"
#include "calibration.h"

// ============================================================================
// CONFIGURATION
// ============================================================================
#define WIFI_SSID "RescueNet-5G"
#define WIFI_PASSWORD "changeme123"

WebServer server(80);

// ============================================================================
// GLOBAL OBJECTS & STATE
// ============================================================================
HB100Sensor radar_sensor;
MotionDetector motion_detector;
SectorScanner sector_scanner;
WiFiClient wifi_client;
DisplayManager display;
Calibration calibration;

enum SystemState { STATE_INIT, STATE_IDLE, STATE_SCANNING, STATE_ERROR };
SystemState current_state = STATE_INIT;

struct DetectionRecord {
    uint32_t timestamp;
    uint8_t sector;
    uint16_t angle;
    uint8_t confidence;
};

// Circular buffer for app polling
DetectionRecord detections[50];
int head = 0;

// ============================================================================
// API HANDLERS
// ============================================================================

void handleStatus() {
    StaticJsonDocument<512> doc;
    doc["status"] = "success";
    JsonObject data = doc.createNestedObject("data");
    data["is_scanning"] = (current_state == STATE_SCANNING);
    data["ip"] = WiFi.localIP().toString();
    data["uptime"] = millis() / 1000;
    
    String response;
    serializeJson(doc, response);
    server.send(200, "application/json", response);
}

void handleStartScan() {
    current_state = STATE_SCANNING;
    sector_scanner.startScan(3000); // 3s per sector
    server.send(200, "application/json", "{\"status\":\"success\", \"message\":\"Scanning started\"}");
}

void handleStopScan() {
    current_state = STATE_IDLE;
    sector_scanner.stopScan();
    server.send(200, "application/json", "{\"status\":\"success\", \"message\":\"Scanning stopped\"}");
}

void handleEvents() {
    StaticJsonDocument<2048> doc;
    doc["status"] = "success";
    JsonArray events = doc.createNestedArray("events");
    
    // Return all events in buffer
    for(int i = 0; i < 50; i++) {
        if(detections[i].timestamp > 0) {
            JsonObject obj = events.createNestedObject();
            obj["timestamp"] = detections[i].timestamp;
            obj["sector"] = detections[i].sector;
            obj["angle"] = detections[i].angle;
            obj["confidence"] = detections[i].confidence;
        }
    }
    
    // Clear buffer after reading
    memset(detections, 0, sizeof(detections));
    head = 0;

    String response;
    serializeJson(doc, response);
    server.send(200, "application/json", response);
}

void setup() {
    Serial.begin(115200);
    SPIFFS.begin(true);
    
    radar_sensor.begin();
    sector_scanner.begin(3000, true);
    display.begin();
    
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
    
    display.showStatus("WIFI", 0, "OK", 0, true, true);
    
    // Register API routes
    server.on("/api/v1/device/status", HTTP_GET, handleStatus);
    server.on("/api/v1/scan/start", HTTP_POST, handleStartScan);
    server.on("/api/v1/scan/stop", HTTP_POST, handleStopScan);
    server.on("/api/v1/events", HTTP_GET, handleEvents);
    server.on("/api/v1/health", HTTP_GET, [](){ server.send(200, "application/json", "{\"healthy\":true}"); });
    
    server.begin();
    current_state = STATE_IDLE;
}

void loop() {
    server.handleClient();
    
    if (current_state == STATE_SCANNING) {
        sector_scanner.update();
        HB100Sensor::SensorReading reading = radar_sensor.readSensor();
        
        MotionDetector::Detection det = motion_detector.processReading(
            reading.filtered_voltage, 
            reading.baseline_voltage,
            sector_scanner.getCurrentSector(),
            sector_scanner.getCurrentAngle()
        );

        if (det.confidence > 20) {
            detections[head] = {millis(), det.sector, det.angle, det.confidence};
            head = (head + 1) % 50;
        }
    }
    
    delay(10);
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
