package com.equationl.manhourslog.widget.statics.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.equationl.manhourslog.MainActivity
import com.equationl.manhourslog.R
import com.equationl.manhourslog.util.DateTimeUtil.formatDateTime
import com.equationl.manhourslog.util.DateTimeUtil.formatTime
import com.equationl.manhourslog.util.fromJson
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import com.equationl.manhourslog.widget.common.constant.WidgetConstants.openActivityTypeKey
import com.equationl.manhourslog.widget.common.model.StaticDataModel
import com.equationl.manhourslog.widget.statics.callback.StaticWidgetCallback

@Composable
fun StaticsContent(staticDataModelJson: String, lastUpdateTime: Long?) {

    val staticDataModel by remember(staticDataModelJson) {
        mutableStateOf(staticDataModelJson.fromJson<StaticDataModel>())
    }

    GlanceTheme {
        Scaffold(
            titleBar = {
                TitleContent()
            }
        ) {
            Column(
                modifier = GlanceModifier
                    .clickable(
                        actionStartActivity<MainActivity>(
                            actionParametersOf(
                                openActivityTypeKey to WidgetConstants.OpActivityType.STATICS.name,
                            )
                        )
                    )
            ) {
                Text(text = "Today: ${staticDataModel?.dayTotalTime?.formatTime() ?: ""}")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(text = "This month: ${staticDataModel?.monthTotalTime?.formatTime() ?: ""}")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(text = "This year: ${staticDataModel?.yearTotalTime?.formatTime() ?: ""}")
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Last update: ${lastUpdateTime?.formatDateTime() ?: "Never update"}",
                    style = TextStyle(
                        fontSize = 12.sp, color = ColorProvider(
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun TitleContent() {
    TitleBar(
        startIcon = ImageProvider(R.drawable.ic_analysis),
        title = "Statics",
        actions = {
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_refresh),
                contentDescription = "Refresh",
                backgroundColor = null,
                modifier = GlanceModifier.size(24.dp).padding(horizontal = 4.dp),
                onClick = actionRunCallback<StaticWidgetCallback>(
                    actionParametersOf(
                        WidgetConstants.actionKey to WidgetConstants.UPDATE_DATA,
                    )
                )
            )
        }
    )
}