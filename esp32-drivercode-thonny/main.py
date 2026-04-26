from machine import Pin, ADC, I2C
import ssd1306
import network
import socket
import json
import time

# ============ Hardware Setup ============
# Radar sensors
radar1 = ADC(Pin(34))
radar2 = ADC(Pin(35))
radar1.atten(ADC.ATTN_11DB)
radar2.atten(ADC.ATTN_11DB)

# LEDs
green = Pin(25, Pin.OUT)
red = Pin(26, Pin.OUT)

# OLED
i2c = I2C(0, scl=Pin(22), sda=Pin(21))
oled = ssd1306.SSD1306_I2C(128, 64, i2c)

threshold = 15

# ============ WiFi Configuration ============
SSID = "ESP32_RADAR"
PASSWORD = "radar123"

# ============ Global Variables ============
baseline1 = 0
baseline2 = 0
last1 = 0
last2 = 0
current_data = {
    "left_distance": ">4m",
    "right_distance": ">4m",
    "motion_detected": False,
    "timestamp": 0,
    "change_left": 0,
    "change_right": 0
}

# ============ Functions ============
def stable_read(sensor):
    total = 0
    for i in range(10):
        total += sensor.read()
        time.sleep(0.01)
    return total // 10

def estimate_distance(change):
    if change < 10:
        return ">4m"
    elif change < 25:
        return "3m"
    elif change < 50:
        return "2m"
    elif change < 80:
        return "1m"
    else:
        return "<1m"

def setup_wifi():
    """Create WiFi Access Point"""
    ap = network.WLAN(network.AP_IF)
    ap.active(True)
    
    # Handle API differences across MicroPython versions
    try:
        ap.config(ssid=SSID, password=PASSWORD, authmode=network.AUTH_WPA_WPA2_PSK)
    except Exception:
        ap.config(essid=SSID, password=PASSWORD, authmode=network.AUTH_WPA_WPA2_PSK)
    
    while not ap.active():
        time.sleep(0.1)
    
    ip_address = ap.ifconfig()[0]
    print("WiFi AP Active")
    print("SSID:", SSID)
    print("Password:", PASSWORD)
    print("IP Address:", ip_address)
    
    # Show WiFi info on OLED
    oled.fill(0)
    oled.text("WiFi Active", 0, 0)
    oled.text("SSID: " + SSID, 0, 15)
    oled.text("IP:", 0, 35)
    oled.text(ip_address, 0, 50)
    oled.show()
    time.sleep(3)
    
    return ap

def create_http_response(data):
    """Create HTTP response with JSON data"""
    response_body = json.dumps(data)
    response = "HTTP/1.1 200 OK\r\n"
    response += "Content-Type: application/json\r\n"
    response += "Access-Control-Allow-Origin: *\r\n"
    response += "Connection: close\r\n"
    response += "Content-Length: " + str(len(response_body)) + "\r\n"
    response += "\r\n"
    response += response_body
    return response

def handle_client(client_socket):
    """Handle incoming HTTP requests"""
    try:
        # Prevent EAGAIN crash by temporarily adding a timeout to wait for HTTP request data
        client_socket.settimeout(2.0)
        request = client_socket.recv(1024).decode('utf-8')
        
        # Handle CORS preflight
        if 'OPTIONS' in request:
            response = "HTTP/1.1 200 OK\r\n"
            response += "Access-Control-Allow-Origin: *\r\n"
            response += "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n"
            response += "Access-Control-Allow-Headers: Content-Type\r\n"
            response += "Connection: close\r\n\r\n"
            client_socket.send(response.encode('utf-8'))
        
        # Handle GET /data request
        elif 'GET /data' in request:
            response = create_http_response(current_data)
            client_socket.send(response.encode('utf-8'))
        
        # Handle GET / (root) request - simple status page
        elif 'GET / ' in request:
            html = """HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nConnection: close\r\n\r\n
<html>
<head>
<title>ESP32 Radar</title>
</head>
<body style='font-family:Arial;text-align:center;margin-top:50px;'>
<h1>ESP32 Doppler Radar System</h1>
<h2>Status: Active</h2>

<p id="data">Loading...</p>

<script>
function fetchData() {
    fetch('/data')
    .then(response => response.json())
    .then(data => {
        document.getElementById("data").innerHTML =
            "Left: " + data.left_distance + "<br>" +
            "Right: " + data.right_distance + "<br>" +
            "Motion: " + (data.motion_detected ? "DETECTED" : "CLEAR");
    });
}

setInterval(fetchData, 1000);
fetchData();
</script>

</body>
</html>
""".format(
                left=current_data['left_distance'],
                right=current_data['right_distance'],
                motion="DETECTED" if current_data['motion_detected'] else "CLEAR"
            )
            client_socket.send(html.encode('utf-8'))
        
        else:
            # 404 response
            response = "HTTP/1.1 404 NOT FOUND\r\nConnection: close\r\n\r\n"
            client_socket.send(response.encode('utf-8'))
            
    except Exception as e:
        print("Error handling client:", e)
    finally:
        # Guarantee closure so the ESP32 doesn't freeze from hitting open socket limits
        try:
            client_socket.close()
        except:
            pass

def start_server():
    """Start HTTP server on port 80"""
    addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
    server_socket = socket.socket()
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind(addr)
    server_socket.listen(5)
    server_socket.setblocking(False)  # Keep the main server listener non-blocking
    print("HTTP Server listening on port 80")
    return server_socket

def update_sensor_data():
    """Read sensors and update global data"""
    global last1, last2, current_data
    
    r1 = stable_read(radar1)
    r2 = stable_read(radar2)
    
    change1 = abs(r1 - last1)
    change2 = abs(r2 - last2)
    
    dist1 = estimate_distance(change1)
    dist2 = estimate_distance(change2)
    
    motion = change1 > threshold or change2 > threshold
    
    # Update current data dictionary for the mobile API
    current_data = {
        "left_distance": dist1,
        "right_distance": dist2,
        "motion_detected": motion,
        "timestamp": time.time(),
        "change_left": change1,
        "change_right": change2
    }
    
    # Update OLED display
    oled.fill(0)
    oled.text("LIFE RADAR", 0, 0)
    oled.text("Left : " + dist1, 0, 20)
    oled.text("Right: " + dist2, 0, 35)
    
    if motion:
        green.on()
        red.off()
        oled.text("MOTION DETECT", 0, 55)
    else:
        green.off()
        red.on()
        oled.text("NO MOTION", 0, 55)
    
    oled.show()
    
    print("Left:", dist1, "Right:", dist2, "Motion:", motion)
    
    last1 = r1
    last2 = r2

# ============ Startup Sequence ============
print("\n=== ESP32 DOPPLER RADAR SYSTEM ===\n")

# Startup Screen
oled.fill(0)
oled.text("DUAL LIFE RADAR", 0, 15)
oled.text("System Booting", 0, 35)
oled.show()
time.sleep(2)

# Calibration
oled.fill(0)
oled.text("Calibrating...", 10, 30)
oled.show()

for i in range(50):
    baseline1 += radar1.read()
    baseline2 += radar2.read()
    time.sleep(0.02)

baseline1 = baseline1 // 50
baseline2 = baseline2 // 50
last1 = baseline1
last2 = baseline2

oled.fill(0)
oled.text("Calibration OK", 0, 30)
oled.show()
time.sleep(2)

# Setup WiFi & API Server
wifi_ap = setup_wifi()
server = start_server()

# ============ Main Loop ============
print("\n=== System Ready ===")
print("Connect Mobile App to WiFi:", SSID)
print("Mobile API URL: http://" + wifi_ap.ifconfig()[0] + "/data")
print("\n")

last_update = time.ticks_ms()

while True:
    # Update hardware sensors every 200ms
    if time.ticks_diff(time.ticks_ms(), last_update) > 200:
        update_sensor_data()
        last_update = time.ticks_ms()
    
    # Listen for mobile app HTTP calls
    try:
        client, addr = server.accept()
        handle_client(client)
    except OSError:
        # Ignore, this means no client is currently making an HTTP request
        pass
    except Exception as e:
        print("Server listener error:", e)
    
    time.sleep(0.01)  # Keep loop breathing properly