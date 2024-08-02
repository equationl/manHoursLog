package com.equationl.manhourslog.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.equationl.manhourslog.constants.ExportHeader
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.equationl.manhourslog.util.DateTimeUtil.timeToTimeStamp
import com.equationl.manhourslog.util.DateTimeUtil.toTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ResolveDataUtil {
    fun rawDataToStaticsModel(
        rawDataList: List<DBManHoursTable>,
        showScale: StatisticsShowScale,
    ): List<StaticsScreenModel> {
        val daySum = mutableMapOf<String, Long>()
        val monthSum = mutableMapOf<String, Long>()
        val yearSum = mutableMapOf<String, Long>()

        for (item in rawDataList) {
            val key = item.startTime.formatDateTime(format = "yyyy-MM-dd")
            daySum[key] = (daySum[key] ?: 0L) + item.totalTime
        }

        if (showScale == StatisticsShowScale.Month || showScale == StatisticsShowScale.Year) {
            daySum.forEach { (t, u) ->
                val split = t.split("-")
                val key = "${split[0]}-${split[1]}"

                monthSum[key] = (monthSum[key] ?: 0L) + u
            }

            Log.w("el", "resolveData: monthSum = $monthSum")
        }

        if (showScale == StatisticsShowScale.Year) {
            monthSum.forEach { (t, u) ->
                val split = t.split("-")
                val key = split[0]

                yearSum[key] = (yearSum[key] ?: 0L) + u
            }

            Log.w("el", "resolveData: yearSum = $yearSum")
        }

        when (showScale) {
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
                        headerTotalTime = yearSum[key] ?: 0L,
                        note = null,
                        dataSourceType = null
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
                        headerTotalTime = monthSum[key] ?: 0L,
                        note = null,
                        dataSourceType = null
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
                        headerTotalTime = daySum[key] ?: 0L,
                        note = it.noteText,
                        dataSourceType = it.dataSourceType
                    )
                }
            }
        }
    }

    fun getCsvRow(
        showScale: StatisticsShowScale,
        model: StaticsScreenModel
    ): String {
        return when (showScale) {
            StatisticsShowScale.Year -> {
                "${model.startTime.formatDateTime("yyyy-MM")},${model.totalTime.formatTime()}\n"
            }

            StatisticsShowScale.Month -> {
                "${model.startTime.formatDateTime("yyyy-MM-dd")},${model.totalTime.formatTime()}\n"
            }

            StatisticsShowScale.Day -> {
                "${model.startTime.formatDateTime()},${model.endTime.formatDateTime()},${model.totalTime.formatTime()},${model.note ?: ""},${model.startTime}\n"
            }
        }
    }

    /**
     * 将 csv 数据导入数据库
     *
     * @return 是否导入成功，返回 false 表示有部分数据导入失败，此时可能已经有数据导入成功
     * */
    suspend fun importFromCsv(
        context: Context,
        csvLines: Sequence<String>,
        db: ManHoursDB
    ): Boolean {
        var isHeader = true
        var hasConflict = false

        for (line in csvLines) {
            //Log.i("el", "onImport: line = .$line.")
            if (line.isBlank()) {
                Log.w("el", "importFromCsv: line is blank", )
                continue
            }
            if (isHeader) {
                // 这里为了兼容旧版本格式改为使用字段数量判断而不是直接匹配列表头
                if (line.split(",").size < 3) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Only support import day's detail .csv file", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
                isHeader = false
            }
            else {
                try {
                    val lineSplit = line.split(",")
                    // 如果存在时间戳则使用时间戳，否则重新格式化 start_time 。 但是只有存在时间戳才能完成重复判定
                    val startTime = lineSplit.getOrNull(4)?.toLongOrNull() ?: lineSplit[0].toTimestamp()
                    val insertData = DBManHoursTable(
                        startTime = startTime,
                        endTime = lineSplit[1].toTimestamp(),
                        totalTime = lineSplit[2].timeToTimeStamp(),
                        noteText = lineSplit.getOrNull(3), // 兼容旧版本，最后一列可能为空
                        dataSourceType = 1
                    )

                    val insertResult = db.manHoursDB().insertData(insertData)
                    if (insertResult <= 0) { // 插入失败可能是由于重复数据导致（start_time 设置为了唯一键）
                        val startTimeStamp = lineSplit.getOrNull(4)?.toLongOrNull()
                        if (startTimeStamp != null) {
                            // 由于删除数据使用的是软删除，所以还需要判断是否是被软删除了，如果是的话需要恢复数据（这里无法兼容旧数据，如果是旧数据没有时间戳的话无法恢复数据）
                            val tempRow = db.manHoursDB().queryRowByStartTime(startTimeStamp)
                            if (tempRow?.isDelete == true) {
                                val updateResult = db.manHoursDB().markDeleteAndTypeRowByStartTime(startTimeStamp, delete = 0, dataType = 1)
                                if (updateResult <= 0) {
                                    Log.w("el", "onImport: updateDeleteFlag fail: startTime = $startTimeStamp")
                                }
                            }

                            val noteText = lineSplit.getOrNull(3)
                            if (!noteText.isNullOrBlank()) { // 如果备注不为空，则认为是更新备注
                                val updateResult = db.manHoursDB().updateNoteByStartTime(startTimeStamp, noteText)
                                if (updateResult <= 0) {
                                    hasConflict = true
                                    Log.w("el", "onImport: updateNote fail: startTime = ${startTimeStamp}, note = $noteText")
                                }
                            }
                        }
                        else {
                            hasConflict = true
                            Log.w("el", "onImport: insetData = $insertData, result = $insertResult")
                        }
                    }
                } catch (tr: Throwable) {
                    Log.e("el", "onImport: $line", tr)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Import fail: $tr", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return hasConflict
    }


    /** 为同步准备数据 */
    suspend fun prepareDataForSync(
        db: ManHoursDB
    ): ByteArray = withContext(Dispatchers.IO) {
        val rawDataList = db.manHoursDB().queryRangeDataList(0, System.currentTimeMillis(), 1, Int.MAX_VALUE)
        val dataModel = rawDataToStaticsModel(rawDataList, StatisticsShowScale.Day)
        var dataText = SocketConstant.SYNC_DATA_HEADER
        dataModel.forEachIndexed { index, model ->
            if (index == 0) {
                dataText += ExportHeader.DAY
            }
            dataText += getCsvRow(StatisticsShowScale.Day, model)
        }

        val resultByteArray = mutableListOf<Byte>()
        resultByteArray.addAll(dataText.toByteArray().toList())
        resultByteArray.addAll(SocketConstant.END_FLAG.toList())

        resultByteArray.toByteArray()
    }
}