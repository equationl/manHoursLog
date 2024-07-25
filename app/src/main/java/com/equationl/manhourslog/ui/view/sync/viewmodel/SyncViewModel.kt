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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val db: ManHoursDB
): ViewModel() {
    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _uiState = MutableStateFlow(
        SyncState()
    )

    val uiState = _uiState.asStateFlow()


    fun clientConnect(address: String) {
        // TODO
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
        // TODO
        _uiState.update {
            it.copy(
                bottomTip = "Preparing to receive data...",
                syncDeviceType = SyncDeviceType.Receive,
            )
        }

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

        SocketServer.startServer(serverCallback)

        _uiState.update {
            it.copy(
                bottomTip = "Please using another device connect me",
                currentTitle = ip
            )
        }
    }

    fun stop() {
        // TODO
        if (_uiState.value.syncDeviceType == SyncDeviceType.Receive) {
            SocketServer.stopServer()
        }
        if (_uiState.value.syncDeviceType == SyncDeviceType.Send) {
            SocketClient.closeConnect()
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
        // TODO

        SocketClient.sendToServer("${SocketConstant.CHECK}:${BuildConfig.VERSION_CODE}".toByteArray())
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


    private val serverCallback = object : ServerCallback {
        override fun receiveClientMsg(ipAddress: String, msg: ByteArray) {
            Log.i(TAG, "receiveClientMsg: ip: $ipAddress, msg: $msg")

            parseServerData(ipAddress, msg)
        }

        override fun otherMsg(msg: String, type: Int?) {
            Log.i(TAG, "otherMsg: $msg")
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
            }
        }

    }

    private fun parseServerData(ipAddress: String, msg: ByteArray) {
        if (msg.decodeToString().startsWith(SocketConstant.CHECK)) { // 同步前的握手数据
            SocketServer.sendToClient("${SocketConstant.CHECK}:${BuildConfig.VERSION_CODE}".toByteArray())
        }
    }

    private fun parseClientData(ipAddress: String, msg: ByteArray) {
        if (msg.decodeToString().startsWith(SocketConstant.CHECK)) { // 同步前的握手数据
            // 数据传输没问题，开始同步
            startSync()
        }
    }

    private fun startSync() {
        // TODO 开始发送数据
        viewModelScope.launch {
            val rawDataList = db.manHoursDB().queryRangeDataList(0, System.currentTimeMillis(), 1, Int.MAX_VALUE)
            val dataModel = ResolveDataUtil.rawDataToStaticsModel(rawDataList, StatisticsShowScale.Day)
            var dataText = ""
            dataModel.forEachIndexed { index, model ->
                if (index == 0) {
                    dataText += ExportHeader.DAY
                }
                dataText += ResolveDataUtil.getCsvRow(StatisticsShowScale.Day, model)
            }

            SocketClient.sendToServer(dataText.toByteArray())
        }
    }
}