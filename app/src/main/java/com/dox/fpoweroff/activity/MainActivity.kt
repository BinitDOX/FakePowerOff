package com.dox.fpoweroff.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.ui.navigation.MainNavigation
import com.dox.fpoweroff.ui.theme.FPowerOffTheme
import com.dox.fpoweroff.utility.Constants
import com.dox.fpoweroff.utility.Constants.SpecialPermission.ACCESSIBILITY
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager.checkAccessibilityPermission(this)
        permissionManager.checkDNDPermission(this)

        setContent {
            FPowerOffTheme {
                MainNavigation()
            }
        }
    }
}