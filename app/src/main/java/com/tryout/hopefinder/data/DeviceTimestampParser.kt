package com.tryout.hopefinder.data

import java.time.Instant
import java.time.OffsetDateTime

fun parseDeviceTimestamp(timestamp: String?, fallback: Long = System.currentTimeMillis()): Long {
    val cleanedTimestamp = timestamp?.trim().orEmpty()
    if (cleanedTimestamp.isEmpty()) {
        return fallback
    }

    cleanedTimestamp.toLongOrNull()?.let { return it }

    return try {
        Instant.parse(cleanedTimestamp).toEpochMilli()
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(cleanedTimestamp).toInstant().toEpochMilli()
        } catch (_: Exception) {
            fallback
        }
    }
}