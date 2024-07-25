package com.equationl.manhourslog.ui.view.sync.state

data class SyncState(
    val syncDeviceType: SyncDeviceType = SyncDeviceType.Wait,
    val bottomTip: String? = null,
    val currentTitle: String? = null,
    val isClientConnected: Boolean = false
)

enum class SyncDeviceType {
    Send,
    Receive,
    /** 尚未选择作为什么角色  */
    Wait
}
