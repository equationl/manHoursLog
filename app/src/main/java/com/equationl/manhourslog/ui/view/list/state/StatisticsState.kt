package com.equationl.manhourslog.ui.view.list.state

import androidx.compose.foundation.lazy.LazyListState
import com.equationl.manhourslog.model.StaticsScreenModel
import com.equationl.manhourslog.util.DateTimeUtil
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.toTimestamp
import me.bytebeats.views.charts.bar.BarChartData

data class StatisticsState(
    val isLoading: Boolean = true,
    val showRange: StatisticsShowRange = DateTimeUtil.getYearRange(System.currentTimeMillis().formatDateTime("yyyy").toInt()), // 默认今年
    val showType: StatisticsShowType = StatisticsShowType.List,
    val showScale: StatisticsShowScale = StatisticsShowScale.Day,
    val dataList: List<StaticsScreenModel> = listOf(),
    val barChartData: List<BarChartData.Bar> = listOf(),
    val listState: LazyListState = LazyListState()
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

fun StatisticsShowScale.getNextRange(time: Long): StatisticsShowRange? {
    return when (this) {
        StatisticsShowScale.Year -> DateTimeUtil.getMonthRange(time.formatDateTime("yyyy").toInt(), time.formatDateTime("MM").toInt() - 1)
        StatisticsShowScale.Month -> {
            val start = time.formatDateTime("yyyy-MM-dd").toTimestamp("yyyy-MM-dd")
            val end = start + DateTimeUtil.DAY_MILL_SECOND_TIME
            StatisticsShowRange(start, end)
        }
        StatisticsShowScale.Day -> null
    }
}
