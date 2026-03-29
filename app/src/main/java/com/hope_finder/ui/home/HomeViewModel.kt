package com.hope_finder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hope_finder.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
        startDataSimulation()
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

    private fun startDataSimulation() {
        viewModelScope.launch {
            while (true) {
                // Only simulate if Firebase data is empty or as a fallback
                if (_uiState.value.radarCells.isEmpty()) {
                    simulateData()
                }
                delay(5000)
            }
        }
    }

    private fun simulateData() {
        val mockStats = DashboardStats(
            probesOnline = Random.nextInt(5, 12),
            activeScanZones = Random.nextInt(2, 6),
            lifeSignalsDetected = Random.nextInt(0, 4),
            rescueAlerts = Random.nextInt(0, 3)
        )

        val mockProbes = listOf(
            ConnectedProbe("p1", "PROBE-ALPHA", Random.nextInt(10, 100), "Online", 24.5f, 85, true),
            ConnectedProbe("p2", "PROBE-BRAVO", Random.nextInt(10, 100), "Busy", 26.1f, 72),
            ConnectedProbe("p3", "PROBE-CHARLIE", Random.nextInt(10, 100), "Offline", 22.0f, 0)
        )

        val mockSignals = listOf(
            LifeSignal("s1", "Sector 4-B", 87, System.currentTimeMillis()),
            LifeSignal("s2", "Sector 2-A", 62, System.currentTimeMillis() - 60000)
        )

        val mockAlerts = if (mockStats.rescueAlerts > 0) listOf(
            SystemAlert("a1", "Life Signal", "Life signal detected at Sector 4-B", "High", System.currentTimeMillis())
        ) else emptyList()

        _uiState.value = _uiState.value.copy(
            stats = mockStats,
            probes = mockProbes,
            lifeSignals = mockSignals,
            alerts = mockAlerts,
            radarCells = (0..10).map { i -> 
                RadarCell("c$i", "Zone $i", if(i%3==0) "Scanning" else "Active", "MN-01", listOf("p1")) 
            }
        )
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
