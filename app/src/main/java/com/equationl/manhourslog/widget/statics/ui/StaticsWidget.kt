package com.equationl.manhourslog.widget.statics.ui

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.equationl.manhourslog.ui.view.home.state.LogState
import com.equationl.manhourslog.util.datastore.DataKey
import com.equationl.manhourslog.util.datastore.DataStoreUtils.dataStore
import com.equationl.manhourslog.util.fromJson
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import com.equationl.manhourslog.widget.statics.callback.StaticWidgetCallback

class StaticsWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val data = context.dataStore.data

        provideContent {
            val dataState by data.collectAsState(initial = null)

            val logState = dataState?.get(stringPreferencesKey(DataKey.LOG_STATE))?.fromJson<LogState>() ?: LogState(isStart = false)

            LaunchedEffect(logState) {
                // 这样写很丑，但是有用😂
                StaticWidgetCallback.updateDate(context, null)
            }

            val prefs = currentState<Preferences>()
            val staticDataModelJson = prefs[WidgetConstants.prefKeyStaticsData]
            val lastUpdateTime = prefs[WidgetConstants.prefKeyLastUpdateTime]

            StaticsContent(staticDataModelJson ?: "", lastUpdateTime)
        }
    }
}