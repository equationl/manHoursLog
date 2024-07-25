package com.equationl.manhourslog.util

import android.util.Log
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.equationl.manhourslog.util.DateTimeUtil.toTimestamp

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
}