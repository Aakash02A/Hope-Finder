package com.hope_finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.Alert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AlertRepository {
    fun getAlerts(): Flow<List<Alert>>
    suspend fun resolveAlert(alertId: String)
}

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlertRepository {
    override fun getAlerts(): Flow<List<Alert>> = callbackFlow {
        val subscription = firestore.collection("alerts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjects(Alert::class.java) ?: emptyList()
                trySend(alerts)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun resolveAlert(alertId: String) {
        firestore.collection("alerts").document(alertId)
            .update("isResolved", true)
    }
}
