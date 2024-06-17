package com.equationl.manhourslog.ui.view.list.state

import com.equationl.manhourslog.database.DBManHoursTable

data class StatisticsState(
    val isLoading: Boolean = true,
    val showType: StatisticsShowType = StatisticsShowType.Day,
    val dataList: List<DBManHoursTable> = listOf()
)

enum class StatisticsShowType {
    Month,
    Day,
    Detail
}
