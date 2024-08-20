package com.equationl.manhourslog.ui.view.sync.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.manhourslog.App
import com.equationl.manhourslog.BuildConfig
import com.equationl.manhourslog.constants.SocketConstant
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.socket.client.ClientCallback
import com.equationl.manhourslog.socket.client.SocketClient
import com.equationl.manhourslog.ui.view.sync.state.SyncClientState
import com.equationl.manhourslog.util.ResolveDataUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncClientViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _uiState = MutableStateFlow(SyncClientState())
    val uiState = _uiState.asStateFlow()

    private var rcvSyncData: MutableList<Byte>? = null

    init {
        // 初始化
        _uiState.update {
            it.copy(
                isClientConnected = false,
                currentTitle = "Standby",
                bottomTip = "Wait for connect to another device"
            )
        }
    }

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
        else { // 未连接，不响应

        }
    }

    fun stop() {
        try {
            SocketClient.closeConnect()
        } catch (tr: Throwable) {
            Log.e(TAG, "stop: ", tr)
        }

        _uiState.update {
            it.copy(
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
        else if (msgText.startsWith(SocketConstant.SYNC_DATA_FINISH)) { // 传输发送完成
            _uiState.update {
                it.copy(
                    currentTitle = "Receiving data...",
                    bottomTip = "Send data finish, Receiving data from the other device...",
                )
            }
        }
        else if (msgText.startsWith(SocketConstant.SYNC_DATA_HEADER)) { // 开始接收数据
            _uiState.update {
                it.copy(
                    bottomTip = "Receiving data...",
                )
            }
            rcvSyncData = mutableListOf()

            syncData(msg, ipAddress)
        }
        else {
            Log.w(TAG, "parseServerData: 接收到未知的数据：$msgText\n原始数据：$msg")
        }
    }

    private fun startSync() {
        // 开始发送数据
        viewModelScope.launch {
            SocketClient.sendToServer(ResolveDataUtil.prepareDataForSync(db))
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
                        parseClientData(ipAddress, msg.copyOfRange(it+2, msg.size))
                    }
                    return@repeat
                }
            }
        }
    }

    /**
     * 解析接收到的数据
     * */
    private fun resolveSyncData(fullData: MutableList<Byte>) {
        viewModelScope.launch {
            val fullDataText = fullData.toByteArray().decodeToString().replace(SocketConstant.SYNC_DATA_HEADER, "")

            Log.i(TAG, "resolveSyncData: fullData = $fullDataText")

            val result = ResolveDataUtil.importFromCsv(App.instance, fullDataText.lineSequence(), db, 2)

            val tipText = if (result) "Sync Finish" else "Sync Finish, But Some data not sync"

            _uiState.update {
                it.copy(
                    currentTitle = "Sync Now",
                    bottomTip = "$tipText \n Click to sync again or click Stop to exit",
                )
            }

            SocketClient.sendToServer(SocketConstant.SYNC_DATA_FINISH.toByteArray())
        }
    }
}