package com.dox.fpoweroff

import android.app.Application
import com.dox.fpoweroff.logging.CrashlyticsTree
import com.dox.fpoweroff.logging.FileLoggingTree
import com.dox.fpoweroff.utility.Constants.PRIVATE_LOGS
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FPowerOffApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        Timber.plant(FileLoggingTree(this, PRIVATE_LOGS))
    }
}