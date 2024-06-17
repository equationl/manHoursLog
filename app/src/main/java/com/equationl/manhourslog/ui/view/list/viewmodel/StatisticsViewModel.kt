package com.equationl.manhourslog.ui.view.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.ui.view.list.state.StatisticsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val db: ManHoursDB
): ViewModel() {
    private val _uiState = MutableStateFlow(
        StatisticsState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // TODO
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}