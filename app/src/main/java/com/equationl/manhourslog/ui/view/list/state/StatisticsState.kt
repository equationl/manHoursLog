package com.equationl.manhourslog.ui.view.list.state

import androidx.paging.PagingData
import com.equationl.manhourslog.database.DBManHoursTable
import kotlinx.coroutines.flow.Flow

data class StatisticsState(
    val isLoading: Boolean = true,
    val showRange: StatisticsShowRange = StatisticsShowRange(),
    val showType: StatisticsShowType = StatisticsShowType.List,
    val showScale: StatisticsShowScale = StatisticsShowScale.Day,
    val dataFlow: Flow<PagingData<DBManHoursTable>>? = null,
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
