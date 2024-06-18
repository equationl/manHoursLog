package com.equationl.manhourslog.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.database.ManHoursDbUtil

class ManHoursPagingSource(
    private val api: ManHoursDB,
    private val startTime: Long,
    private val endTime: Long,
) : PagingSource<Int, DBManHoursTable>() {
    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, DBManHoursTable> {
        try {
            val nextPageNumber = params.key ?: 1  // 从第 1 页开始加载
            val result = ManHoursDbUtil.getManHoursDataList(api, startTime, endTime, page = nextPageNumber, pageSize = params.loadSize)

            Log.w("el", "load: result = $result")

            val totalPage = result.total
            return LoadResult.Page(
                data = result.data,
                prevKey = null, // 设置为 null 表示只加载下一页
                nextKey = if (nextPageNumber >= totalPage || totalPage == -1) null else nextPageNumber + 1
            )
        } catch (tr: Throwable) {
            return LoadResult.Error(tr)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DBManHoursTable>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}