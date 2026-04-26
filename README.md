# Hope-Finder V2

Hope-Finder V2 is an integrated IoT and Mobile Application system designed for search and rescue or presence detection. It utilizes a **Dual Doppler Radar System** powered by an ESP32 microcontroller to detect human life and motion. The accompanying **Android Application** provides a real-time interface, offline-first data logging, and cloud synchronization.

---

## 🎯 System Architecture

The project consists of two main components communicating over a local Wi-Fi network:

1. **Hardware (ESP32 Doppler Radar)**
2. **Software (Android Jetpack Compose App)**

### 1. Hardware (ESP32 & MicroPython)
The hardware acts as the edge computing device and server. It runs MicroPython and interfaces with physical sensors to detect motion.

**Hardware Components:**
*   **ESP32 Microcontroller:** The brain of the hardware.
*   **2x Doppler Radar Sensors:** Connected via Analog-to-Digital Converter (ADC) pins (GPIO 34 & 35) to measure left and right environmental disturbances.
*   **SSD1306 OLED Display:** Connected via I2C (SCL 22, SDA 21) to show live status, distances, and IP information directly on the device.
*   **Status LEDs:** Green (GPIO 25) for Motion Detected, Red (GPIO 26) for Clear/No Motion.

**How the firmware works:**
1.  **Boot & Calibration:** On startup, the ESP32 calibrates the baseline noise of the environment.
2.  **Wi-Fi Access Point:** It spins up its own Wi-Fi network (SSID: `ESP32_RADAR`).
3.  **HTTP Server:** It starts a local web server on port 80.
4.  **Continuous Polling:** In the main loop, it reads the sensors every 200ms, calculates the signal change, estimates the distance (`<1m` to `>4m`), and triggers motion alerts.
5.  **API Endpoints:** It serves this real-time data via HTTP GET requests (e.g., `/data`, `/events`) allowing clients to fetch JSON payloads of the radar state.

### 2. Software (Android Application)
The mobile application acts as the client and control center.

**Tech Stack:**
*   **UI:** Kotlin & Jetpack Compose (Modern declarative UI).
*   **Networking:** OkHttp & Gson for polling the local ESP32 HTTP Server.
*   **Local Storage:** Room Database for offline-first data caching.
*   **Cloud Sync:** Firebase Firestore for long-term synchronization and remote monitoring.

**How the App works:**
1.  **Connection:** The user connects their phone to the `ESP32_RADAR` Wi-Fi network.
2.  **Real-Time Dashboard (`DeviceApiClient`):** The app continuously polls the ESP32's REST API to fetch live distances and motion states.
3.  **Visualization:** Data is displayed in a modern UI built with Jetpack Compose.
4.  **Offline-First & Firebase Integration:** When motion events are detected, they are immediately stored locally in a Room database. If the phone regains internet access, these events are synced to Firebase Firestore so remote command centers can review the historical data.

---

## 📁 Repository Structure

```text
Hope-Finder-V2/
├── app/                        # Android Application Source Code
│   ├── src/main/java/.../ui/   # Jetpack Compose UI Screens & Components
│   ├── src/main/java/.../data/ # Room Database Entities & DAO
│   ├── src/main/java/.../net/  # OkHttp API Client for ESP32
│   └── build.gradle.kts        # Android build configuration
│
└── esp32-drivercode-thonny/    # ESP32 MicroPython Firmware
    ├── main.py                 # Main radar logic, AP setup, & Web Server
    └── ssd1306.py              # OLED Display Driver
```

---

## 🚀 End-to-End Workflow

1.  **Power On:** The ESP32 is powered on. It calibrates its sensors and creates a local Wi-Fi hotspot.
2.  **Connect:** The Android phone connects to the ESP32's Wi-Fi.
3.  **Monitor:** The user opens the Hope-Finder App, which immediately begins polling the ESP32.
4.  **Detect:** As the radar sensors detect changes (e.g., human movement), the ESP32 calculates distance and flags the motion. The OLED updates locally.
5.  **Report:** The Android app receives the JSON payload, updates the UI dashboard, and logs the event locally in the Room Database.
6.  **Sync:** Once the phone disconnects from the ESP32 and connects to the internet (or via cellular data), the logged events are synced to Firebase.

---

## 🛠 Setup & Installation

### ESP32 Setup (Thonny IDE)
1. Flash your ESP32 with the latest MicroPython firmware.
2. Open Thonny IDE.
3. Upload `main.py` and `ssd1306.py` from the `esp32-drivercode-thonny/` folder to the root of the ESP32.
4. Reboot the ESP32. It will display "System Booting" on the OLED and start broadcasting Wi-Fi.

### Android App Setup
1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Ensure you have your `google-services.json` file placed in the `app/` directory for Firebase integration.
4. Build and run the app on a physical Android device (API 35 target). Emulator usage is not recommended as it cannot connect to the physical ESP32 Wi-Fi network.
