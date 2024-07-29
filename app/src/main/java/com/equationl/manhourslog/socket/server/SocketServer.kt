package com.equationl.manhourslog.socket.server

import android.util.Log
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.constants.SocketConstant.HEARTBEAT_RESPONSE_MSG
import com.equationl.manhourslog.constants.SocketConstant.SOCKET_PORT
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Socket服务端
 */
object SocketServer {

    private const val TAG = "SocketServer"

    private var socket: Socket? = null
    private var serverSocket: ServerSocket? = null

    private lateinit var mCallback: ServerCallback

    private lateinit var outputStream: OutputStream

    var result = true

    // 服务端线程池
    private var serverThreadPool: ExecutorService? = null

    /**
     * 开启服务
     */
    fun startServer(callback: ServerCallback): Boolean {
        mCallback = callback
        Thread {
            try {
                // 同时只接受一个连接
                serverSocket = ServerSocket(SOCKET_PORT, 1, null)
                mCallback.otherMsg("Server start success", type = SocketConstant.SERVER_START_SUCCESS)
                while (result) {
                    socket = serverSocket?.accept()
                    mCallback.otherMsg("${socket?.inetAddress} to connected", type = SocketConstant.CONNECT_SUCCESS)
                    ServerThread(socket!!, mCallback).start()
                }
            } catch (e: IOException) {
                Log.e(TAG, "startServer: ", e)
                mCallback.otherMsg("${socket?.inetAddress} connect fail: $e", type = SocketConstant.CONNECT_FAIL)
                result = false
            }
        }.start()
        return result
    }

    /**
     * 关闭服务
     */
    fun stopServer() {
        socket?.apply {
            shutdownInput()
            shutdownOutput()
            close()
        }
        serverSocket?.close()

        //关闭线程池
        serverThreadPool?.shutdownNow()
        serverThreadPool = null
    }

    /**
     * 发送到客户端
     */
    fun sendToClient(msg: ByteArray) {
        if (serverThreadPool == null) {
            serverThreadPool = Executors.newCachedThreadPool()
        }
        serverThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接", type = SocketConstant.NOT_CONNECT)
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭", type = SocketConstant.SOCKET_CLOSED)
                return@execute
            }
            outputStream = socket!!.getOutputStream()
            try {

                outputStream.write(msg)
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向客户端发送消息: ${msg.decodeToString()} 失败: $e", SocketConstant.SEND_FAIL)
            }
        }
    }

    /**
     * 回复心跳消息
     *
     */
    fun replyHeartbeat() {
        if (serverThreadPool == null) {
            serverThreadPool = Executors.newCachedThreadPool()
        }
        val msg = HEARTBEAT_RESPONSE_MSG
        serverThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接", type = SocketConstant.NOT_CONNECT)
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭", type = SocketConstant.SOCKET_CLOSED)
                return@execute
            }
            outputStream = socket!!.getOutputStream()
            try {
                outputStream.write(msg.toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("发送心跳消息失败: $e", type = SocketConstant.HEARTBEAT_FAIL)
            }
        }
    }

    class ServerThread(private val socket: Socket, private val callback: ServerCallback) :
        Thread() {

        override fun run() {
            val inputStream: InputStream?
            try {
                inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                var len: Int
                var receiveStr = ""
                if (inputStream.available() == 0) {
                    Log.e(TAG, "inputStream.available() == 0")
                }
                while (inputStream.read(buffer).also { len = it } != -1) {
                    receiveStr += String(buffer, 0, len, Charsets.UTF_8)

                    Log.d(TAG, "run: rcv data($len) = $receiveStr")

                    socket.inetAddress.hostAddress?.let {
                        if (receiveStr == HEARTBEAT_RESPONSE_MSG) {//收到客户端发送的心跳消息
                            //准备回复
                            replyHeartbeat()
                        } else {
                            callback.receiveClientMsg(it, buffer.copyOfRange(0, len))
                        }
                    }
                    receiveStr = ""

                }
            } catch (e: IOException) {
                e.printStackTrace()
                when (e) {
                    is SocketTimeoutException -> {
                        Log.e(TAG, "连接超时，正在重连")
                    }
                    is NoRouteToHostException -> {
                        Log.e(TAG, "该地址不存在，请检查")
                    }
                    is ConnectException -> {
                        Log.e(TAG, "连接异常或被拒绝，请检查")
                    }
                    is SocketException -> {
                        when (e.message) {
                            "Already connected" -> Log.e(TAG, "连接异常或被拒绝，请检查")
                            "Socket closed" -> Log.e(TAG, "连接已关闭")
                        }
                    }
                }
            }
        }
    }

}