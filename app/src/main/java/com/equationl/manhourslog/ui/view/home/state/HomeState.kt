package com.equationl.manhourslog.ui.view.home.state

data class HomeState(
    val isLoading: Boolean = true,
    val logState: LogState = LogState(isStart = false),
    /** 总工时（s） */
    val totalManHours: Long = 0L,
    val noteValue: String = "",
)

data class LogState(
    val isStart: Boolean,
    val startTime: Long? = null
)