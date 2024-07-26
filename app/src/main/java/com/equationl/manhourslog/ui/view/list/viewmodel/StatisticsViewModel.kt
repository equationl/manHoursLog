package com.equationl.manhourslog.ui.view.list.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.equationl.manhourslog.util.ResolveDataUtil
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

    fun onChangeNote(value: String, id: Int) {
        viewModelScope.launch {
            db.manHoursDB().updateNoteById(id, value)
            _uiState.update { state ->
                val newList = arrayListOf<StaticsScreenModel>()
                newList.addAll(state.dataList)
                val editValue = newList.find { it.id == id }
                if (editValue != null) {
                    editValue.note = value
                }
                state.copy(dataList = newList)
            }
        }
    }

    fun onClickDeleteItem(snackBarHostState: SnackbarHostState, id: Int) {
        viewModelScope.launch {
            val result = db.manHoursDB().markDeleteRowById(id)
            if (result == 1) {
                val removeIndex = _uiState.value.dataList.indexOfFirst { it.id == id }
                val removeItem = _uiState.value.dataList[removeIndex]

                _uiState.update { state ->
                    val newList = arrayListOf<StaticsScreenModel>()
                    newList.addAll(state.dataList)
                    newList.removeAt(removeIndex)
                    state.copy(dataList = newList)
                }

                val snackBarResult = snackBarHostState.showSnackbar(
                    message = "Row already delete",
                    actionLabel = "UNDO",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )

                if (snackBarResult == SnackbarResult.ActionPerformed) {
                    val recoverResult = db.manHoursDB().markDeleteRowById(id, delete = 0)
                    if (recoverResult == 1) {
                        _uiState.update { state ->
                            val newList = arrayListOf<StaticsScreenModel>()
                            newList.addAll(state.dataList)
                            newList.add(removeIndex, removeItem)
                            state.copy(dataList = newList)
                        }
                    }
                    else {
                        snackBarHostState.showSnackbar(message = "UNDO fail!")
                    }
                }
            }
            else {
                snackBarHostState.showSnackbar(message = "Delete fail!")
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

        var hasConflict = false

        uri?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val buffer = context.contentResolver.openInputStream(it)?.bufferedReader()
                buffer?.useLines {
                    hasConflict = ResolveDataUtil.importFromCsv(context, it, db)
                }

                withContext(Dispatchers.Main) {
                    if (hasConflict) {
                        Toast.makeText(context, "Import Finish, But some row not insert, Maybe it's because of duplicate data", Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(context, "Import Finish", Toast.LENGTH_SHORT).show()
                    }
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
                                outputStream.write(ResolveDataUtil.getCsvRow(_uiState.value.showScale, model).toByteArray())
                            }
                            StatisticsShowScale.Month -> {
                                if (index == 0) {
                                    outputStream.write(ExportHeader.MONTH.toByteArray())
                                }
                                outputStream.write(ResolveDataUtil.getCsvRow(_uiState.value.showScale, model).toByteArray())
                            }
                            StatisticsShowScale.Day -> {
                                if (index == 0) {
                                    outputStream.write(ExportHeader.DAY.toByteArray())
                                }
                                outputStream.write(ResolveDataUtil.getCsvRow(_uiState.value.showScale, model).toByteArray())
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
        return ResolveDataUtil.rawDataToStaticsModel(rawDataList, _uiState.value.showScale)
    }
}