package com.equationl.manhourslog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "man_hours_table", indices = [Index(value = ["start_Time"], unique = true)])
data class DBManHoursTable (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "start_Time")
    var startTime: Long,
    @ColumnInfo(name = "end_Time")
    var endTime: Long,
    @ColumnInfo(name = "total_Time")
    var totalTime: Long,
    @ColumnInfo(name = "delete_flag", defaultValue = "0")
    var isDelete: Boolean = false,
    @ColumnInfo(name = "note_text")
    var noteText: String? = null,
    /**
     * 数据来源：
     *
     * 0：原始记录；1：导入数据；
     * */
    @ColumnInfo(name = "data_source_type", defaultValue = "0")
    var dataSourceType: Int = 0,
    @ColumnInfo(name = "edit_time", defaultValue = "0")
    var editTime: Long = System.currentTimeMillis()
)