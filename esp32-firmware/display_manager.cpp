/**
 * Display Manager Implementation
 */

#include "display_manager.h"
#include <Wire.h>
#include <Adafruit_SSD1306.h>

static Adafruit_SSD1306 display_device(128, 64, &Wire, -1);

DisplayManager::DisplayManager()
    : display_healthy(false),
      last_error_display_time(0) {
    
    state = {
        255,           // brightness
        0,             // last_update
        MODE_STATUS    // current_mode
    };
    
    memset(framebuffer, 0, sizeof(framebuffer));
}

bool DisplayManager::begin() {
    // Initialize I2C (GPIO21=SDA, GPIO22=SCL)
    Wire.begin(21, 22);
    
    // Initialize SSD1306 display
    if (!display_device.begin(SSD1306_SWITCHCAPVCC, I2C_ADDR)) {
        Serial.println("[Display] SSD1306 initialization failed");
        display_healthy = false;
        return false;
    }
    
    display_device.clearDisplay();
    display_device.setTextSize(1);
    display_device.setTextColor(SSD1306_WHITE);
    display_device.setCursor(0, 0);
    display_device.println("Hope-Finder v1.0");
    display_device.display();
    
    display_healthy = true;
    return true;
}

void DisplayManager::showStatus(const char* sector_label, uint16_t angle,
                               const char* motion_level, float signal_strength,
                               bool wifi_connected, bool api_ok) {
    if (!display_healthy) return;
    
    display_device.clearDisplay();
    display_device.setTextSize(1);
    display_device.setTextColor(SSD1306_WHITE);
    
    // Title
    display_device.setCursor(0, 0);
    display_device.println("RADAR SCAN STATUS");
    
    // Sector info (row 2)
    display_device.setCursor(0, 10);
    display_device.printf("Sector: %s (%d)", sector_label, angle);
    
    // Motion level (row 3)
    display_device.setCursor(0, 20);
    display_device.printf("Motion: %s", motion_level);
    
    // Signal strength (row 4)
    display_device.setCursor(0, 30);
    display_device.printf("Signal: %.2f V", signal_strength);
    
    // Wi-Fi status (row 5)
    display_device.setCursor(0, 40);
    display_device.printf("WiFi: %s", wifi_connected ? "OK" : "--");
    
    // API status (row 6)
    display_device.setCursor(80, 40);
    display_device.printf("API: %s", api_ok ? "OK" : "--");
    
    // Time
    display_device.setCursor(0, 55);
    uint32_t uptime_s = millis() / 1000;
    display_device.printf("Uptime: %02dh%02dm", uptime_s/3600, (uptime_s%3600)/60);
    
    display_device.display();
    state.last_update = millis();
}

void DisplayManager::showScanning(uint8_t sector, uint16_t angle, uint8_t confidence) {
    if (!display_healthy) return;
    
    display_device.clearDisplay();
    display_device.setTextSize(1);
    display_device.setTextColor(SSD1306_WHITE);
    
    // Title
    display_device.setCursor(0, 0);
    display_device.println("SCANNING...");
    
    // Draw simple circular scan indicator
    // Center at (60, 35), radius ~20
    for (int r = 5; r <= 20; r += 5) {
        display_device.drawCircle(60, 35, r, SSD1306_WHITE);
    }
    
    // Draw radial line for current sector
    int angle_rad = angle * 3.14159 / 180;
    int x_end = 60 + (int)(20 * cos(angle_rad));
    int y_end = 35 - (int)(20 * sin(angle_rad));
    display_device.drawLine(60, 35, x_end, y_end, SSD1306_WHITE);
    
    // Sector and angle info
    display_device.setCursor(0, 10);
    display_device.printf("Sector: %d, Angle: %d", sector, angle);
    
    // Confidence
    display_device.setCursor(0, 20);
    display_device.printf("Confidence: %d%%", confidence);
    
    // Detection indicator
    if (confidence > 50) {
        display_device.setCursor(0, 30);
        display_device.println("POSSIBLE MOTION DETECTED!");
    }
    
    display_device.display();
    state.last_update = millis();
}

void DisplayManager::showCalibration(uint8_t progress_percent, float baseline) {
    if (!display_healthy) return;
    
    display_device.clearDisplay();
    display_device.setTextSize(1);
    display_device.setTextColor(SSD1306_WHITE);
    
    // Title
    display_device.setCursor(0, 0);
    display_device.println("CALIBRATION IN PROGRESS");
    
    // Progress bar (horizontal)
    display_device.setCursor(0, 15);
    display_device.println("Progress:");
    
    int bar_width = (progress_percent * 100) / 100;
    if (bar_width > 100) bar_width = 100;
    
    display_device.drawRect(0, 25, 128, 8, SSD1306_WHITE);
    display_device.fillRect(0, 25, bar_width, 8, SSD1306_WHITE);
    
    // Percentage
    display_device.setCursor(50, 38);
    display_device.printf("%d%%", progress_percent);
    
    // Baseline value
    display_device.setCursor(0, 50);
    display_device.printf("Baseline: %.3f V", baseline);
    
    display_device.display();
    state.last_update = millis();
}

void DisplayManager::showError(const char* error_code, const char* message) {
    if (!display_healthy) return;
    
    // Only update every second to avoid flicker
    if (millis() - last_error_display_time < 500) {
        return;
    }
    
    display_device.clearDisplay();
    display_device.setTextSize(1);
    display_device.setTextColor(SSD1306_WHITE);
    
    // Title
    display_device.setCursor(0, 0);
    display_device.println("ERROR!");
    
    // Error code
    display_device.setCursor(0, 15);
    display_device.printf("Code: %s", error_code);
    
    // Error message (wrapped)
    display_device.setCursor(0, 30);
    display_device.println(message);
    
    // Suggestion
    display_device.setCursor(0, 50);
    display_device.println("Check connections");
    
    display_device.display();
    last_error_display_time = millis();
}

void DisplayManager::showAlert(uint8_t confidence, const char* sector_label, uint16_t angle) {
    if (!display_healthy) return;
    
    display_device.clearDisplay();
    display_device.setTextSize(2);
    display_device.setTextColor(SSD1306_WHITE);
    
    // Large alert text
    display_device.setCursor(10, 5);
    display_device.println("ALERT!");
    
    display_device.setTextSize(1);
    
    // Confidence
    display_device.setCursor(0, 25);
    display_device.printf("Confidence: %d%%", confidence);
    
    // Location
    display_device.setCursor(0, 35);
    display_device.printf("Sector: %s", sector_label);
    
    display_device.setCursor(0, 45);
    display_device.printf("Angle: %d degrees", angle);
    
    // Strong indication
    display_device.setCursor(0, 55);
    display_device.println("POSSIBLE HUMAN PRESENCE");
    
    display_device.display();
    state.last_update = millis();
}

void DisplayManager::clear() {
    if (!display_healthy) return;
    
    display_device.clearDisplay();
    display_device.display();
}

void DisplayManager::setBrightness(uint8_t brightness) {
    state.brightness = brightness;
    // SSD1306 brightness control via contrast (0-255)
    display_device.setContrast(brightness);
}

void DisplayManager::update() {
    // Display updates are triggered by specific show*() methods
}

void DisplayManager::drawPixel(uint8_t x, uint8_t y, bool on) {
    if (on) {
        display_device.drawPixel(x, y, SSD1306_WHITE);
    }
}

void DisplayManager::drawLine(uint8_t x1, uint8_t y1, uint8_t x2, uint8_t y2) {
    display_device.drawLine(x1, y1, x2, y2, SSD1306_WHITE);
}

void DisplayManager::drawCircle(uint8_t x, uint8_t y, uint8_t r) {
    display_device.drawCircle(x, y, r, SSD1306_WHITE);
}

void DisplayManager::drawRectangle(uint8_t x, uint8_t y, uint8_t w, uint8_t h) {
    display_device.drawRect(x, y, w, h, SSD1306_WHITE);
}

void DisplayManager::drawText(uint8_t x, uint8_t y, const char* text, bool large) {
    display_device.setCursor(x, y);
    if (large) {
        display_device.setTextSize(2);
    } else {
        display_device.setTextSize(1);
    }
    display_device.println(text);
}

void DisplayManager::flush() {
    display_device.display();
}
