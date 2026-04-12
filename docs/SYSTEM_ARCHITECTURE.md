# Radar-Based Living Person Detection System
## Complete System Architecture

### Project Overview
A prototype emergency search-and-rescue system using a single HB100 Doppler radar probe to detect possible human motion under rubble. The system processes motion signals, estimates direction/angle of detection, and transmits real-time data to a mobile application for rescue team visualization and decision-making.

---

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       Physical Layer                            │
├─────────────────────────────────────────────────────────────────┤
│  HB100 Doppler Radar → LM358 Op-Amp → ESP32 ADC (GPIO34)       │
│  [Optional] Servo/Stepper Motor → GPIO5/GPIO18                 │
│  SSD1306 OLED Display → I2C (GPIO21, GPIO22)                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    ESP32 Firmware Layer                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌─ Sensor Input Module                                        │
│  │   • ADC sampling (100-1000 Hz)                             │
│  │   • Noise filtering                                         │
│  │   • Signal conditioning                                    │
│  │                                                             │
│  ├─ Motion Detection Engine                                    │
│  │   • Baseline calculation                                    │
│  │   • Moving average smoothing                               │
│  │   • Peak detection                                         │
│  │   • Confidence scoring                                     │
│  │                                                             │
│  ├─ Sector Scanning Controller                                │
│  │   • 12-sector rotation logic                              │
│  │   • Angle-to-sector mapping                               │
│  │   • Direction labeling                                     │
│  │                                                             │
│  ├─ Wi-Fi & HTTP Client                                       │
│  │   • Real-time JSON transmission                           │
│  │   • Endpoint management                                    │
│  │   • Connection retry logic                                 │
│  │                                                             │
│  ├─ OLED Display Manager                                      │
│  │   • Status rendering                                       │
│  │   • Real-time updates                                      │
│  │   • Error state display                                    │
│  │                                                             │
│  └─ Calibration & Configuration Module                        │
│      • Baseline tuning                                         │
│      • Threshold adjustment                                    │
│      • Device health monitoring                               │
│                                                             │
│  Event Logger: Timestamped storage in SPIFFS                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     Network Layer (Wi-Fi)                       │
├─────────────────────────────────────────────────────────────────┤
│  HTTP POST/GET to Mobile App (JSON over TCP/IP)               │
│  • Live detection stream                                        │
│  • Status polling                                              │
│  • Event history                                               │
│  • Device health                                               │
│  • Configuration endpoints                                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                  Mobile App Layer (Android)                    │
├─────────────────────────────────────────────────────────────────┤
│  ┌─ Network Backend                                            │
│  │   • HTTP client (OkHttp)                                   │
│  │   • Polling/WebSocket logic                                │
│  │   • Error handling & retry                                 │
│  │                                                             │
│  ├─ Data Layer                                                 │
│  │   • Room database (local storage)                          │
│  │   • Data entities                                          │
│  │   • Repository pattern                                     │
│  │                                                             │
│  ├─ ViewModel Layer                                            │
│  │   • Screen state management                                │
│  │   • Live data flow                                         │
│  │   • Business logic coordination                            │
│  │                                                             │
│  └─ UI Layer (Jetpack Compose)                                 │
│      • Dashboard Screen                                        │
│      • Radar Screen (circular animation)                       │
│      • Alerts Screen (list view)                              │
│      • Reports & Analytics                                     │
│      • Bottom Navigation                                       │
│                                                             │
│  Local Features:                                               │
│  • Event caching                                               │
│  • Offline mode support                                        │
│  • PDF/CSV export                                              │
│  • Alert notifications                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Component Breakdown

### 1. ESP32 Firmware Architecture

#### Main Modules:

**Sensor Input Handler** (`hb100_sensor.h/cpp`)
- Configures ADC for continuous sampling
- Reads analog signal from LM358 (0-3.3V)
- Applies digital low-pass filtering
- Maintains raw and filtered signal buffers
- Monitors signal health (detects open circuits, shorts)

**Motion Detection Engine** (`motion_detector.h/cpp`)
- Maintains running baseline from ambient readings
- Implements moving average (window: 500-2000 samples)
- Detects envelope peaks (derivative analysis)
- Calculates Doppler shift indicators
- Scores motion confidence (0-100%)
- Maps confidence to motion levels:
  - **No Motion**: 0-15%
  - **Weak Motion**: 16-40%
  - **Possible Human Movement**: 41-75%
  - **Strong Movement / High Confidence**: 76-100%

**Sector Scanner** (`sector_scanner.h/cpp`)
- Divides 360° into 12 sectors (30° each)
- Sector labels: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW
- If motor present: sweeps continuously, logs angle + motion
- If no motor: rotates through sectors sequentially (samples each for 2-5 seconds)
- Records sector index, angle (0-359°), motion level, confidence

**Wi-Fi & HTTP Client** (`wifi_client.h/cpp`)
- Connects to configured SSID/password
- Maintains persistent connection with auto-reconnect
- Implements HTTP POST for real-time detection events
- Supports GET for status polling
- Timeout handling and error recovery

**Calibration Module** (`calibration.h/cpp`)
- Ambient noise baseline establishment (30-60 second sweep)
- Motion threshold tuning (operator-guided)
- Sensitivity adjustment
- Stores settings to SPIFFS

**OLED Display Manager** (`display_manager.h/cpp`)
- Renders status on 128x64 OLED
- Shows: sector, angle, motion level, signal strength, Wi-Fi status
- Real-time update (100ms refresh)
- Error state indicators

**Watchdog & Health Monitor** (`health_monitor.h/cpp`)
- Monitors Wi-Fi connectivity
- Checks ADC signal stability
- Detects calibration state
- Logs errors to internal storage

#### Data Structures:
```cpp
struct SensorReading {
  uint32_t timestamp;
  uint16_t raw_adc;
  float filtered_signal;
  float baseline;
  float signal_delta;
};

struct Detection {
  uint32_t timestamp;
  uint8_t sector;
  uint16_t angle;
  float signal_strength;
  uint8_t motion_level;  // 0-3
  uint8_t confidence;    // 0-100
  bool human_presence_possible;
};

struct DeviceStatus {
  bool wifi_connected;
  int16_t rssi;
  bool radar_healthy;
  bool calibrated;
  uint32_t last_detection;
  uint32_t uptime;
};
```

---

### 2. Mobile App Architecture

#### Data Layer
**Room Database Models** (`models/`)
- `DetectionEntity`: Stores detection events locally
- `AlertEntity`: Stores alerts with acknowledgment status
- `ScanSessionEntity`: Groups detections by session
- `CalibrationProfileEntity`: Stores calibration data

**Repository Pattern** (`repository/`)
- `DetectionRepository`: Manages local and remote detection data
- `DeviceRepository`: Manages device connection state
- `AlertRepository`: Alert management logic

#### ViewModel Layer (`viewmodel/`)
- `DashboardViewModel`: Device status, connection, latest detection
- `RadarViewModel`: Real-time detection stream, scan animation state
- `AlertsViewModel`: Alert filtering, sorting, acknowledgment
- `ReportsViewModel`: Historical data, statistics, export logic
- `MainViewModel`: Navigation state, global device context

#### UI Layer (Jetpack Compose) (`ui/screens/` & `ui/components/`)
- **DashboardScreen**: Overview of system status
- **RadarScreen**: Circular scanning visualization with animated sweep
- **AlertsScreen**: Severity-sorted detection alerts
- **ReportsScreen**: Analytics and export options
- **Components**: Reusable UI elements for consistency
  - `CircularRadar`: Animated circular scanner
  - `StatusBadge`: Connection/status indicators
  - `MotionLevelIndicator`: Color-coded motion display
  - `ConfidenceBar`: Visual confidence meter

#### Network Layer (`network/`)
- `DeviceClient`: HTTP interactions with ESP32
- `DetectionApiService`: Interfaces for real-time API calls
- `ConnectionState`: Manages connection lifecycle

#### Local Storage
- Room database for recent detections (capped at 1000 recent)
- Shared preferences for configuration
- File storage for exported reports

---

### 3. API Design

#### HTTP Endpoints (ESP32 → Mobile App)

**1. Live Detection Event**
- Endpoint: `POST /api/v1/detection`
- Trigger: Real-time on motion detection
- Rate: Up to 10 events/second

**2. Device Status**
- Endpoint: `GET /api/v1/device/status`
- Response: Current health, connection, uptime

**3. Event History**
- Endpoint: `GET /api/v1/events?start=ts&limit=100`
- Response: Historical detections with pagination

**4. Calibration Update**
- Endpoint: `POST /api/v1/calibration`
- Body: Threshold, sensitivity parameters

**5. Device Health Check**
- Endpoint: `GET /api/v1/health`
- Response: System diagnostics

See `API_SPECIFICATION.md` for detailed endpoint documentation and JSON schemas.

---

### 4. Data Flow & Real-Time Processing

#### Acquisition Pipeline:
```
ADC Sample (100-1000 Hz)
    ↓
Low-Pass Filter (50Hz corner)
    ↓
Noise Baseline Tracking
    ↓
Envelope Detection (Hilbert Transform or simple peak detection)
    ↓
Motion Scoring Algorithm
    ↓
Sector Accumulation (weighted by direction)
    ↓
Confidence Calculation
    ↓
→ HTTP Transmission to Mobile App
→ OLED Display Update
→ Log to SPIFFS
```

#### Real-Time App Pipeline:
```
HTTP Poll/Stream from ESP32
    ↓
Parse JSON Detection
    ↓
Store in Room Database
    ↓
Update ViewModel LiveData
    ↓
Trigger Radar Animation
    ↓
Check Alert Threshold
    ↓
Display Alert Notification (if confidence > 60%)
    ↓
Render on UI (Dashboard, Radar, Alerts)
```

---

### 5. Scanning Strategy

#### Sector-Based Rotation (No Motor):
- 12 sectors labeled with cardinal directions
- Each sector sampled for 3 seconds
- Full rotation: 36 seconds
- Repeating cycle provides continuous coverage

#### Array Coordinates (Future Hexagonal Network):
Each detection tagged with:
- Probe ID (currently: "master_probe_01")
- Sector/angle
- Confidence score
- When multi-probe system deployed: combine detections from adjacent probes to triangulate

---

### 6. Scalability to Hexagonal Network

The prototype is designed to scale:

**Current State (1 Master Probe)**:
- Single center probe with 360° scanning
- Coverage map in single device

**Future State (6-Probe Hexagonal Array)**:
- Each probe with ID: "probe_01" through "probe_06"
- Positioned at vertices of hexagon
- Each probe scans 120° outward sector
- Central aggregation device (or mobile app) correlates:
  - Signal triangulation
  - Cross-probe motion pattern recognition
  - High-confidence zone identification

**Code Design Notes**:
- Firmware modular: easily duplicated for multiple probes
- Data models include `device_id` and `probe_index` fields
- API routes support device filtering: `/api/v1/detections?device_id=probe_03`
- Mobile app can display multi-probe overlay on unified radar map
- Database schema supports multi-device sessions

---

## Hardware Configuration

### GPIO Mapping (ESP32)

| GPIO | Function | Notes |
|------|----------|-------|
| GPIO34 | ADC IN (HB100 signal) | Analog input from LM358 output |
| GPIO5 | Motor Control (PWM) | For servo/stepper rotation (optional) |
| GPIO18 | Motor Direction | For stepper direction control |
| GPIO21 | I2C SDA (OLED) | Serial display communication |
| GPIO22 | I2C SCL (OLED) | |

### Power Requirements
- HB100 Radar: 5V, ~30mA
- LM358 Op-Amp: ±5V or 5-15V single supply, ~2mA
- ESP32: 3.3V, ~100mA (Wi-Fi: up to 300mA during TX)
- OLED: 3.3V, ~20mA
- Optional Motor: 5-12V, 200-500mA (independent supply)

---

## Motion Detection Algorithm

### Signal Processing:

1. **Baseline Establishment**
   - Average first 100 samples at startup
   - Re-baseline every hour or on demand

2. **Filtering**
   ```
   filtered[n] = 0.7 * filtered[n-1] + 0.3 * raw[n]
   ```
   - Moving average window: 50 samples (500ms at 100Hz)

3. **Envelope Detection**
   - Peak detection: find local maxima above baseline
   - Calculate envelope as smoothed peak values

4. **Doppler Shift Scoring**
   - Frequency domain analysis (optional, for future FFT)
   - Amplitude analysis: peak - baseline = signal_strength
   - Consistency over 1-2 seconds = confidence boost

5. **Confidence Calculation**
   ```
   confidence = min(100, (signal_strength / threshold) * 100)
   ```
   - Adjustable threshold (default: 0.5V)
   - Multi-sample averaging improves estimate

---

## Safety & Compliance Notes

- **No Certainty Claims**: Output "possible human presence" not "human detected"
- **False Positive Management**: 
  - Require consistent signal over 2+ sectors
  - Cross-check with time consistency
  - Allow operator override/calibration
- **Emergency Context**: Biased toward sensitivity (alert on weak signal with disclaimer)
- **Data Privacy**: All data local except live stream to mobile app; no cloud storage in prototype
- **Accessibility**: Large text, high contrast, simple navigation for stressed operators

---

## Testing Strategy

1. **Unit Tests** (ESP32 firmware)
   - Motion detection algorithm with synthetic signals
   - Sector mapping logic
   - HTTP payload formatting

2. **Integration Tests**
   - ADC → Detection pipeline
   - Wi-Fi transmission reliability
   - OLED display updates

3. **UI Tests** (Android app)
   - Radar animation smoothness
   - Data refresh cycles
   - Alert notification triggering

4. **Field Tests**
   - Placement in various conditions
   - Calibration repeatability
   - Detection accuracy against known targets

---

## Deployment Checklist

- [ ] Flash ESP32 with compiled firmware
- [ ] Configure Wi-Fi credentials (SSID, password)
- [ ] Run calibration mode (establish baseline)
- [ ] Deploy OLED display (test I2C connection)
- [ ] Install mobile app on rescue team phones
- [ ] Connect mobile app to ESP32 network
- [ ] Verify real-time data stream
- [ ] Test alert notifications
- [ ] Confirm outdoor battery life (~4-8 hours typical)

---

## Future Enhancements

1. Multi-probe hexagonal array deployment
2. ML-based motion classification (FFT + neural network)
3. Cloud backup of detection events
4. Integration with rescue operation command center
5. Drone integration for aerial deployment
6. Spectrum analysis (FFT) for breathing/heartbeat detection
7. Redundant ESP32 units for failover
8. Persistent GPS logging of detection locations

---

**Document Version**: 1.0  
**Last Updated**: 2026-03-29  
**Status**: Active Prototype Development
