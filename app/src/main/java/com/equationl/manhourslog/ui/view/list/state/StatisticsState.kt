package com.equationl.manhourslog.ui.view.list.state

import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.util.DateTimeUtil

data class StatisticsState(
    val isLoading: Boolean = true,
    val showRange: StatisticsShowRange = StatisticsShowRange(
        // 默认当前月
        start = DateTimeUtil.getWeeOfCurrentMonth(),
        end = DateTimeUtil.getCurrentMonthEnd(),
    ),
    val showType: StatisticsShowType = StatisticsShowType.List,
    val showScale: StatisticsShowScale = StatisticsShowScale.Day,
    val dataList: List<StaticsScreenModel> = listOf(),
)

data class StatisticsShowRange(
    val start: Long = 0L,
    val end: Long = 0L
)

enum class StatisticsShowType {
    List,
    Chart
}

enum class StatisticsShowScale {
    Year,
    Month,
    Day,
}

fun StatisticsShowScale.getNext(): StatisticsShowScale {
    return when (this) {
        StatisticsShowScale.Year -> StatisticsShowScale.Month
        StatisticsShowScale.Month -> StatisticsShowScale.Day
        StatisticsShowScale.Day -> StatisticsShowScale.Year
    }
}
