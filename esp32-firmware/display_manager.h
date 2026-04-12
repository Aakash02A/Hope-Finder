/**
 * OLED Display Manager
 * Manages SSD1306 OLED display (128x64)
 */

#ifndef DISPLAY_MANAGER_H
#define DISPLAY_MANAGER_H

#include <Arduino.h>

class DisplayManager {
public:
    enum DisplayMode {
        MODE_STATUS,
        MODE_SCANNING,
        MODE_CALIBRATION,
        MODE_ERROR,
        MODE_ALERT
    };

    struct DisplayState {
        uint8_t brightness;               // 0-255
        uint32_t last_update;
        DisplayMode current_mode;
    };

    DisplayManager();

    /**
     * Initialize I2C OLED display (SSD1306)
     */
    bool begin();

    /**
     * Display home/status screen
     */
    void showStatus(const char* sector_label, uint16_t angle, 
                   const char* motion_level, float signal_strength,
                   bool wifi_connected, bool api_ok);

    /**
     * Display scanning animation
     */
    void showScanning(uint8_t sector, uint16_t angle, uint8_t confidence);

    /**
     * Display calibration progress
     */
    void showCalibration(uint8_t progress_percent, float baseline);

    /**
     * Display error message
     */
    void showError(const char* error_code, const char* message);

    /**
     * Display alert
     */
    void showAlert(uint8_t confidence, const char* sector_label, uint16_t angle);

    /**
     * Clear display
     */
    void clear();

    /**
     * Set brightness (0-255)
     */
    void setBrightness(uint8_t brightness);

    /**
     * Update display (refresh)
     */
    void update();

    /**
     * Check if display is healthy
     */
    bool isHealthy() const { return display_healthy; }

private:
    void drawPixel(uint8_t x, uint8_t y, bool on);
    void drawLine(uint8_t x1, uint8_t y1, uint8_t x2, uint8_t y2);
    void drawCircle(uint8_t x, uint8_t y, uint8_t r);
    void drawRectangle(uint8_t x, uint8_t y, uint8_t w, uint8_t h);
    void drawText(uint8_t x, uint8_t y, const char* text, bool large = false);
    void flush();

    DisplayState state;
    uint8_t framebuffer[128 * 64 / 8];  // 1024 bytes for 128x64 1-bit display
    bool display_healthy;
    uint32_t last_error_display_time;
    
    static const uint32_t UPDATE_INTERVAL_MS = 100;
    static const uint8_t I2C_ADDR = 0x3C;  // SSD1306 default address
};

#endif // DISPLAY_MANAGER_H
