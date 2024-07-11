package com.equationl.manhourslog.widget.statics.callback

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.ACTION_NAME
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.APP_WIDGET_ID
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.UPDATE_ACTION
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.UPDATE_DATA
import com.equationl.manhourslog.widget.statics.receiver.StaticsWidgetReceiver

class StaticWidgetCallback : ActionCallback {
    companion object {
        @Suppress("unused")
        private const val TAG = "el, QuickStartWidgetCallback"
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val actionKey = ActionParameters.Key<String>(ACTION_NAME)
        val actionName = parameters[actionKey]
        // val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        if (actionName == UPDATE_DATA) {
            updateDate(context, glanceId)
        }
    }

    private fun updateDate(context: Context, glanceId: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        val intent = Intent(context, StaticsWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
            putExtra(APP_WIDGET_ID, appWidgetId)
        }
        context.sendBroadcast(intent)
    }
}