package com.equationl.manhourslog.widget.statics.ui

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.equationl.manhourslog.widget.common.constant.WidgetConstants

// TODO 联动 logState 更新（实时更新）
class StaticsWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {
            val prefs = currentState<Preferences>()
            val staticDataModelJson = prefs[WidgetConstants.prefKeyStaticsData]
            val lastUpdateTime = prefs[WidgetConstants.prefKeyLastUpdateTime]

            StaticsContent(staticDataModelJson ?: "", lastUpdateTime)
        }
    }
}