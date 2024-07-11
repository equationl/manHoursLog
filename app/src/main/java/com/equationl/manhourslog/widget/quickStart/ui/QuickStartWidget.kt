package com.equationl.manhourslog.widget.quickStart.ui

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.equationl.manhourslog.ui.view.home.state.LogState
import com.equationl.manhourslog.util.datastore.DataKey
import com.equationl.manhourslog.util.datastore.DataStoreUtils.dataStore
import com.equationl.manhourslog.util.fromJson

class QuickStartWidget: GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val data = context.dataStore.data

        provideContent {
            val dataState by data.collectAsState(initial = null)
            val logState = dataState?.get(stringPreferencesKey(DataKey.LOG_STATE))?.fromJson<LogState>() ?: LogState(isStart = false)

            QuickStartContent(logState)
        }
    }
}