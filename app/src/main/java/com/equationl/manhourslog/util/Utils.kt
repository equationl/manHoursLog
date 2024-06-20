package com.equationl.manhourslog.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object Utils {
    val randomColor
        get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
}