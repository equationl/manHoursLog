package com.equationl.manhourslog.model

data class PageModel<T>(
    val pageSize: Int,
    val page: Int,
    val total: Int,
    val data: List<T>
)
