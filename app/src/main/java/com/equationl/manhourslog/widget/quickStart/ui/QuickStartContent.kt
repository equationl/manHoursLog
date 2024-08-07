package com.equationl.manhourslog.widget.quickStart.ui

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import com.equationl.manhourslog.MainActivity
import com.equationl.manhourslog.ui.view.home.state.LogState
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.toJson
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.openActivityTypeKey
import com.equationl.manhourslog.widget.quickStart.callback.QuickStartWidgetCallback

@Composable
fun QuickStartContent(logState: LogState) {
    GlanceTheme {
        Column(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .appWidgetBackground()
                .background(GlanceTheme.colors.widgetBackground)
                .appWidgetBackgroundCornerRadius()
                .clickable(
                    actionStartActivity<MainActivity>(
                        actionParametersOf(
                            openActivityTypeKey to WidgetConstants.OpActivityType.HOME.name,
                        )
                    )
                ),
        ) {
            Text(text = if (logState.isStart) "Start at ${logState.startTime?.formatDateTime("HH:mm:ss") ?: ""}" else "Click below to record")
            Spacer(modifier = GlanceModifier.height(16.dp))
            Column(
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier
                    .size(50.dp)
                    .cornerRadius(50.dp)
                    .background(GlanceTheme.colors.primary)
                    .clickable(onClick = actionRunCallback<QuickStartWidgetCallback>(
                        actionParametersOf(
                            WidgetConstants.actionKey to WidgetConstants.TOGGLE_STATE,
                            WidgetConstants.logStateKey to logState.toJson()
                        )
                    ))
            ) {
                Text(
                    text = if (logState.isStart) "Finish" else "Start",
                    style = TextDefaults.defaultTextStyle.copy(color = GlanceTheme.colors.onPrimary)
                )
            }


        }
    }
}

private fun GlanceModifier.appWidgetBackgroundCornerRadius(): GlanceModifier {
    if (Build.VERSION.SDK_INT >= 31) {
        cornerRadius(android.R.dimen.system_app_widget_background_radius)
    }
    return cornerRadius(16.dp)
}