package com.equationl.manhourslog.ui.view.list.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowRange
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowType
import com.equationl.manhourslog.ui.view.list.state.StatisticsState
import com.equationl.manhourslog.util.DateTimeUtil
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.equationl.manhourslog.util.DateTimeUtil.toTimestamp
import com.equationl.manhourslog.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bytebeats.views.charts.bar.BarChartData
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
        viewModelScope.launch {
            loadData()
        }
    }

    fun changeShowScale(newScale: StatisticsShowScale, newRange: StatisticsShowRange?) {
        Log.w("el", "changeShowScale: scale = $newScale, range = $newRange", )


        _uiState.update {
            it.copy(
                showScale = newScale,
                showRange = newRange ?: it.showRange
            )
        }
        viewModelScope.launch {
            loadData()
        }
    }

    fun onFilterShowRange(value: StatisticsShowRange) {
        _uiState.update {
            it.copy(
                showRange = value
            )
        }
        viewModelScope.launch {
            loadData()
        }
    }

    fun onChangeShowType() {
        // TODO
        _uiState.update {
            it.copy(
                showType = if (it.showType == StatisticsShowType.Chart) StatisticsShowType.List else StatisticsShowType.Chart,
                showScale = if (it.showType == StatisticsShowType.List) StatisticsShowScale.Year else it.showScale
            )
        }

        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {

        _uiState.update { it.copy(isLoading = true) }

        val rawDataList = db.manHoursDB().queryRangeDataList(_uiState.value.showRange.start, _uiState.value.showRange.end, 1, Int.MAX_VALUE)

        Log.w("el", "loadData: rawData = $rawDataList")

        val resolveResult = resolveData(rawDataList)
        val barChartDataList = arrayListOf<BarChartData.Bar>()

        if (_uiState.value.showType == StatisticsShowType.Chart) {
            for (item in resolveResult) {
                val newBar = BarChartData.Bar(
                    label = "${item.startTime.formatDateTime("yyyy-MM")}(${item.totalTime.formatTime()})",
                    value = (item.totalTime / DateTimeUtil.HOUR_MILL_SECOND_TIME).toFloat(),
                    color = Utils.randomColor
                )
                barChartDataList.add(newBar)
            }
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                dataList = resolveResult,
                barChartData = barChartDataList
            )
        }
    }

    private fun resolveData(rawDataList: List<DBManHoursTable>): List<StaticsScreenModel> {
        val daySum = mutableMapOf<String, Long>()
        val monthSum = mutableMapOf<String, Long>()
        val yearSum = mutableMapOf<String, Long>()

        for (item in rawDataList) {
            val key = item.startTime.formatDateTime(format = "yyyy-MM-dd")
            daySum[key] = (daySum[key] ?: 0L) + item.totalTime
        }

        if (_uiState.value.showScale == StatisticsShowScale.Month || _uiState.value.showScale == StatisticsShowScale.Year) {
            daySum.forEach { (t, u) ->
                val split = t.split("-")
                val key = "${split[0]}-${split[1]}"

                monthSum[key] = (monthSum[key] ?: 0L) + u
            }

            Log.w("el", "resolveData: monthSum = $monthSum")
        }

        if (_uiState.value.showScale == StatisticsShowScale.Year) {
            monthSum.forEach { (t, u) ->
                val split = t.split("-")
                val key = split[0]

                yearSum[key] = (yearSum[key] ?: 0L) + u
            }

            Log.w("el", "resolveData: yearSum = $yearSum")
        }

        when (_uiState.value.showScale) {
            StatisticsShowScale.Year -> {
                val tempMap = mutableMapOf<String, DBManHoursTable>()
                for (item in rawDataList) {
                    val tempKey = item.startTime.formatDateTime("yyyy")
                    tempMap[tempKey] = item
                }

                return tempMap.map {
                    val key = it.value.startTime.formatDateTime("yyyy")
                    StaticsScreenModel(
                        id = it.value.id,
                        startTime = it.value.startTime.formatDateTime("yyyy-MM").toTimestamp("yyyy-MM"),
                        endTime = 0L,
                        totalTime = monthSum[it.value.startTime.formatDateTime("yyyy-MM")] ?: 0L,
                        headerTitle = key,
                        headerTotalTime = yearSum[key] ?: 0L
                    )
                }
            }
            StatisticsShowScale.Month -> {
                val tempMap = mutableMapOf<String, DBManHoursTable>()
                for (item in rawDataList) {
                    val tempKey = item.startTime.formatDateTime("yyyy-MM-dd")
                    tempMap[tempKey] = item
                }

                return tempMap.map {
                    val key = it.value.startTime.formatDateTime("yyyy-MM")
                    StaticsScreenModel(
                        id = it.value.id,
                        startTime = it.value.startTime.formatDateTime("yyyy-MM-dd").toTimestamp("yyyy-MM-dd"),
                        endTime = 0L,
                        totalTime = daySum[it.value.startTime.formatDateTime("yyyy-MM-dd")] ?: 0L,
                        headerTitle = key,
                        headerTotalTime = monthSum[key] ?: 0L
                    )
                }
            }
            StatisticsShowScale.Day -> {
                return rawDataList.map {
                    val key = it.startTime.formatDateTime("yyyy-MM-dd")
                    StaticsScreenModel(
                        id = it.id,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        totalTime = it.totalTime,
                        headerTitle = key,
                        headerTotalTime = daySum[key] ?: 0L
                    )
                }
            }
        }
    }
}