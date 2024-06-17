package com.equationl.manhourslog.util

object Utils {
    fun Long.formatTime(): String {
        val base = this / 1000
        val hours = base / 3600
        val minutes = (base - hours * 3600) / 60
        val seconds = base - hours * 3600 - minutes * 60

        return "${hours.toString().padStart(2, '0')}" +
                ":${minutes.toString().padStart(2, '0')}" +
                ":${seconds.toString().padStart(2, '0')}"
    }
}