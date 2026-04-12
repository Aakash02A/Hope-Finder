# HTTP API Specification
## ESP32 ↔ Mobile App Communication

### Base URL
```
http://<ESP32_IP>:80/api/v1
```

### Common Response Format

**Success (200)**
```json
{
  "status": "success",
  "timestamp": "2026-03-29T10:20:30Z",
  "data": { }
}
```

**Error (4xx/5xx)**
```json
{
  "status": "error",
  "error_code": "WIFI_DOWN",
  "message": "Wi-Fi connection lost",
  "timestamp": "2026-03-29T10:20:30Z"
}
```

---

## Endpoint 1: Live Detection Event
**Endpoint**: `POST /detection`  
**Trigger**: Sent by ESP32 in real-time when motion detected  
**Latency**: < 500ms from detection to transmission  
**Frequency**: Up to 10 events/second during active scanning

### Request Body
```json
{
  "device_id": "master_probe_01",
  "event_id": "evt_1711772430_0",
  "timestamp": "2026-03-29T10:20:30.123Z",
  "sector": 3,
  "sector_label": "ENE",
  "angle": 112,
  "raw_signal": 2.156,
  "filtered_signal": 1.892,
  "baseline": 0.45,
  "signal_delta": 1.442,
  "signal_strength": 0.78,
  "motion_level": 2,
  "motion_label": "Possible Human Movement",
  "confidence": 76,
  "confidence_level": "HIGH",
  "human_presence_possible": true,
  "wifi_rssi": -52,
  "scan_duration_ms": 3000
}
```

### Response
```json
{
  "status": "success",
  "message": "Event received and stored",
  "timestamp": "2026-03-29T10:20:30.150Z"
}
```

### Confidence Mapping
| Confidence Range | Level | Motion Label | Action |
|------------------|-------|------|--------|
| 0-15% | NONE | No Motion | Skip |
| 16-40% | LOW | Weak Motion | Log only |
| 41-75% | MEDIUM | Possible Human Movement | Alert operator |
| 76-100% | HIGH | Strong Movement / High Confidence | Immediate alert |

---

## Endpoint 2: Device Status
**Endpoint**: `GET /device/status`  
**Trigger**: Requested by mobile app (every 5-30 seconds)  
**Purpose**: Real-time health and connection state

### Response
```json
{
  "status": "success",
  "data": {
    "device_id": "master_probe_01",
    "device_name": "Master Probe - Sector 1",
    "firmware_version": "1.0.0",
    "uptime_seconds": 3600,
    "timestamp": "2026-03-29T10:20:30Z",
    "wifi": {
      "connected": true,
      "ssid": "RescueNet-5G",
      "rssi": -52,
      "ip": "192.168.1.100"
    },
    "radar": {
      "healthy": true,
      "signal_strength": 0.78,
      "baseline": 0.45,
      "noise_level": 0.15
    },
    "system": {
      "calibrated": true,
      "scanning": true,
      "current_sector": 3,
      "current_angle": 112,
      "memory_free_bytes": 65536,
      "cpu_usage_percent": 35
    },
    "last_detection": {
      "timestamp": "2026-03-29T10:20:28Z",
      "confidence": 76,
      "sector": 3
    },
    "errors": []
  }
}
```

---

## Endpoint 3: Event History
**Endpoint**: `GET /events?start=<timestamp>&limit=100&sector=<0-11>`  
**Purpose**: Retrieve historical detections for playback/analysis  
**Parameters**:
- `start`: ISO 8601 timestamp (optional, default: last hour)
- `limit`: Number of events (1-500, default: 100)
- `sector`: Filter by sector (0-11, optional)
- `min_confidence`: Filter by confidence (0-100, optional, default: 0)

### Response
```json
{
  "status": "success",
  "data": {
    "total_events": 245,
    "events": [
      {
        "event_id": "evt_1711772430_0",
        "timestamp": "2026-03-29T10:20:30Z",
        "sector": 3,
        "sector_label": "ENE",
        "angle": 112,
        "signal_strength": 0.78,
        "motion_level": 2,
        "motion_label": "Possible Human Movement",
        "confidence": 76,
        "human_presence_possible": true,
        "duration_ms": 2500
      }
    ],
    "earliest": "2026-03-29T05:20:30Z",
    "latest": "2026-03-29T10:20:30Z"
  }
}
```

---

## Endpoint 4: Start Scanning
**Endpoint**: `POST /scan/start`  
**Purpose**: Initiate sector scanning cycle  
**Body**:
```json
{
  "duration_seconds": 300,
  "sector_dwell_ms": 3000,
  "sensitivity": "high"
}
```

### Response
```json
{
  "status": "success",
  "data": {
    "scan_id": "scan_1711772430",
    "started_at": "2026-03-29T10:20:30Z",
    "estimated_duration_seconds": 300
  }
}
```

---

## Endpoint 5: Stop Scanning
**Endpoint**: `POST /scan/stop`  
**Purpose**: Halt current scan

### Response
```json
{
  "status": "success",
  "data": {
    "scan_id": "scan_1711772430",
    "stopped_at": "2026-03-29T10:25:30Z",
    "events_during_scan": 45
  }
}
```

---

## Endpoint 6: Calibration
**Endpoint**: `POST /calibration/start`  
**Purpose**: Begin baseline and threshold calibration  
**Duration**: 60 seconds

### Request
```json
{
  "mode": "quick",
  "duration_seconds": 60
}
```

### Response
```json
{
  "status": "success",
  "data": {
    "calibration_id": "cal_1711772430",
    "started_at": "2026-03-29T10:20:30Z",
    "estimated_duration_seconds": 60,
    "baseline_current": 0.45
  }
}
```

---

## Endpoint 7: Calibration Status
**Endpoint**: `GET /calibration/status`  
**Purpose**: Poll calibration progress

### Response
```json
{
  "status": "success",
  "data": {
    "calibration_id": "cal_1711772430",
    "state": "in_progress",
    "progress_percent": 45,
    "baseline_samples": 450,
    "estimated_baseline": 0.42,
    "started_at": "2026-03-29T10:20:30Z"
  }
}
```

---

## Endpoint 8: Calibration Update Parameters
**Endpoint**: `POST /calibration/update`  
**Purpose**: Adjust sensitivity, threshold after calibration

### Request
```json
{
  "motion_threshold": 0.5,
  "sensitivity_multiplier": 1.2,
  "confidence_floor": 40
}
```

### Response
```json
{
  "status": "success",
  "data": {
    "updated_at": "2026-03-29T10:20:30Z",
    "motion_threshold": 0.5,
    "sensitivity_multiplier": 1.2
  }
}
```

---

## Endpoint 9: Device Health Check
**Endpoint**: `GET /health`  
**Purpose**: Diagnostics and system state

### Response
```json
{
  "status": "success",
  "data": {
    "device_healthy": true,
    "checks": {
      "wifi_connected": true,
      "radar_responding": true,
      "oled_display": true,
      "calibration_valid": true,
      "memory_available": true,
      "cpu_load_normal": true
    },
    "errors": [],
    "warnings": [
      "Wi-Fi RSSI below -60dBm (weak signal)"
    ],
    "last_check": "2026-03-29T10:20:30Z"
  }
}
```

---

## Endpoint 10: Event Session Summary
**Endpoint**: `GET /session/summary?start=<ts>&end=<ts>`  
**Purpose**: Generate statistics for reports

### Response
```json
{
  "status": "success",
  "data": {
    "session_id": "sess_1711772430",
    "start_time": "2026-03-29T05:20:30Z",
    "end_time": "2026-03-29T10:20:30Z",
    "total_events": 245,
    "events_by_confidence": {
      "none": 0,
      "low": 45,
      "medium": 120,
      "high": 80
    },
    "events_by_sector": {
      "0": 20,
      "1": 35,
      "2": 18,
      "3": 55,
      "4": 25,
      "5": 12,
      "6": 80
    },
    "peak_activity_hour": 7,
    "average_confidence": 65,
    "scan_coverage_percent": 98
  }
}
```

---

## Endpoint 11: Configuration Get
**Endpoint**: `GET /config`  
**Purpose**: Retrieve all device configuration

### Response
```json
{
  "status": "success",
  "data": {
    "device_name": "Master Probe - Sector 1",
    "scanning": {
      "enabled": true,
      "sector_dwell_ms": 3000,
      "motor_enabled": false,
      "rotation_speed_rpm": 0
    },
    "motion_detection": {
      "motion_threshold": 0.5,
      "confidence_floor": 40,
      "sensitivity_multiplier": 1.0,
      "baseline_refresh_interval_s": 3600
    },
    "wifi": {
      "ssid": "RescueNet-5G",
      "rssi_threshold": -70
    },
    "display": {
      "brightness": 255,
      "update_interval_ms": 100
    }
  }
}
```

---

## Endpoint 12: Configuration Update
**Endpoint**: `POST /config/update`  
**Purpose**: Update device configuration

### Request
```json
{
  "device_name": "Master Probe - Updated",
  "motion_detection": {
    "motion_threshold": 0.6,
    "sensitivity_multiplier": 1.1
  }
}
```

### Response
```json
{
  "status": "success",
  "data": {
    "updated_at": "2026-03-29T10:20:30Z",
    "restart_required": false
  }
}
```

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 400 | Bad request (invalid JSON, missing fields) |
| 404 | Not found (scanning not active, etc.) |
| 500 | Server error (Wi-Fi down, sensor error) |
| 503 | Service unavailable (calibration in progress) |
| 504 | Gateway timeout (device rebooting) |

---

## Error Codes

| Error Code | Description | Recovery |
|-----------|-------------|----------|
| WIFI_DOWN | Wi-Fi connection lost | Auto-reconnect |
| RADAR_ERROR | ADC or signal error | Check connections, restart |
| CALIBRATION_INCOMPLETE | Must calibrate before scanning | Run calibration endpoint |
| INVALID_SECTOR | Sector out of range (0-11) | Use valid sector |
| PAYLOAD_TOO_LARGE | JSON exceeds buffer | Split into chunks |
| SCANNING_ACTIVE | Can't calibrate while scanning | Stop scan first |

---

## Real-Time Connection Strategy (Mobile App)

### Polling Approach (Recommended for prototype)
1. App polls `/device/status` every 10 seconds
2. App polls `/events?start=<last_event_time>` every 2 seconds
3. On receiving event: check confidence, trigger alert if > 60%
4. Update UI: radar animation, alerts list, dashboard

### WebSocket Alternative (Future)
- Upgrade to WebSocket endpoint: `ws://<ESP32_IP>/api/v1/ws/events`
- Server pushes detection events in real-time
- Reduces polling overhead, truly real-time

---

## Data Retention

- Recent events: Last 1000 detections in SPIFFS (circular buffer)
- Session data: Configurable via EEPROM, default: current session only
- Logs: Last 100 system events

---

## Security Notes (Prototype)

- **No Authentication**: Prototype assumes private network
- **CORS**: Not applicable (direct HTTP connection)
- **Future**: Add API key or token-based auth for deployed systems
- **HTTPS**: Not supported on ESP32 (resource constraints); use VPN for sensitive deployments

---

**API Specification Version**: 1.0  
**Last Updated**: 2026-03-29
