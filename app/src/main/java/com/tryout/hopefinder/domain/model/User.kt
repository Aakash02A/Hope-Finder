package com.tryout.hopefinder.domain.model

/**
 * Represents a user in the Rescue Radar System.
 */
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.RESCUE_OPERATOR,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User roles for Role-Based Access Control (RBAC).
 */
enum class UserRole {
    ADMIN,          // Full access including reports & analytics
    RESCUE_OPERATOR // Can run scans and view real-time alerts
}
