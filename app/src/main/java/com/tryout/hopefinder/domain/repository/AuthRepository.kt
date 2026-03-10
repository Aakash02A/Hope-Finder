package com.tryout.hopefinder.domain.repository

import com.tryout.hopefinder.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 */
interface AuthRepository {
    
    /**
     * Get current authenticated user.
     * @return Flow of User or null if not authenticated.
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Sign in with email and password.
     * @param email User's email address.
     * @param password User's password.
     * @return Result containing User on success or exception on failure.
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Sign out the current user.
     */
    suspend fun signOut()
    
    /**
     * Register a new user.
     * @param email User's email address.
     * @param password User's password.
     * @param displayName User's display name.
     * @return Result containing User on success or exception on failure.
     */
    suspend fun register(
        email: String, 
        password: String, 
        displayName: String
    ): Result<User>
    
    /**
     * Check if a user is currently authenticated.
     * @return true if authenticated.
     */
    fun isAuthenticated(): Boolean
}
