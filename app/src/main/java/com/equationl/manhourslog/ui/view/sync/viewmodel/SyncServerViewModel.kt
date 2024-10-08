package com.equationl.manhourslog.ui.view.sync.viewmodel

import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.App
import com.equationl.manhourslog.BuildConfig
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.socket.server.ServerCallback
import com.equationl.manhourslog.socket.server.SocketServer
import com.equationl.manhourslog.ui.view.sync.state.SyncServerState
import com.equationl.manhourslog.util.ResolveDataUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncServerViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    companion object {
        private const val TAG = "SyncViewModel"
    }

    private var rcvSyncData: MutableList<Byte>? = null
    private var isTheServerStarted: Boolean = false
    private var serverSyncData: ByteArray? = null

    private val _uiState = MutableStateFlow(SyncServerState())
    val uiState = _uiState.asStateFlow()

    init {
        // 初始化数据
        _uiState.update {
            _uiState.value.copy(
                currentTitle = "Launcher",
                bottomTip = "Click above to launcher server"
            )
        }
    }

    fun onClickReceive() {
        if (isTheServerStarted) {
            // 服务端已启动，不响应点击
            return
        }

        _uiState.update {
            it.copy(
                bottomTip = "Preparing to receive data...",
            )
        }

        SocketServer.startServer(serverCallback)
    }

    fun stop() {
        try {
            SocketServer.stopServer()
            isTheServerStarted = false
        } catch (tr: Throwable) {
            Log.e(TAG, "stop: ", tr)
        }

        _uiState.update {
            it.copy(
                isServerConnected = false,
                bottomTip = null,
                currentTitle = null
            )
        }
    }

    private fun getIpAddress(): String? {
        val wifiManager = App.instance.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        if (ip == 0) {
            return null
        }

        return intToIp(ip)
    }

    /**
     * Ip地址转换
     */
    private fun intToIp(ip: Int) =
        "${(ip and 0xFF)}.${(ip shr 8 and 0xFF)}.${(ip shr 16 and 0xFF)}.${(ip shr 24 and 0xFF)}"


    @OptIn(ExperimentalStdlibApi::class)
    private val serverCallback = object : ServerCallback {
        override fun receiveClientMsg(ipAddress: String, msg: ByteArray) {
            Log.i(TAG, "receiveClientMsg: ip: $ipAddress, msg: ${msg.toHexString()}")

            parseServerData(ipAddress, msg)
        }

        override fun otherMsg(msg: String, type: Int?) {
            Log.i(TAG, "otherMsg: $msg， type: $type")
            when (type) {
                SocketConstant.SERVER_START_SUCCESS -> { // 服务端启动成功
                    val ip = getIpAddress()
                    if (ip == null) {
                        Toast.makeText(App.instance, "Please open wifi then connect a wifi", Toast.LENGTH_SHORT).show()
                        _uiState.update {
                            it.copy(
                                bottomTip = null,
                            )
                        }
                        return
                    }

                    isTheServerStarted = true

                    _uiState.update {
                        it.copy(
                            bottomTip = "Please using another device connect me by the above IP",
                            currentTitle = ip
                        )
                    }
                }
                SocketConstant.CONNECT_SUCCESS -> { // 服务端接收到新的客户端连接

                }
                SocketConstant.CONNECT_FAIL -> { // 服务端抛出异常
                    isTheServerStarted = false

                    _uiState.update {
                        it.copy(
                            bottomTip = null,
                            currentTitle = null,
                            isServerConnected = true
                        )
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            App.instance,
                            "Preparing to receive failed, please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parseServerData(ipAddress: String, msg: ByteArray) {
        val msgText = msg.decodeToString()

        if (rcvSyncData != null) { // 正在接收同步数据
            syncData(msg, ipAddress)
        }
        else if (msgText.startsWith(SocketConstant.CONNECT_SUCCESS_FLAG)) { // 有客户端连接成功
            _uiState.update {
                it.copy(
                    bottomTip = "$ipAddress Connected, Please let it initiate the sync",
                    currentTitle = "Ready for sync",
                    isServerConnected = true
                )
            }
        }
        else if (msgText.startsWith(SocketConstant.READY_TO_SYNC_FLAG)) { // 同步前的握手数据
            _uiState.update {
                it.copy(
                    currentTitle = "Checking",
                    bottomTip = "Check connecting..."
                )
            }

            viewModelScope.launch {
                // 准备数据，当数据接收完成后将此数据发送给客户端
                serverSyncData = ResolveDataUtil.prepareDataForSync(db)
                SocketServer.sendToClient("${SocketConstant.READY_TO_SYNC_FLAG}:${BuildConfig.VERSION_CODE}".toByteArray())
            }
        }
        else if (msgText.startsWith(SocketConstant.SYNC_DATA_HEADER)) { // 开始同步数据
            _uiState.update {
                it.copy(
                    currentTitle = "Syncing",
                    bottomTip = "Synchronizing data...",
                )
            }
            rcvSyncData = mutableListOf()

            syncData(msg, ipAddress)
        }
        else if (msgText.startsWith(SocketConstant.SYNC_DATA_FINISH)) { // 同步完成
            _uiState.update {
                it.copy(
                    currentTitle = "Finish",
                    bottomTip = "Data sync finish, you can exit now",
                )
            }
        }
        else {
            Log.w(TAG, "parseServerData: 接收到未知的数据：$msgText\n原始数据：${msg.toHexString()}")
        }
    }

    private fun syncData(msg: ByteArray, ipAddress: String) {
        repeat(msg.size) {
            if (msg[it] != SocketConstant.END_FLAG[0]) {
                // fixme java.lang.NullPointerException  !!
                //rcvSyncData!!.add(msg[it])
                rcvSyncData?.add(msg[it])
            }
            else {
                if (msg.getOrNull(it+1) == SocketConstant.END_FLAG[1]) { // 数据传输结束
                    val fullData = mutableListOf<Byte>()
                    fullData.addAll(rcvSyncData!!)
                    rcvSyncData = null
                    resolveSyncData(fullData)
                    if (it+2 < msg.size) { // 还有剩余数据
                        parseServerData(ipAddress, msg.copyOfRange(it+2, msg.size))
                    }
                    return@repeat
                }
            }
        }
    }

    /**
     * 解析服务端接收到的同步数据
     * */
    private fun resolveSyncData(fullData: MutableList<Byte>) {
        viewModelScope.launch {
            val fullDataText = fullData.toByteArray().decodeToString().replace(SocketConstant.SYNC_DATA_HEADER, "")

            Log.i(TAG, "resolveSyncData: fullData = $fullDataText")

            val result = ResolveDataUtil.importFromCsv(App.instance, fullDataText.lineSequence(), db, 2)

            val tipText = if (result) "Receive Finish" else "Receive Finish, But Some data not sync"

            _uiState.update {
                it.copy(
                    currentTitle = "Send data...",
                    bottomTip = "$tipText\nSending my data",
                )
            }

            SocketServer.sendToClient(SocketConstant.SYNC_DATA_FINISH.toByteArray())
            if (serverSyncData != null) {
                // 如果本机数据不为空，则发送给客户端
                SocketServer.sendToClient(serverSyncData!!)
            }
        }
    }
}