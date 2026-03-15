package com.hope_finder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    private fun observeDashboardData() {
        viewModelScope.launch {
            // Observe Stats
            firestore.collection("stats").document("current")
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.toObject(DashboardStats::class.java)?.let { stats ->
                        _uiState.value = _uiState.value.copy(stats = stats)
                    }
                }

            // Observe Radar Cells
            firestore.collection("radar_cells")
                .addSnapshotListener { snapshot, _ ->
                    val cells = snapshot?.toObjects(RadarCell::class.java) ?: emptyList()
                    _uiState.value = _uiState.value.copy(radarCells = cells)
                }

            // Observe Life Signals
            firestore.collection("life_signals")
                .limit(5)
                .addSnapshotListener { snapshot, _ ->
                    val signals = snapshot?.toObjects(LifeSignal::class.java) ?: emptyList()
                    _uiState.value = _uiState.value.copy(lifeSignals = signals)
                }

            // Observe Probes
            firestore.collection("probes")
                .addSnapshotListener { snapshot, _ ->
                    val probes = snapshot?.toObjects(ConnectedProbe::class.java) ?: emptyList()
                    _uiState.value = _uiState.value.copy(probes = probes)
                }

            // Observe Alerts
            firestore.collection("alerts")
                .limit(5)
                .addSnapshotListener { snapshot, _ ->
                    val alerts = snapshot?.toObjects(SystemAlert::class.java) ?: emptyList()
                    _uiState.value = _uiState.value.copy(alerts = alerts)
                }
        }
    }
}

data class HomeUiState(
    val stats: DashboardStats = DashboardStats(),
    val radarCells: List<RadarCell> = emptyList(),
    val lifeSignals: List<LifeSignal> = emptyList(),
    val probes: List<ConnectedProbe> = emptyList(),
    val alerts: List<SystemAlert> = emptyList(),
    val isLoading: Boolean = false
)
