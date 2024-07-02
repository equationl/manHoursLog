package com.equationl.manhourslog.ui.view.list.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.constants.ExportHeader
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
import com.equationl.manhourslog.util.DateTimeUtil.timeToTimeStamp
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

    fun onClickDeleteItem(id: Int) {
        viewModelScope.launch {
            db.manHoursDB().markDeleteRowById(id)
            // loadData()
            _uiState.update { state ->
                val newList = arrayListOf<StaticsScreenModel>()
                newList.addAll(state.dataList)
                newList.removeIf { it.id == id }
                state.copy(dataList = newList)
            }
        }
    }


    fun changeShowScale(newScale: StatisticsShowScale, newRange: StatisticsShowRange?) {
        Log.w("el", "changeShowScale: scale = $newScale, range = $newRange")


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

    fun onImport(result: ActivityResult, context: Context) {
        val data = result.data
        val uri = data?.data

        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                var isHeader = true
                val buffer = context.contentResolver.openInputStream(it)?.bufferedReader()
                buffer?.useLines {
                    for (line in it) {
                        //Log.i("el", "onImport: line = .$line.")
                        if (isHeader) {
                            if (line != ExportHeader.DAY.replace("\n", "")) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Only support import day's detail .csv file", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }
                            isHeader = false
                        }
                        else {
                            try {
                                val lineSplit = line.split(",")
                                val insertData = DBManHoursTable(
                                    startTime = lineSplit[0].toTimestamp(),
                                    endTime = lineSplit[1].toTimestamp(),
                                    totalTime = lineSplit[2].timeToTimeStamp()
                                )

                                // FIXME 没有处理重复数据
                                val insertResult = db.manHoursDB().insertData(insertData)
                                if (insertResult <= 0) {
                                    Log.w("el", "onImport: insetData = $insertData, result = $insertResult")
                                }
                            } catch (tr: Throwable) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Import fail: $tr", Toast.LENGTH_SHORT).show()
                                }
                                Log.e("el", "onImport: ", tr)
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Import Finish", Toast.LENGTH_SHORT).show()
                }

                loadData()
            }
        }
    }

    fun onExport(result: ActivityResult, context: Context) {
        val data = result.data
        val uri = data?.data
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let {outputStream ->
                viewModelScope.launch(Dispatchers.IO) {
                    _uiState.value.dataList.forEachIndexed { index, model ->
                        when (_uiState.value.showScale) {
                            StatisticsShowScale.Year -> {
                                if (index == 0) {
                                    outputStream.write(ExportHeader.YEAR.toByteArray())
                                }
                                outputStream.write("${model.startTime.formatDateTime("yyyy-MM")},${model.totalTime.formatTime()}\n".toByteArray())
                            }
                            StatisticsShowScale.Month -> {
                                if (index == 0) {
                                    outputStream.write(ExportHeader.MONTH.toByteArray())
                                }
                                outputStream.write("${model.startTime.formatDateTime("yyyy-MM-dd")},${model.totalTime.formatTime()}\n".toByteArray())
                            }
                            StatisticsShowScale.Day -> {
                                if (index == 0) {
                                    outputStream.write(ExportHeader.DAY.toByteArray())
                                }
                                outputStream.write("${model.startTime.formatDateTime()},${model.endTime.formatDateTime()},${model.totalTime.formatTime()}\n".toByteArray())
                            }
                        }
                    }

                    outputStream.flush()
                    outputStream.close()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export finish", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun onChangeShowType(context: Context) {
        _uiState.update {
            it.copy(
                showType = if (it.showType == StatisticsShowType.Chart) StatisticsShowType.List else StatisticsShowType.Chart,
                showScale = if (it.showType == StatisticsShowType.List) StatisticsShowScale.Year else it.showScale
            )
        }

        if (_uiState.value.showType == StatisticsShowType.Chart) {
            Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        }
        else {
            Utils.changeScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }

        viewModelScope.launch {
            loadData()
        }
    }

    fun createNewDocumentIntent(): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_TITLE, "manHoursLog_${System.currentTimeMillis().formatDateTime("yyyy_MM_dd_HH_mm_ss")}.csv")
        }
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        return intent
    }

    fun createReadDocumentIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/comma-separated-values"

            // putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        return intent
    }

    private suspend fun loadData() = withContext(Dispatchers.IO) {

        _uiState.update { it.copy(isLoading = true) }

        val rawDataList = db.manHoursDB().queryRangeDataList(_uiState.value.showRange.start, _uiState.value.showRange.end, 1, Int.MAX_VALUE)

        Log.w("el", "loadData(${_uiState.value.showRange.start}, ${_uiState.value.showRange.end}, 1, ${Int.MAX_VALUE}): rawData = $rawDataList")

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
                    val tempKey = item.startTime.formatDateTime("yyyy-MM")
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