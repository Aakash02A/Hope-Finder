package com.hope_finder.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.model.RadarProbeData
import com.hope_finder.data.repository.RadarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadarViewModel @Inject constructor(
    private val repository: RadarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    fun startScanning(probeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getRadarData(probeId).collectLatest { data ->
                if (data != null) {
                    _uiState.value = _uiState.value.copy(
                        radarData = data,
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class RadarUiState(
    val radarData: RadarProbeData = RadarProbeData(),
    val isLoading: Boolean = false
)
