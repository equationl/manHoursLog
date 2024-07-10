package com.equationl.manhourslog.widget.receiver

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.equationl.manhourslog.widget.callback.QuickStartWidgetCallback
import com.equationl.manhourslog.widget.ui.quickStart.QuickStartWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class QuickStartWidgetReceiver: GlanceAppWidgetReceiver() {
    private val coroutineScope = MainScope()

    override val glanceAppWidget: GlanceAppWidget = QuickStartWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(QuickStartWidgetCallback.APP_WIDGET_ID, -1)

        if (intent.action == QuickStartWidgetCallback.UPDATE_ACTION) {
            coroutineScope.launch {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                glanceAppWidget.update(context, glanceId)
            }
        }
    }
}