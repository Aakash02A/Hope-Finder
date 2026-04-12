# MicroPython ESP32 Setup

This folder contains a MicroPython firmware target for the ESP32 that matches the Android app's API contract.

## Files
- `boot.py` connects to Wi-Fi and prints the IP address.
- `main.py` starts a small HTTP server on port 80 and serves the routes the app uses.
- `config.py` stores Wi-Fi and device settings.

## Supported routes
- `GET /health`
- `GET /api/v1/device/status`
- `POST /api/v1/detection`
- `GET /api/v1/events`
- `POST /api/v1/scan/start`
- `POST /api/v1/scan/stop`
- `POST /api/v1/calibration/start`
- `GET /api/v1/calibration/status`
- `GET /api/v1/session/summary`

## VS Code only workflow
1. Flash MicroPython onto the ESP32 once.
2. Open this folder in VS Code.
3. Install a MicroPython extension such as MicroPico or Pymakr in VS Code.
4. Upload `boot.py`, `main.py`, and `config.py` to the board.
5. Open the serial/REPL view to read the IP address.
6. Enter that IP in the Android app dashboard.

## Before upload
Update `config.py` with your real Wi-Fi SSID and password.
