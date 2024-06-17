package com.equationl.manhourslog.ui.view.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.ui.view.home.state.HomeState
import com.equationl.manhourslog.ui.view.home.state.LogState
import com.equationl.manhourslog.util.datastore.DataKey
import com.equationl.manhourslog.util.datastore.DataStoreUtils
import com.equationl.manhourslog.util.fromJson
import com.equationl.manhourslog.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val db: ManHoursDB
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun toggleStart() {
        _uiState.update {
            if (it.logState.isStart) {
                val newLogState = LogState(isStart = false)
                DataStoreUtils.saveSyncStringData(DataKey.LOG_STATE, newLogState.toJson())

                // 插入数据到数据库
                viewModelScope.launch {
                    val currentTime = System.currentTimeMillis()
                    db.manHoursDB().insertData(
                        DBManHoursTable(
                            startTime = it.logState.startTime ?: 0L,
                            endTime = currentTime,
                            totalTime = currentTime - (it.logState.startTime ?: 0L)
                        )
                    )

                    updateTodayManHours()
                }

                it.copy(logState = newLogState)
            }
            else {
                val newLogState = LogState(isStart = true, startTime = System.currentTimeMillis())
                DataStoreUtils.saveSyncStringData(DataKey.LOG_STATE, newLogState.toJson())
                it.copy(logState = newLogState)
            }
        }
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            updateTodayManHours()
            val logState = DataStoreUtils.getData(DataKey.LOG_STATE, "").first().fromJson<LogState>() ?: LogState(isStart = false)
            _uiState.update { it.copy(isLoading = false, logState = logState) }
        }
    }

    private fun updateTodayManHours() {
        viewModelScope.launch {
            var d: Long = Date().time
            val offset: Int = TimeZone.getDefault().getOffset(d)
            d = ((d + offset) / 86400000L) * 86400000L - offset

            val totalManHours = db.manHoursDB().queryTodayTotalTime(startTime = d, endTime = d + 86400000L)

            _uiState.update { it.copy(totalManHours = totalManHours) }
        }
    }

}