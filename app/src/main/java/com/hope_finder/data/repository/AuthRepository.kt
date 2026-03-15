package com.hope_finder.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?
    
    suspend fun login(email: String, password: String): Result<AuthResult>
    suspend fun register(email: String, password: String): Result<AuthResult>
    suspend fun forgotPassword(email: String): Result<Unit>
    fun logout()
}
