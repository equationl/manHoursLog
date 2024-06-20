package com.equationl.manhourslog.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object Utils {
    val randomColor
        get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    fun changeScreenOrientation(context: Context, orientation: Int) {
        Log.i("el", "call changeScreenOrientation with $orientation")

        val activity = context.findActivity() ?: return
        //val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
    }
}