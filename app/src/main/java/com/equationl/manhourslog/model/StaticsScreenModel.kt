package com.equationl.manhourslog.model

data class StaticsScreenModel(
    val id: Int,
    val startTime: Long,
    val endTime: Long,
    val totalTime: Long,
    val headerTitle: String,
    val headerTotalTime: Long,
    var note: String?,
    /**
     * 数据来源：
     *
     * null：不使用；0：原始记录；1：导入数据；2：同步数据
     * */
    val dataSourceType: Int?
)
