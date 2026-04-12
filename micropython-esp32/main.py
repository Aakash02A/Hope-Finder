import gc
import json
import network
import socket
import time

try:
    import ujson as json_lib
except ImportError:
    json_lib = json

from config import (
    API_PREFIX,
    DEFAULT_CALIBRATION_DURATION_SECONDS,
    DEFAULT_SCAN_DURATION_SECONDS,
    DEFAULT_SECTOR_DWELL_MS,
    DEVICE_ID,
    DEVICE_NAME,
    FIRMWARE_VERSION,
    HTTP_PORT,
    WIFI_SSID,
)

from wifi_manager import wlan

state = {
    "calibrated": False,
    "scanning": False,
    "current_sector": 0,
    "current_angle": 0,
    "signal_strength": 0.0,
    "baseline": 0.45,
    "noise_level": 0.15,
    "scan_id": "scan_0",
    "calibration_id": "cal_0",
    "last_detection": None,
    "events": [],
    "errors": [],
    "warnings": [],
    "scan_started_at": None,
    "calibration_started_at": None,
    "last_check": None,
    "uptime_start": time.time(),
}


SECTOR_LABELS = [
    "N", "NNE", "NE", "ENE", "E", "ESE",
    "SE", "SSE", "S", "SSW", "SW", "WSW",
]


def iso_timestamp():
    t = time.gmtime()
    return "%04d-%02d-%02dT%02d:%02d:%02dZ" % t[:6]


def uptime_seconds():
    return int(time.time() - state["uptime_start"])


def wifi_ip():
    try:
        return wlan.ifconfig()[0]
    except Exception:
        return "0.0.0.0"


def wifi_rssi():
    try:
        return wlan.status("rssi")
    except Exception:
        return -60


def response_json(payload, status_line="HTTP/1.1 200 OK"):
    body = json_lib.dumps(payload)
    return "\r\n".join([
        status_line,
        "Content-Type: application/json; charset=utf-8",
        "Connection: close",
        "Content-Length: %d" % len(body),
        "",
        body,
    ])


def ok(payload):
    return response_json(payload)


def error_response(code, message, http_status="HTTP/1.1 500 Internal Server Error"):
    return response_json({
        "status": "error",
        "error_code": code,
        "message": message,
        "timestamp": iso_timestamp(),
    }, http_status)


def success_wrapper(data):
    return {
        "status": "success",
        "timestamp": iso_timestamp(),
        "data": data,
    }


def device_status_payload():
    last_detection = state["last_detection"]
    return success_wrapper({
        "device_id": DEVICE_ID,
        "device_name": DEVICE_NAME,
        "firmware_version": FIRMWARE_VERSION,
        "uptime_seconds": uptime_seconds(),
        "timestamp": iso_timestamp(),
        "wifi": {
            "connected": wlan.isconnected(),
            "ssid": WIFI_SSID if wlan.isconnected() else "",
            "rssi": wifi_rssi(),
            "ip": wifi_ip(),
        },
        "radar": {
            "healthy": True,
            "signal_strength": state["signal_strength"],
            "baseline": state["baseline"],
            "noise_level": state["noise_level"],
        },
        "system": {
            "calibrated": state["calibrated"],
            "scanning": state["scanning"],
            "current_sector": state["current_sector"],
            "current_angle": state["current_angle"],
            "memory_free_bytes": gc.mem_free(),
            "cpu_usage_percent": 0,
        },
        "last_detection": last_detection,
        "errors": state["errors"],
    })


def health_payload():
    checks = {
        "wifi": wlan.isconnected(),
        "radar": True,
        "storage": True,
        "display": True,
        "server": True,
    }
    return success_wrapper({
        "device_healthy": all(checks.values()),
        "checks": checks,
        "errors": state["errors"],
        "warnings": state["warnings"],
        "last_check": iso_timestamp(),
    })


def event_history_payload(limit=100, sector=None, min_confidence=None):
    all_events = state["events"][:]
    events = all_events[:]
    if sector is not None:
        events = [event for event in events if event["sector"] == sector]
    if min_confidence is not None:
        events = [event for event in events if event["confidence"] >= min_confidence]
    events = events[:max(1, min(limit, 500))]

    if events:
        earliest = events[-1]["timestamp"]
        latest = events[0]["timestamp"]
    else:
        earliest = iso_timestamp()
        latest = iso_timestamp()

    return success_wrapper({
        "total_events": len(all_events),
        "events": events,
        "earliest": earliest,
        "latest": latest,
    })


def scan_command_payload(action):
    return success_wrapper({
        "scan_id": state["scan_id"],
        "started_at": state["scan_started_at"] or iso_timestamp(),
        "estimated_duration_seconds": DEFAULT_SCAN_DURATION_SECONDS if action == "start" else None,
        "stopped_at": iso_timestamp() if action == "stop" else None,
        "events_during_scan": len(state["events"]) if action == "stop" else None,
    })


def calibration_payload():
    return success_wrapper({
        "calibration_id": state["calibration_id"],
        "started_at": state["calibration_started_at"] or iso_timestamp(),
        "estimated_duration_seconds": DEFAULT_CALIBRATION_DURATION_SECONDS,
        "baseline_current": state["baseline"],
    })


def session_summary_payload(start, end):
    total_events = len(state["events"])
    by_confidence = {"LOW": 0, "MEDIUM": 0, "HIGH": 0}
    by_sector = {}
    for event in state["events"]:
        level = event["confidence_level"]
        if level in by_confidence:
            by_confidence[level] += 1
        sector_key = str(event["sector"])
        by_sector[sector_key] = by_sector.get(sector_key, 0) + 1

    average_confidence = 0.0
    if total_events:
        average_confidence = sum(event["confidence"] for event in state["events"]) / total_events

    return success_wrapper({
        "session_id": "session_%d_%d" % (start, end),
        "start_time": iso_timestamp(),
        "end_time": iso_timestamp(),
        "total_events": total_events,
        "events_by_confidence": by_confidence,
        "events_by_sector": by_sector,
        "peak_activity_hour": time.localtime()[3],
        "average_confidence": float(average_confidence),
        "scan_coverage_percent": 100 if state["scanning"] else 0,
    })


def parse_query(path):
    if "?" not in path:
        return path, {}
    base, query = path.split("?", 1)
    params = {}
    for pair in query.split("&"):
        if not pair:
            continue
        if "=" in pair:
            key, value = pair.split("=", 1)
            params[key] = value
        else:
            params[pair] = ""
    return base, params


def read_request(conn):
    request = b""
    while b"\r\n\r\n" not in request:
        chunk = conn.recv(512)
        if not chunk:
            break
        request += chunk
        if len(request) > 8192:
            break

    header_text = request.decode("utf-8", "ignore")
    header_part, _, body_part = header_text.partition("\r\n\r\n")
    lines = header_part.split("\r\n")
    method, path, _ = lines[0].split(" ", 2)
    content_length = 0
    for line in lines[1:]:
        if line.lower().startswith("content-length:"):
            content_length = int(line.split(":", 1)[1].strip())
            break

    body_bytes = body_part.encode("utf-8")
    while len(body_bytes) < content_length:
        chunk = conn.recv(content_length - len(body_bytes))
        if not chunk:
            break
        body_bytes += chunk
    body = body_bytes.decode("utf-8", "ignore")
    return method, path, body


def build_detection(body_text):
    try:
        incoming = json_lib.loads(body_text) if body_text else {}
    except Exception:
        incoming = {}

    detection = {
        "device_id": incoming.get("device_id", DEVICE_ID),
        "event_id": incoming.get("event_id", "evt_%d_%d" % (int(time.time()), len(state["events"]))),
        "timestamp": incoming.get("timestamp", iso_timestamp()),
        "sector": int(incoming.get("sector", state["current_sector"])),
        "sector_label": incoming.get("sector_label", SECTOR_LABELS[state["current_sector"] % len(SECTOR_LABELS)]),
        "angle": int(incoming.get("angle", state["current_angle"])),
        "raw_signal": float(incoming.get("raw_signal", 2.156)),
        "filtered_signal": float(incoming.get("filtered_signal", 1.892)),
        "baseline": float(incoming.get("baseline", state["baseline"])),
        "signal_delta": float(incoming.get("signal_delta", 1.442)),
        "signal_strength": float(incoming.get("signal_strength", 0.78)),
        "motion_level": int(incoming.get("motion_level", 2)),
        "motion_label": incoming.get("motion_label", "Possible Human Movement"),
        "confidence": int(incoming.get("confidence", 76)),
        "confidence_level": incoming.get("confidence_level", "HIGH"),
        "human_presence_possible": bool(incoming.get("human_presence_possible", True)),
        "wifi_rssi": int(incoming.get("wifi_rssi", wifi_rssi())),
        "scan_duration_ms": int(incoming.get("scan_duration_ms", DEFAULT_SECTOR_DWELL_MS)),
    }
    state["last_detection"] = {
        "timestamp": detection["timestamp"],
        "confidence": detection["confidence"],
        "sector": detection["sector"],
    }
    state["events"].insert(0, detection)
    state["events"] = state["events"][:100]
    state["signal_strength"] = detection["signal_strength"]
    return detection


def route_request(method, path, body):
    base_path, params = parse_query(path)

    if base_path == "/health" and method == "GET":
        return ok(health_payload())

    if base_path == API_PREFIX + "/device/status" and method == "GET":
        return ok(device_status_payload())

    if base_path == API_PREFIX + "/detection" and method == "POST":
        build_detection(body)
        return ok({
            "status": "success",
            "message": "Event received and stored",
            "timestamp": iso_timestamp(),
        })

    if base_path == API_PREFIX + "/events" and method == "GET":
        limit = int(params.get("limit", "100"))
        sector = params.get("sector")
        min_confidence = params.get("min_confidence")
        return ok(event_history_payload(
            limit=limit,
            sector=int(sector) if sector is not None and sector != "" else None,
            min_confidence=int(min_confidence) if min_confidence is not None and min_confidence != "" else None,
        ))

    if base_path == API_PREFIX + "/scan/start" and method == "POST":
        state["scanning"] = True
        state["scan_started_at"] = iso_timestamp()
        state["scan_id"] = "scan_%d" % int(time.time())
        return ok(scan_command_payload("start"))

    if base_path == API_PREFIX + "/scan/stop" and method == "POST":
        state["scanning"] = False
        return ok(scan_command_payload("stop"))

    if base_path == API_PREFIX + "/calibration/start" and method == "POST":
        state["calibrated"] = True
        state["calibration_started_at"] = iso_timestamp()
        state["calibration_id"] = "cal_%d" % int(time.time())
        return ok(calibration_payload())

    if base_path == API_PREFIX + "/calibration/status" and method == "GET":
        return ok(success_wrapper({
            "calibration_id": state["calibration_id"],
            "in_progress": False,
            "progress_percent": 100 if state["calibrated"] else 0,
            "baseline_current": state["baseline"],
            "estimated_duration_seconds": DEFAULT_CALIBRATION_DURATION_SECONDS,
            "started_at": state["calibration_started_at"] or iso_timestamp(),
        }))

    if base_path == API_PREFIX + "/session/summary" and method == "GET":
        start = int(params.get("start", str(int(time.time() * 1000))))
        end = int(params.get("end", str(int(time.time() * 1000))))
        return ok(session_summary_payload(start, end))

    return error_response("NOT_FOUND", "Route not found", "HTTP/1.1 404 Not Found")


def start_server():
    addr = socket.getaddrinfo("0.0.0.0", HTTP_PORT)[0][-1]
    server = socket.socket()
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(addr)
    server.listen(1)
    print("[HTTP] Listening on port %d" % HTTP_PORT)
    print("[HTTP] Device IP:", wifi_ip())

    while True:
        conn, _ = server.accept()
        try:
            method, path, body = read_request(conn)
            response = route_request(method, path, body)
            conn.send(response.encode("utf-8"))
        except Exception as exc:
            try:
                state["errors"].append(str(exc))
                conn.send(error_response("SERVER_ERROR", str(exc)).encode("utf-8"))
            except Exception:
                pass
        finally:
            conn.close()


print("=== Hope-Finder MicroPython Firmware ===")
print("Device:", DEVICE_ID)
print("Wi-Fi connected:", wlan.isconnected())
print("IP:", wifi_ip())
state["last_check"] = iso_timestamp()
start_server()
