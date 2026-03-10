package com.tryout.hopefinder.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tryout.hopefinder.domain.model.User
import com.tryout.hopefinder.domain.model.UserRole
import com.tryout.hopefinder.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Fetch user data from Firestore
                firestore.collection(USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)?.copy(
                            id = firebaseUser.uid,
                            email = firebaseUser.email ?: ""
                        )
                        trySend(user)
                    }
                    .addOnFailureListener {
                        // Return basic user if Firestore fails
                        trySend(
                            User(
                                id = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                displayName = firebaseUser.displayName ?: "",
                                role = UserRole.RESCUE_OPERATOR
                            )
                        )
                    }
            } else {
                trySend(null)
            }
        }
        
        firebaseAuth.addAuthStateListener(listener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Authentication failed"))
            
            // Get user data from Firestore
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java)?.copy(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: ""
            ) ?: User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                role = UserRole.RESCUE_OPERATOR
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun register(
        email: String, 
        password: String, 
        displayName: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Registration failed"))
            
            val user = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = UserRole.RESCUE_OPERATOR,
                createdAt = System.currentTimeMillis()
            )
            
            // Save user to Firestore
            firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    companion object {
        private const val USERS_COLLECTION = "users"
    }
}
