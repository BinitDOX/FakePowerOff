package com.dox.fpoweroff.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) {
            return
        }

        crashlytics.log(message)

        if (t != null) {
            crashlytics.recordException(t)
        }
    }
}