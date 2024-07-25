package com.equationl.manhourslog.constants

object ExportHeader {
    const val DAY = "Start Time,End Time,Total Man Hours,Note,Start Timestamp\n"
    const val MONTH = "Day,Total Man Hours\n"
    const val YEAR = "Month,Total Man Hours\n"
}

object SocketConstant {
    const val SOCKET_PORT = 62598
    const val HEARTBEAT_SEND_MSG = "manHoursLogHeartbeatRequest"
    const val HEARTBEAT_RESPONSE_MSG = "manHoursLogHeartbeatResponse"
    const val CHECK = "check"

    const val SOCKET_CLOSED = -1000
    const val HEARTBEAT_FAIL = -1001

    const val CONNECT_SUCCESS = 0
    const val CONNECT_FAIL = -1
    const val NOT_CONNECT = -2
    const val SEND_FAIL = -3
}