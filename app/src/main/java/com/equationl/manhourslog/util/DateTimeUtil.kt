package com.equationl.manhourslog.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {
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

        return getWeeOfCurrentMonth() + maxDate * 86400000L
    }


}