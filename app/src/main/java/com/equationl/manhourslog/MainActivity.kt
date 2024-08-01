package com.equationl.manhourslog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equationl.manhourslog.constants.Route
import com.equationl.manhourslog.ui.theme.ManHoursLogTheme
import com.equationl.manhourslog.ui.view.HomeNavHost
import com.equationl.manhourslog.widget.common.constant.WidgetConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openType = intent.getStringExtra(WidgetConstants.OPEN_ACTIVITY_TYPE)

        enableEdgeToEdge()
        setContent {
            ManHoursLogTheme {
                if (openType == WidgetConstants.OpActivityType.STATICS.name) {
                    HomeNavHost(startDestination = Route.STATISTIC)
                }
                else {
                    HomeNavHost()
                }
            }
        }
    }
}