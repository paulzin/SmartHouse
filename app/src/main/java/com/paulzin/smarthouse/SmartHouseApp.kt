package com.paulzin.smarthouse

import android.app.Application
import io.paperdb.Paper
import timber.log.Timber


class SmartHouseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Paper.init(this)
    }
}