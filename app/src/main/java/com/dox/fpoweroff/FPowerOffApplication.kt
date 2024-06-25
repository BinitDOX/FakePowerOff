package com.dox.fpoweroff

import android.app.Application
import com.dox.fpoweroff.logging.FileLoggingTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FPowerOffApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.plant(FileLoggingTree(this, false))
    }
}