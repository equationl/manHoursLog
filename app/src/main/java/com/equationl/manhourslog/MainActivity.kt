package com.equationl.manhourslog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equationl.manhourslog.ui.theme.ManHoursLogTheme
import com.equationl.manhourslog.ui.view.HomeNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ManHoursLogTheme {
                HomeNavHost()
            }
        }
    }
}