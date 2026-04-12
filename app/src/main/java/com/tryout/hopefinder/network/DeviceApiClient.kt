package com.tryout.hopefinder.network

import android.util.Log
import com.google.gson.Gson
import com.tryout.hopefinder.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * HTTP Client for communicating with ESP32 API
 */
class DeviceApiClient(private val baseUrl: String) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Send a detection event to the API
     */
    suspend fun sendDetectionEvent(detection: DetectionResponse): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(detection)
            val body = json.toRequestBody(jsonMediaType)
            
            val request = Request.Builder()
                .url("$baseUrl/detection")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error sending detection", e)
            Result.failure(e)
        }
    }

    /**
     * Get current device status
     */
    suspend fun getDeviceStatus(): Result<DeviceStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/device/status")
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val statusResponse = gson.fromJson(body, DeviceStatusResponse::class.java)
                Result.success(statusResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error getting device status", e)
            Result.failure(e)
        }
    }

    /**
     * Get event history
     */
    suspend fun getEventHistory(
        startTime: Long? = null,
        limit: Int = 100,
        sector: Int? = null,
        minConfidence: Int? = null
    ): Result<EventHistoryResponse> = withContext(Dispatchers.IO) {
        try {
            var url = "$baseUrl/events?limit=$limit"
            startTime?.let { url += "&start=$it" }
            sector?.let { url += "&sector=$it" }
            minConfidence?.let { url += "&min_confidence=$it" }

            val request = Request.Builder()
                .url(url)
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val historyResponse = gson.fromJson(body, EventHistoryResponse::class.java)
                Result.success(historyResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error getting event history", e)
            Result.failure(e)
        }
    }

    /**
     * Start scanning
     */
    suspend fun startScan(durationSeconds: Int = 300): Result<ScanCommandResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val requestBody = mapOf(
                    "duration_seconds" to durationSeconds,
                    "sector_dwell_ms" to 3000,
                    "sensitivity" to "high"
                )
                val json = gson.toJson(requestBody)
                val body = json.toRequestBody(jsonMediaType)

                val request = Request.Builder()
                    .url("$baseUrl/scan/start")
                    .post(body)
                    .build()

                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val scanResponse = gson.fromJson(responseBody, ScanCommandResponse::class.java)
                    Result.success(scanResponse)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e("DeviceApiClient", "Error starting scan", e)
                Result.failure(e)
            }
        }

    /**
     * Stop scanning
     */
    suspend fun stopScan(): Result<ScanCommandResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/scan/stop")
                .post("".toRequestBody())
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val scanResponse = gson.fromJson(body, ScanCommandResponse::class.java)
                Result.success(scanResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error stopping scan", e)
            Result.failure(e)
        }
    }

    /**
     * Start calibration
     */
    suspend fun startCalibration(): Result<ScanCommandResponse> = withContext(Dispatchers.IO) {
        try {
            val requestBody = mapOf(
                "mode" to "quick",
                "duration_seconds" to 60
            )
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("$baseUrl/calibration/start")
                .post(body)
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val calibResponse = gson.fromJson(responseBody, ScanCommandResponse::class.java)
                Result.success(calibResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error starting calibration", e)
            Result.failure(e)
        }
    }

    /**
     * Get health check status
     */
    suspend fun getHealthStatus(): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .build()

            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val healthResponse = gson.fromJson(body, HealthCheckResponse::class.java)
                Result.success(healthResponse)
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("DeviceApiClient", "Error getting health status", e)
            Result.failure(e)
        }
    }

    /**
     * Get session summary
     */
    suspend fun getSessionSummary(startTime: Long, endTime: Long): Result<SessionSummaryResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/session/summary?start=$startTime&end=$endTime")
                    .build()

                val response = httpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val summaryResponse = gson.fromJson(body, SessionSummaryResponse::class.java)
                    Result.success(summaryResponse)
                } else {
                    Result.failure(Exception("HTTP ${response.code}"))
                }
            } catch (e: Exception) {
                Log.e("DeviceApiClient", "Error getting session summary", e)
                Result.failure(e)
            }
        }

    /**
     * Check if device is reachable
     */
    suspend fun isDeviceReachable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .build()

            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: SocketTimeoutException) {
            false
        } catch (e: ConnectException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
