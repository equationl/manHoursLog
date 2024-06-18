package com.equationl.manhourslog.ui.view.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.paging.ManHoursPagingSource
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowRange
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.list.state.StatisticsState
import com.equationl.manhourslog.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    private val _uiState = MutableStateFlow(
        StatisticsState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun changeShowScale(newScale: StatisticsShowScale) {
        // TODO
        _uiState.update {
            it.copy(
                showScale = newScale
            )
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        // 默认展示当月
        val showRange = StatisticsShowRange(
            start = DateTimeUtil.getWeeOfCurrentMonth(),
            end = DateTimeUtil.getCurrentMonthEnd(),
        )

        val dataFlow =
            Pager(
                PagingConfig(pageSize = 50, initialLoadSize = 50)
            ) {
                ManHoursPagingSource(
                    api = db,
                    startTime = showRange.start,
                    endTime = showRange.end
                )
            }.flow.cachedIn(viewModelScope)

        _uiState.update {
            it.copy(
                isLoading = false,
                showRange = showRange,
                dataFlow = dataFlow
            )
        }
    }
}