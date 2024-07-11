package com.equationl.manhourslog.widget.statics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.equationl.manhourslog.R
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.equationl.manhourslog.util.fromJson
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import com.equationl.manhourslog.widget.common.model.StaticDataModel
import com.equationl.manhourslog.widget.statics.callback.StaticWidgetCallback

@Composable
fun StaticsContent(staticDataModelJson: String) {

    val staticDataModel by remember(staticDataModelJson) {
        mutableStateOf(staticDataModelJson.fromJson<StaticDataModel>())
    }

    GlanceTheme {
        Scaffold(
            titleBar = {
                TitleContent()
            }
        ) {
            Column {
                Text(text = "Today: ${staticDataModel?.dayTotalTime?.formatTime() ?: ""}")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(text = "This month: ${staticDataModel?.monthTotalTime?.formatTime() ?: ""}")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(text = "This year: ${staticDataModel?.yearTotalTime?.formatTime() ?: ""}")
            }
        }
    }
}

@Composable
private fun TitleContent() {
    TitleBar(
        startIcon = ImageProvider(R.drawable.ic_launcher_foreground),
        title = "Statics",
        actions = {
            CircleIconButton(
                imageProvider = ImageProvider(android.R.drawable.ic_popup_sync),
                contentDescription = "Refresh",
                modifier = GlanceModifier.size(24.dp),
                onClick = actionRunCallback<StaticWidgetCallback>(
                    actionParametersOf(
                        WidgetConstants.actionKey to WidgetConstants.UPDATE_DATA,
                    )
                )
            )
        }
    )
}