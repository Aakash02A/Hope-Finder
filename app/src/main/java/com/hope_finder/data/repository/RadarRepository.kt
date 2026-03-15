package com.hope_finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.RadarProbeData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface RadarRepository {
    fun getRadarData(probeId: String): Flow<RadarProbeData?>
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
}
