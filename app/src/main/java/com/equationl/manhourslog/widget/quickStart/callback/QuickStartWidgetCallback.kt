package com.equationl.manhourslog.widget.quickStart.callback

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.equationl.manhourslog.database.DBManHoursTable
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.ui.view.home.state.LogState
import com.equationl.manhourslog.util.datastore.DataKey
import com.equationl.manhourslog.util.datastore.DataStoreUtils
import com.equationl.manhourslog.util.fromJson
import com.equationl.manhourslog.util.toJson
import com.equationl.manhourslog.widget.quickStart.receiver.QuickStartWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickStartWidgetCallback : ActionCallback {
    companion object {
        @Suppress("unused")
        private const val TAG = "el, QuickStartWidgetCallback"

        const val ACTION_NAME = "actionName"
        const val TOGGLE_STATE = "toggleState"
        const val LOG_STATE = "logState"
        const val APP_WIDGET_ID = "appWidgetId"

        const val UPDATE_ACTION = "updateAction"
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val actionKey = ActionParameters.Key<String>(ACTION_NAME)
        val actionName = parameters[actionKey]
        // val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        val logState = parameters[ActionParameters.Key<String>(LOG_STATE)]?.fromJson<LogState>() ?: LogState(isStart = false)

        if (actionName == TOGGLE_STATE) {
            toggleState(context, logState, CoroutineScope(Dispatchers.IO), glanceId)
        }
    }

    private fun toggleState(context: Context, logState: LogState, scope: CoroutineScope, glanceId: GlanceId) {
        val db = ManHoursDB.create(context)
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        if (logState.isStart) {
            val newLogState = LogState(isStart = false)
            DataStoreUtils.saveSyncStringData(DataKey.LOG_STATE, newLogState.toJson())

            // 插入数据到数据库
            scope.launch {
                val currentTime = System.currentTimeMillis()
                db.manHoursDB().insertData(
                    DBManHoursTable(
                        startTime = logState.startTime ?: 0L,
                        endTime = currentTime,
                        totalTime = currentTime - (logState.startTime ?: 0L),
                        noteText = null
                    )
                )
            }
        }
        else {
            val newLogState = LogState(isStart = true, startTime = System.currentTimeMillis())
            DataStoreUtils.saveSyncStringData(DataKey.LOG_STATE, newLogState.toJson())
        }

        val intent = Intent(context, QuickStartWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
            putExtra(APP_WIDGET_ID, appWidgetId)
        }
        context.sendBroadcast(intent)
    }
}