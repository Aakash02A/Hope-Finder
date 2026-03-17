package com.hope_finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.RadarProbeData
import com.hope_finder.data.model.SystemAlert
import com.hope_finder.data.model.RescueReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface RadarRepository {
    fun getRadarData(probeId: String): Flow<RadarProbeData?>
    suspend fun updateRadarData(probeId: String, data: RadarProbeData)
    suspend fun triggerAlert(alert: SystemAlert)
    suspend fun saveReport(report: RescueReport)
}

@Singleton
class RadarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RadarRepository {
    override fun getRadarData(probeId: String): Flow<RadarProbeData?> = callbackFlow {
        val subscription = firestore.collection("radar_scans")
            .document(probeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val data = snapshot?.toObject(RadarProbeData::class.java)
                trySend(data)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateRadarData(probeId: String, data: RadarProbeData) {
        firestore.collection("radar_scans").document(probeId).set(data).await()
    }

    override suspend fun triggerAlert(alert: SystemAlert) {
        val id = if (alert.id.isEmpty()) UUID.randomUUID().toString() else alert.id
        firestore.collection("alerts").document(id).set(alert.copy(id = id)).await()
    }

    override suspend fun saveReport(report: RescueReport) {
        val id = if (report.id.isEmpty()) UUID.randomUUID().toString() else report.id
        firestore.collection("reports").document(id).set(report.copy(id = id)).await()
    }
}
