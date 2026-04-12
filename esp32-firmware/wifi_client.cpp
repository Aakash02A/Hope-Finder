/**
 * Wi-Fi Client Implementation
 */

#include "wifi_client.h"
#include <HTTPClient.h>
#include <ArduinoJson.h>

WiFiClient::WiFiClient()
    : ssid_configured(nullptr),
      password_configured(nullptr),
      api_base_url("http://192.168.1.1:80/api/v1"),
      on_connected(nullptr),
      on_disconnected(nullptr),
      last_connection_attempt(0),
      last_rssi_check(0),
      http_requests_count(0),
      http_errors_count(0),
      connection_attempts_count(0),
      auto_reconnect(true) {
    
    current_connection = {
        false,  // connected
        0,      // rssi
        "",     // ssid
        "",     // ip
        0       // connect_time
    };
}

bool WiFiClient::begin(const char* ssid, const char* password, uint32_t timeout_ms) {
    ssid_configured = ssid;
    password_configured = password;
    
    return connect(timeout_ms);
}

bool WiFiClient::connect(uint32_t timeout_ms) {
    if (isConnected()) {
        return true;
    }
    
    connection_attempts_count++;
    Serial.printf("[WiFi] Connecting to %s...\n", ssid_configured);
    
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid_configured, password_configured);
    
    uint32_t start_time = millis();
    
    while (!isConnected() && (millis() - start_time) < timeout_ms) {
        delay(500);
        Serial.print(".");
    }
    
    Serial.println();
    
    if (isConnected()) {
        current_connection.connected = true;
        current_connection.connect_time = millis();
        current_connection.ssid = WiFi.SSID().c_str();
        current_connection.ip = WiFi.localIP().toString().c_str();
        
        Serial.printf("[WiFi] Connected! IP: %s\n", current_connection.ip);
        
        if (on_connected) {
            on_connected();
        }
        
        return true;
    } else {
        Serial.println("[WiFi] Connection failed");
        current_connection.connected = false;
        
        if (on_disconnected) {
            on_disconnected();
        }
        
        return false;
    }
}

bool WiFiClient::isConnected() const {
    return WiFi.status() == WL_CONNECTED;
}

bool WiFiClient::sendDetectionEvent(const char* json_payload) {
    if (!isConnected()) {
        http_errors_count++;
        return false;
    }
    
    HTTPClient http;
    char url[256];
    snprintf(url, sizeof(url), "%s/detection", api_base_url);
    
    http.begin(url);
    http.addHeader("Content-Type", "application/json");
    
    int http_code = http.POST((uint8_t*)json_payload, strlen(json_payload));
    
    if (http_code == 200) {
        http_requests_count++;
        http.end();
        return true;
    } else {
        http_errors_count++;
        Serial.printf("[HTTP] POST Error: %d\n", http_code);
        http.end();
        return false;
    }
}

bool WiFiClient::getRequest(const char* endpoint, char* response_buffer, size_t buffer_size) {
    if (!isConnected()) {
        return false;
    }
    
    HTTPClient http;
    char url[256];
    snprintf(url, sizeof(url), "%s%s", api_base_url, endpoint);
    
    http.begin(url);
    
    int http_code = http.GET();
    
    if (http_code == 200) {
        String payload = http.getString();
        strncpy(response_buffer, payload.c_str(), buffer_size - 1);
        http_requests_count++;
        http.end();
        return true;
    } else {
        http_errors_count++;
        http.end();
        return false;
    }
}

void WiFiClient::update() {
    // Check and update RSSI
    if (millis() - last_rssi_check > RSSI_UPDATE_INTERVAL_MS) {
        updateSignalStrength();
        last_rssi_check = millis();
    }
    
    // Auto-reconnect if disconnected
    if (auto_reconnect && !isConnected() && 
        (millis() - last_connection_attempt) > RECONNECT_INTERVAL_MS) {
        Serial.println("[WiFi] Attempting reconnection...");
        connect(5000);
        last_connection_attempt = millis();
    }
}

void WiFiClient::disconnect() {
    WiFi.disconnect(true);  // Turn off Wi-Fi
    current_connection.connected = false;
    
    if (on_disconnected) {
        on_disconnected();
    }
}

WiFiClient::Diagnostics WiFiClient::getDiagnostics() const {
    return {
        connection_attempts_count,
        http_requests_count,
        http_errors_count,
        last_rssi_check,
        auto_reconnect
    };
}

void WiFiClient::handleConnectionEvent() {
    // Called when Wi-Fi state changes
}

void WiFiClient::updateSignalStrength() {
    if (isConnected()) {
        current_connection.rssi = WiFi.RSSI();
    }
}
