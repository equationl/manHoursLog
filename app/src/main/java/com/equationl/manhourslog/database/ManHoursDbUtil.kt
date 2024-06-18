package com.equationl.manhourslog.database

import com.equationl.manhourslog.model.PageModel

object ManHoursDbUtil {
    suspend fun getManHoursDataList(
        db: ManHoursDB,
        startTime: Long,
        endTime: Long,
        page: Int = 1,
        pageSize: Int = 50,
    ): PageModel<DBManHoursTable> {

        //Log.w("el", "SELECT COUNT() FROM man_hours_table WHERE start_Time BETWEEN $startTime AND $endTime")
        //Log.w("el", "SELECT * FROM man_hours_table WHERE start_Time BETWEEN $startTime AND $endTime ORDER BY id ASC LIMIT ($pageSize + 1) OFFSET (($page - 1) * $pageSize)")

        val count = db.manHoursDB().queryRangeDataCount(startTime, endTime)
        if (count > 0) {
            val dataList = db.manHoursDB().queryRangeDataList(startTime, endTime, page, pageSize)
            return PageModel(
                pageSize = pageSize,
                page = page,
                total = count,
                data = dataList
            )
        }

        return PageModel(
            pageSize = pageSize,
            page = page,
            total = 0,
            data = listOf()
        )
    }
}