package com.dox.fpoweroff.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dox.fpoweroff.manager.InAppUpdateManager
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.ui.data.RouteItem
import com.dox.fpoweroff.ui.navigation.MainNavigation
import com.dox.fpoweroff.ui.theme.FPowerOffTheme
import com.dox.fpoweroff.utility.Constants
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!permissionManager.checkAccessibilityPermission(this, false)) {
            permissionManager.showAccessibilityDisclosureDialog(this) {
                permissionManager.checkAccessibilityPermission(this)
                permissionManager.checkDNDPermission(this)
            }
        }

        val isTutorialCompleted =
            sharedPreferencesManager.get(Constants.TUTORIAL_COMPLETED_KEY).toBoolean()
        val startRoute =
            if (isTutorialCompleted) RouteItem.Dashboard.route else RouteItem.Tutorial.route

        setContent {
            FPowerOffTheme {
                MainNavigation(startRoute = startRoute)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager.checkForUpdate(this) {
            showUpdateDownloadedSnackbar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        inAppUpdateManager.cleanUp()
    }

    private fun showUpdateDownloadedSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { inAppUpdateManager.completeUpdate() }
            show()
        }
    }
}