package com.equationl.manhourslog.util

import com.equationl.manhourslog.ui.view.list.state.StatisticsShowRange
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {
    const val DAY_MILL_SECOND_TIME = 86400_000L
    const val HOUR_MILL_SECOND_TIME = 3600_000L



    fun Long.formatTime(): String {
        val base = this / 1000
        val hours = base / 3600
        val minutes = (base - hours * 3600) / 60
        val seconds = base - hours * 3600 - minutes * 60

        return "${hours.toString().padStart(2, '0')}" +
                ":${minutes.toString().padStart(2, '0')}" +
                ":${seconds.toString().padStart(2, '0')}"
    }

    fun Long.formatDateTime(format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return sDateFormat.format(Date(this))
    }

    fun String.toTimestamp(format: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val date = SimpleDateFormat(format, Locale.getDefault()).parse(this)
        return date?.time ?: 0L
    }

    fun getWeeOfToday(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis
    }

    fun getWeeOfCurrentMonth(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.DAY_OF_MONTH] = 1
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.MILLISECOND] = 0
        return cal.timeInMillis
    }

    fun getCurrentMonthEnd(): Long {
        val cal = Calendar.getInstance()
        cal[Calendar.DATE] = 1 //把日期设置为当月第一天
        cal.roll(Calendar.DATE, -1) //日期回滚一天，也就是最后一天

        //当月有多少天
        val maxDate = cal[Calendar.DATE]

        return getWeeOfCurrentMonth() + maxDate * DAY_MILL_SECOND_TIME
    }

    fun getYearRange(year: Int): StatisticsShowRange {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = Calendar.JANUARY
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val startTimestamp = calendar.timeInMillis // 年初时间戳

        calendar[Calendar.MONTH] = Calendar.DECEMBER
        calendar[Calendar.DAY_OF_MONTH] = 31
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        val endTimestamp = calendar.timeInMillis // 年末时间戳

        return StatisticsShowRange(startTimestamp, endTimestamp)
    }

    fun getMonthRange(year: Int, month: Int): StatisticsShowRange {
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val startTimestamp = calendar.timeInMillis // 月初时间戳

        calendar.roll(Calendar.DATE, -1)
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        val endTimestamp = calendar.timeInMillis // 月末时间戳

        return StatisticsShowRange(startTimestamp, endTimestamp)
    }


}