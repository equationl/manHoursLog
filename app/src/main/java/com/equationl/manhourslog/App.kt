package com.equationl.manhourslog

import android.app.Application
import com.equationl.manhourslog.util.datastore.DataStoreUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DataStoreUtils.init(this)
    }
}