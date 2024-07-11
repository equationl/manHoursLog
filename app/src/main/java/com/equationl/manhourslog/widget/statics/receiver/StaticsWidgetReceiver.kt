package com.equationl.manhourslog.widget.statics.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.equationl.manhourslog.database.ManHoursDB
import com.equationl.manhourslog.util.DateTimeUtil
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.toJson
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import com.equationl.manhourslog.widget.common.model.StaticDataModel
import com.equationl.manhourslog.widget.statics.ui.StaticsWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class StaticsWidgetReceiver: GlanceAppWidgetReceiver() {
    private val coroutineScope = MainScope()

    override val glanceAppWidget: GlanceAppWidget = StaticsWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            coroutineScope.launch {
                updateWidgetData(context, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(WidgetConstants.APP_WIDGET_ID, -1)

        if (intent.action == WidgetConstants.UPDATE_ACTION) {
            coroutineScope.launch {
                updateWidgetData(context, appWidgetId)
            }
        }
    }

    private suspend fun updateWidgetData(context: Context, appWidgetId: Int) {
        val db = ManHoursDB.create(context)
        val weeOfToday = DateTimeUtil.getWeeOfToday()
        val weeOfMonth = DateTimeUtil.getWeeOfCurrentMonth()
        val endOfMonth = DateTimeUtil.getCurrentMonthEnd()
        val yearRange = DateTimeUtil.getYearRange(System.currentTimeMillis().formatDateTime("yyyy").toInt())

        val dayTotalTime = db.manHoursDB().queryRangeTotalTime(startTime = weeOfToday, endTime = weeOfToday + DateTimeUtil.DAY_MILL_SECOND_TIME)
        val monthTotalTime = db.manHoursDB().queryRangeTotalTime(startTime = weeOfMonth, endTime = endOfMonth)
        val yearTotalTime = db.manHoursDB().queryRangeTotalTime(startTime = yearRange.start, endTime = yearRange.end)
        val staticDataModel = StaticDataModel(
            dayTotalTime = dayTotalTime,
            monthTotalTime = monthTotalTime,
            yearTotalTime = yearTotalTime,
        )

        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { pref ->
            pref.toMutablePreferences().apply {
                this[WidgetConstants.prefKeyStaticsData] = staticDataModel.toJson()
                this[WidgetConstants.prefKeyLastUpdateTime] = System.currentTimeMillis()
            }
        }
        glanceAppWidget.update(context, glanceId)
    }
}