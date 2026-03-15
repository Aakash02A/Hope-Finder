package com.hope_finder.ui.probe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.model.Probe
import com.hope_finder.data.repository.ProbeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProbeStatusViewModel @Inject constructor(
    private val repository: ProbeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<ProbeUiState> = combine(
        repository.getAllProbes(),
        _searchQuery
    ) { probes, query ->
        val filteredProbes = if (query.isBlank()) {
            probes
        } else {
            probes.filter { it.id.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true) }
        }
        ProbeUiState(probes = filteredProbes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProbeUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }
}

data class ProbeUiState(
    val probes: List<Probe> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
