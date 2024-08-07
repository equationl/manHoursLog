package com.equationl.manhourslog.socket.server

/**
 * 服务端回调
 */
interface ServerCallback {
    //接收客户端的消息
    fun receiveClientMsg(ipAddress: String, msg: ByteArray)
    //其他消息
    fun otherMsg(msg: String, type: Int? = null)
}