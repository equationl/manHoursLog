package com.equationl.manhourslog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "man_hours_table")
data class DBManHoursTable (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "start_Time")
    var startTime: Long,
    @ColumnInfo(name = "end_Time")
    var endTime: Long,
    @ColumnInfo(name = "total_Time")
    var totalTime: Long
)