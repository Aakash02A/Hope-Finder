import network
import time

from config import WIFI_PASSWORD, WIFI_SSID


def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if wlan.isconnected():
        print("[WiFi] Already connected:", wlan.ifconfig())
        return wlan

    print("[WiFi] Connecting to %s..." % WIFI_SSID)
    wlan.connect(WIFI_SSID, WIFI_PASSWORD)

    timeout = 20
    while timeout > 0 and not wlan.isconnected():
        time.sleep(1)
        timeout -= 1
        print("[WiFi] Waiting for connection...")

    if wlan.isconnected():
        print("[WiFi] Connected")
        print("[WiFi] Network config:", wlan.ifconfig())
    else:
        print("[WiFi] Connection failed")

    return wlan


wlan = connect_wifi()
