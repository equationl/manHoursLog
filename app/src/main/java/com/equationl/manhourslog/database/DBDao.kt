package com.equationl.manhourslog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ManHoursDao{
    @Insert
    suspend fun insertData(data: DBManHoursTable)

    @Query("SELECT SUM(total_Time) FROM man_hours_table WHERE start_Time BETWEEN :startTime AND :endTime")
    suspend fun queryTodayTotalTime(startTime: Long, endTime: Long): Long
}