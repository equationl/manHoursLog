package com.equationl.manhourslog.model

data class StaticsScreenModel(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val totalTime: Long,
    val headerTitle: String,
    val headerTotalTime: Long
)
