package com.hope_finder.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hope_finder.data.model.RescueReport
import com.hope_finder.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    val uiState: StateFlow<ReportUiState> = repository.getReports()
        .map { reports ->
            ReportUiState(reports = reports)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportUiState(isLoading = true)
        )

    fun exportReport(reportId: String) {
        // Mock export logic
        viewModelScope.launch {
            // Logic to generate PDF/CSV and save or share
        }
    }
}

data class ReportUiState(
    val reports: List<RescueReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
