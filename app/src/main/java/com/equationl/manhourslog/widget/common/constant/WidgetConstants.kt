package com.equationl.manhourslog.widget.common.constant

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.action.ActionParameters

object WidgetConstants {
    const val ACTION_NAME = "actionName"
    const val TOGGLE_STATE = "toggleState"
    const val UPDATE_DATA = "updateData"
    const val LOG_STATE = "logState"
    const val APP_WIDGET_ID = "appWidgetId"
    const val OPEN_ACTIVITY_TYPE = "openActivityType"

    const val UPDATE_ACTION = "updateAction"

    val prefKeyStaticsData = stringPreferencesKey("staticData")
    val prefKeyLastUpdateTime = longPreferencesKey("lastUpdateTime")

    val actionKey = ActionParameters.Key<String>(ACTION_NAME)
    val logStateKey = ActionParameters.Key<String>(LOG_STATE)
    val openActivityTypeKey = ActionParameters.Key<String>(OPEN_ACTIVITY_TYPE)

    enum class OpActivityType {
        HOME,
        STATICS
    }
}