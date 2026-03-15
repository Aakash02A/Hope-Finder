package com.hope_finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.RescueReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ReportRepository {
    fun getReports(): Flow<List<RescueReport>>
    suspend fun getReportById(id: String): RescueReport?
}

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportRepository {
    override fun getReports(): Flow<List<RescueReport>> = callbackFlow {
        val subscription = firestore.collection("reports")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.toObjects(RescueReport::class.java) ?: emptyList()
                trySend(reports)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getReportById(id: String): RescueReport? {
        return try {
            firestore.collection("reports").document(id).get().await().toObject(RescueReport::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
