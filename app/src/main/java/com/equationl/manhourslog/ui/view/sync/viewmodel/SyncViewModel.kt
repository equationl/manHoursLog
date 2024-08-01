package com.equationl.manhourslog.ui.view.sync.viewmodel

import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.App
import com.equationl.manhourslog.BuildConfig
import com.equationl.manhourslog.constants.ExportHeader
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.socket.client.ClientCallback
import com.equationl.manhourslog.socket.client.SocketClient
import com.equationl.manhourslog.socket.server.ServerCallback
import com.equationl.manhourslog.socket.server.SocketServer
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.sync.state.SyncDeviceType
import com.equationl.manhourslog.ui.view.sync.state.SyncState
import com.equationl.manhourslog.util.ResolveDataUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TODO 数据同步应该是双向同步而不是现在这种单向同步
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    companion object {
        private const val TAG = "SyncViewModel"
    }

    private var rcvSyncData: MutableList<Byte>? = null
    private var isTheServerStarted: Boolean = false

    private val _uiState = MutableStateFlow(SyncState())
    val uiState = _uiState.asStateFlow()


    fun clientConnect(address: String) {
        if (address.isBlank()) {
            Toast.makeText(App.instance, "Please enter a ip address", Toast.LENGTH_SHORT).show()
            return
        }

        _uiState.update {
            it.copy(
                bottomTip = "Connect to $address",
            )
        }

        SocketClient.connectServer(address, clientCallback)
    }

    fun onClickSend() {
        if (_uiState.value.isClientConnected) {
            // 已连接，开始发送
            checkConnectAvailable()
        }
        else { // 未连接，修改 UI
            _uiState.update {
                it.copy(
                    syncDeviceType = SyncDeviceType.Send,
                    isClientConnected = false,
                    currentTitle = "Standby",
                    bottomTip = "Wait for connect to another device"
                )
            }
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
                syncDeviceType = SyncDeviceType.Receive,
            )
        }

        SocketServer.startServer(serverCallback)
    }

    fun stop() {
        try {
            if (_uiState.value.syncDeviceType == SyncDeviceType.Receive) {
                SocketServer.stopServer()
                isTheServerStarted = false
            }
            if (_uiState.value.syncDeviceType == SyncDeviceType.Send) {
                SocketClient.closeConnect()
            }
        } catch (tr: Throwable) {
            Log.e(TAG, "stop: ", tr)
        }

        _uiState.update {
            it.copy(
                syncDeviceType = SyncDeviceType.Wait,
                bottomTip = null,
                currentTitle = null
            )
        }
    }

    private fun checkConnectAvailable() {
        SocketClient.sendToServer("${SocketConstant.READY_TO_SYNC_FLAG}:${BuildConfig.VERSION_CODE}".toByteArray())

        _uiState.update {
            it.copy(
                currentTitle = "Checking",
                bottomTip = "Check connecting..."
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
                                syncDeviceType = SyncDeviceType.Wait,
                                bottomTip = null,
                            )
                        }
                        return
                    }

                    isTheServerStarted = true

                    _uiState.update {
                        it.copy(
                            bottomTip = "Please using another device connect me",
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
                            syncDeviceType = SyncDeviceType.Wait
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

    private val clientCallback = object : ClientCallback {
        override fun receiveServerMsg(ipAddress: String, msg: ByteArray) {
            Log.i(TAG, "receiveServerMsg: ip = $ipAddress, msg = ${msg.decodeToString()}")
            parseClientData(ipAddress, msg)
        }

        override fun otherMsg(msg: String, type: Int?) {
            Log.i(TAG, "otherMsg: msg = $msg, type = $type")

            when (type) {
                SocketConstant.CONNECT_SUCCESS -> {
                    _uiState.update {
                        it.copy(
                            isClientConnected = true,
                            bottomTip = "Click the button above to start syncing",
                            currentTitle = "Sync Now"
                        )
                    }

                    SocketClient.sendToServer(SocketConstant.CONNECT_SUCCESS_FLAG.toByteArray())
                }
                SocketConstant.CONNECT_FAIL -> {
                    _uiState.update {
                        it.copy(
                            isClientConnected = false,
                            bottomTip = "Connect fail, please retry.\n$msg",
                            currentTitle = "Standby"
                        )
                    }
                }
                SocketConstant.HEARTBEAT_TIMEOUT -> {
                    _uiState.update {
                        it.copy(
                            isClientConnected = false,
                            bottomTip = "Connect fail, The other device is not responding",
                            currentTitle = "Fail"
                        )
                    }

                    try {
                        SocketClient.closeConnect()
                    } catch (tr: Throwable) {
                        Log.e(TAG, "stop: ", tr)
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
                    currentTitle = "Ready for sync"
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
            SocketServer.sendToClient("${SocketConstant.READY_TO_SYNC_FLAG}:${BuildConfig.VERSION_CODE}".toByteArray())
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

    private fun parseClientData(ipAddress: String, msg: ByteArray) {
        val msgText = msg.decodeToString()
        if (msgText.startsWith(SocketConstant.READY_TO_SYNC_FLAG)) { // 同步前的握手数据
            val valueList = msgText.split(":")
            val version = valueList.getOrNull(1)
            if (version == BuildConfig.VERSION_CODE.toString()) {
                _uiState.update {
                    it.copy(
                        currentTitle = "Syncing",
                        bottomTip = "Synchronizing data...",
                    )
                }
                // 数据传输没问题，开始同步
                startSync()
            }
            else { // 客户端版本号不同，拒绝同步
                _uiState.update {
                    it.copy(
                        currentTitle = "Fail",
                        bottomTip = "The app version not same, Please update to the latest version and try again\nYour version: ${BuildConfig.VERSION_CODE}, Another device version: $version",
                    )
                }
            }
        }
        else if (msgText.startsWith(SocketConstant.SYNC_DATA_FINISH)) { // 传输数据完成
            _uiState.update {
                it.copy(
                    currentTitle = "Sync Now",
                    bottomTip = "Sync Finish, Click to sync again or click Stop to exit",
                )
            }
        }
        else {
            Log.w(TAG, "parseServerData: 接收到未知的数据：$msgText\n原始数据：$msg")
        }
    }

    private fun startSync() {
        // 开始发送数据
        viewModelScope.launch {
            val rawDataList = db.manHoursDB().queryRangeDataList(0, System.currentTimeMillis(), 1, Int.MAX_VALUE)
            val dataModel = ResolveDataUtil.rawDataToStaticsModel(rawDataList, StatisticsShowScale.Day)
            var dataText = SocketConstant.SYNC_DATA_HEADER
            dataModel.forEachIndexed { index, model ->
                if (index == 0) {
                    dataText += ExportHeader.DAY
                }
                dataText += ResolveDataUtil.getCsvRow(StatisticsShowScale.Day, model)
            }

            val resultByteArray = mutableListOf<Byte>()
            resultByteArray.addAll(dataText.toByteArray().toList())
            resultByteArray.addAll(SocketConstant.END_FLAG.toList())

            SocketClient.sendToServer(resultByteArray.toByteArray())
        }
    }

    /**
     * 解析服务端接收到的同步数据
     * */
    private fun resolveSyncData(fullData: MutableList<Byte>) {
        viewModelScope.launch {
            val fullDataText = fullData.toByteArray().decodeToString().replace(SocketConstant.SYNC_DATA_HEADER, "")

            Log.i(TAG, "resolveSyncData: fullData = $fullDataText")

            val result = ResolveDataUtil.importFromCsv(App.instance, fullDataText.lineSequence(), db)

            val tipText = if (result) "Sync Finish" else "Sync Finish, But Some data not sync"

            _uiState.update {
                it.copy(
                    currentTitle = "Ready for sync",
                    bottomTip = "$tipText, Click Stop to exit",
                )
            }

            SocketServer.sendToClient(SocketConstant.SYNC_DATA_FINISH.toByteArray())
        }
    }
}