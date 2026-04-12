/**
 * Wi-Fi and HTTP Client
 * Manages Wi-Fi connection and real-time HTTP transmission
 */

#ifndef WIFI_CLIENT_H
#define WIFI_CLIENT_H

#include <Arduino.h>
#include <WiFi.h>

class WiFiClient {
public:
    struct Connection {
        bool connected;
        int16_t rssi;                // Signal strength (-127 to 0)
        const char* ssid;
        const char* ip;
        uint32_t connect_time;
    };

    typedef void (*OnConnected)(void);
    typedef void (*OnDisconnected)(void);

    WiFiClient();

    /**
     * Initialize Wi-Fi with credentials
     */
    bool begin(const char* ssid, const char* password, 
               uint32_t timeout_ms = 15000);

    /**
     * Connect to stored SSID
     */
    bool connect(uint32_t timeout_ms = 15000);

    /**
     * Check connection status
     */
    bool isConnected() const;

    /**
     * Get connection info
     */
    const Connection& getConnection() const { return current_connection; }

    /**
     * Send detection event via HTTP POST
     */
    bool sendDetectionEvent(const char* json_payload);

    /**
     * GET request to retrieve data
     */
    bool getRequest(const char* endpoint, char* response_buffer, size_t buffer_size);

    /**
     * Set callback for connection event
     */
    void setOnConnected(OnConnected callback) { on_connected = callback; }

    /**
     * Set callback for disconnection event
     */
    void setOnDisconnected(OnDisconnected callback) { on_disconnected = callback; }

    /**
     * Update connection status (call regularly)
     */
    void update();

    /**
     * Disconnect from Wi-Fi
     */
    void disconnect();

    /**
     * Check and report errors
     */
    struct Diagnostics {
        uint32_t connection_attempts;
        uint32_t http_requests_sent;
        uint32_t http_errors;
        uint32_t last_rssi_update;
        bool auto_reconnect_enabled;
    } getDiagnostics() const;

    /**
     * Set API base URL (IP:port)
     */
    void setApiUrl(const char* url) { api_base_url = url; }

private:
    void handleConnectionEvent();
    void updateSignalStrength();

    const char* ssid_configured;
    const char* password_configured;
    const char* api_base_url;
    Connection current_connection;
    
    OnConnected on_connected;
    OnDisconnected on_disconnected;
    
    uint32_t last_connection_attempt;
    uint32_t last_rssi_check;
    uint32_t http_requests_count;
    uint32_t http_errors_count;
    uint32_t connection_attempts_count;
    
    bool auto_reconnect;
    static const uint32_t RSSI_UPDATE_INTERVAL_MS = 5000;
    static const uint32_t RECONNECT_INTERVAL_MS = 10000;
};

#endif // WIFI_CLIENT_H
