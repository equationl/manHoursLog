package com.equationl.manhourslog.ui.view.sync.viewmodel

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
import com.equationl.manhourslog.ui.view.list.state.StatisticsShowScale
import com.equationl.manhourslog.ui.view.sync.state.SyncClientState
import com.equationl.manhourslog.util.ResolveDataUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// TODO 数据同步应该是双向同步而不是现在这种单向同步
@HiltViewModel
class SyncClientViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _uiState = MutableStateFlow(SyncClientState())
    val uiState = _uiState.asStateFlow()

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
}