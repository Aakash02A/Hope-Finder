package com.tryout.hopefinder.data

import android.content.Context
import android.os.Build

object DevicePreferences {
    private const val PREFS_NAME = "hope_finder_device_prefs"
    private const val KEY_DEVICE_IP = "device_ip"

    fun getSavedDeviceIp(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DEVICE_IP, null)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    fun saveDeviceIp(context: Context, deviceIp: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DEVICE_IP, deviceIp.trim())
            .apply()
    }

    fun resolveDefaultDeviceIp(): String {
        return if (isRunningInEmulator()) {
            "10.0.2.2"
        } else {
            "192.168.1.100"
        }
    }

    private fun isRunningInEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") ||
            Build.FINGERPRINT.contains("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.DEVICE.contains("emulator") ||
            (Build.BRAND == "generic" && Build.DEVICE == "generic")
    }
}