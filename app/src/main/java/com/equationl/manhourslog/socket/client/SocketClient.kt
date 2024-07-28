package com.equationl.manhourslog.socket.client

import android.os.Handler
import android.util.Log
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.constants.SocketConstant.CONNECT_FAIL
import com.equationl.manhourslog.constants.SocketConstant.CONNECT_SUCCESS
import com.equationl.manhourslog.constants.SocketConstant.HEARTBEAT_SEND_MSG
import com.equationl.manhourslog.constants.SocketConstant.SOCKET_PORT
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Socket客户端
 */
object SocketClient {

    private const val TAG = "SocketClient"

    private var socket: Socket? = null

    private var outputStream: OutputStream? = null

    private var inputStreamReader: InputStreamReader? = null

    private lateinit var mCallback: ClientCallback

    // 客户端线程池
    private var clientThreadPool: ExecutorService? = null

    //心跳发送间隔
    private const val HEART_SPACETIME = 3 * 1000

    private val mHandler: Handler = Handler()

    /**
     * 连接服务
     */
    fun connectServer(ipAddress: String, callback: ClientCallback) {
        mCallback = callback
        Thread {
            try {
                socket = Socket(ipAddress, SOCKET_PORT)
                if (socket!!.isConnected) {
                    //开启心跳,每隔3秒钟发送一次心跳
                    mHandler.post(mHeartRunnable)
                    ClientThread(socket!!, mCallback).start()
                    callback.otherMsg("success", type = CONNECT_SUCCESS)
                }
                else {
                    callback.otherMsg("connect fail", type = CONNECT_FAIL)
                }
            } catch (e: IOException) {
                callback.otherMsg(e.message ?: "connect fail", type = CONNECT_FAIL)
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * 关闭连接
     */
    fun closeConnect() {
        inputStreamReader?.close()
        outputStream?.close()
        socket?.close()
        //关闭线程池
        clientThreadPool?.shutdownNow()
        clientThreadPool = null
    }

    /**
     * 发送数据至服务器
     * @param msg 要发送至服务器的字符串
     */
    fun sendToServer(msg: ByteArray) {
        if (clientThreadPool == null) {
            clientThreadPool = Executors.newSingleThreadExecutor()
        }
        clientThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接", type = SocketConstant.NOT_CONNECT)
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭", type = SocketConstant.SOCKET_CLOSED)
                return@execute
            }
            outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(msg)
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("向服务端发送消息: ${msg.decodeToString()} 失败: ${e.message}", type = SocketConstant.SEND_FAIL)
            }
        }
    }

    private val mHeartRunnable = Runnable { sendHeartbeat() }

    /**
     * 发送心跳消息
     *
     * TODO 增加心跳消息超时重连或关闭连接
     */
    private fun sendHeartbeat() {
        if (clientThreadPool == null) {
            clientThreadPool = Executors.newSingleThreadExecutor()
        }
        val msg = HEARTBEAT_SEND_MSG
        clientThreadPool?.execute {
            if (socket == null) {
                mCallback.otherMsg("客户端还未连接", type = SocketConstant.HEARTBEAT_FAIL)
                return@execute
            }
            if (socket!!.isClosed) {
                mCallback.otherMsg("Socket已关闭", type = SocketConstant.HEARTBEAT_FAIL)
                return@execute
            }
            outputStream = socket?.getOutputStream()
            try {
                outputStream?.write(msg.toByteArray())
                outputStream?.flush()
                //发送成功以后，重新建立一个心跳消息
                mHandler.postDelayed(mHeartRunnable, HEART_SPACETIME.toLong())
                Log.i(TAG, msg)
            } catch (e: IOException) {
                e.printStackTrace()
                mCallback.otherMsg("发送心跳消息失败： $e", type = SocketConstant.HEARTBEAT_FAIL)
            }
        }
    }

    class ClientThread(private val socket: Socket, private val callback: ClientCallback) :
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

                    socket.inetAddress.hostAddress?.let {
                        if (receiveStr == HEARTBEAT_SEND_MSG) {//收到来自服务端的心跳回复消息
                            Log.i(TAG, "心跳正常！")
                            //准备回复
                        } else {
                            callback.receiveServerMsg(it, buffer.copyOfRange(0, len))
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