package com.equationl.manhourslog.model

import com.equationl.manhourslog.database.DBManHoursTable

data class StaticsScreenModel(
    val rawData: DBManHoursTable,
    val yearSum: Long,
    val monthSum: Long,
    val daySum: Long,
    val headerTitle: String
)
