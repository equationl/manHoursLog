package com.equationl.manhourslog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ManHoursDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: DBManHoursTable): Long

    @Query("UPDATE man_hours_table SET delete_flag = 1 WHERE id = :id")
    suspend fun markDeleteRowById(id: Int): Int

    @Query("UPDATE man_hours_table SET note_text = :newValue WHERE id = :id")
    suspend fun updateNoteById(id: Int, newValue: String): Int

    @Query("UPDATE man_hours_table SET note_text = :newValue WHERE start_Time = :startTime")
    suspend fun updateNoteByStartTime(startTime: Long, newValue: String): Int

    @Query("SELECT SUM(total_Time) FROM man_hours_table WHERE (start_Time BETWEEN :startTime AND :endTime) AND delete_flag=0")
    suspend fun queryRangeTotalTime(startTime: Long, endTime: Long): Long

    @Query("SELECT * FROM man_hours_table WHERE (start_Time BETWEEN :startTime AND :endTime) AND delete_flag=0 ORDER BY start_Time DESC LIMIT (:pageSize + 1) OFFSET ((:page - 1) * :pageSize)")
    suspend fun queryRangeDataList(startTime: Long, endTime: Long, page: Int = 1, pageSize: Int = 50): List<DBManHoursTable>

    @Query("SELECT COUNT() FROM man_hours_table WHERE (start_Time BETWEEN :startTime AND :endTime) AND delete_flag=0")
    suspend fun queryRangeDataCount(startTime: Long, endTime: Long): Int
}