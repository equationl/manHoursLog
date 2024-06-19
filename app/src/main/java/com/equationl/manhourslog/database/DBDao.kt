package com.equationl.manhourslog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ManHoursDao{
    @Insert
    suspend fun insertData(data: DBManHoursTable)

    @Query("SELECT SUM(total_Time) FROM man_hours_table WHERE start_Time BETWEEN :startTime AND :endTime")
    suspend fun queryRangeTotalTime(startTime: Long, endTime: Long): Long

    @Query("SELECT * FROM man_hours_table WHERE start_Time BETWEEN :startTime AND :endTime ORDER BY id DESC LIMIT (:pageSize + 1) OFFSET ((:page - 1) * :pageSize)")
    suspend fun queryRangeDataList(startTime: Long, endTime: Long, page: Int = 1, pageSize: Int = 50): List<DBManHoursTable>

    @Query("SELECT COUNT() FROM man_hours_table WHERE start_Time BETWEEN :startTime AND :endTime")
    suspend fun queryRangeDataCount(startTime: Long, endTime: Long): Int
}