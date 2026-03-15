package com.hope_finder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.Probe
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ProbeRepository {
    fun getAllProbes(): Flow<List<Probe>>
}

@Singleton
class ProbeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProbeRepository {
    override fun getAllProbes(): Flow<List<Probe>> = callbackFlow {
        val subscription = firestore.collection("probes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val probes = snapshot?.toObjects(Probe::class.java) ?: emptyList()
                trySend(probes)
            }
        awaitClose { subscription.remove() }
    }
}
