package com.equationl.manhourslog.socket.client

/**
 * 客户端回调
 */
interface ClientCallback {
    //接收服务端的消息
    fun receiveServerMsg(ipAddress: String, msg: ByteArray)
    //其他消息
    fun otherMsg(msg: String, type: Int? = null)
}