package com.dox.fpoweroff.manager

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InAppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var onUpdateDownloaded: (() -> Unit)? = null

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            onUpdateDownloaded?.invoke()
        }
    }

    init {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun checkForUpdate(
        activity: Activity,
        onUpdateDownloaded: () -> Unit
    ) {
        this.onUpdateDownloaded = onUpdateDownloaded

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appUpdateInfo = appUpdateManager.requestAppUpdateInfo()

                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    withContext(Dispatchers.Main) {
                        appUpdateManager.startUpdateFlow(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    }
                    Timber.d("Flexible update flow started.")
                } else {
                    Timber.d("No flexible update available.")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to check for in-app update.")
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun cleanUp() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }
}