package com.dox.fpoweroff.manager

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.dox.fpoweroff.service.EventListenerService
import com.dox.fpoweroff.utility.Constants
import javax.inject.Inject


class PermissionManager @Inject constructor() {
    private fun isAccessibilityPermissionGranted(context: Context, accessibilityService: Class<*> = EventListenerService::class.java): Boolean {
        val expectedComponentName = ComponentName(context, accessibilityService)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServicesSetting?.contains(expectedComponentName.flattenToString()) == true
    }

    private fun isNotificationPolicyPermissionGranted(context: Context): Boolean{
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun startSpecialPermissionActivity(context: Context, specialPermission: Constants.SpecialPermission) {
        val action = when (specialPermission) {
            Constants.SpecialPermission.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            Constants.SpecialPermission.DND -> Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
        }
        val intent = Intent(action)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        Toast.makeText(context, "Please grant ${specialPermission.value} permission", Toast.LENGTH_SHORT).show()
    }

    fun checkAccessibilityPermission(context: Context): Boolean{
        return if (!isAccessibilityPermissionGranted(context)) {
            startSpecialPermissionActivity(context, Constants.SpecialPermission.ACCESSIBILITY)
            false
        } else {
            true
        }
    }

    fun checkDNDPermission(context: Context): Boolean{
        return if (!isNotificationPolicyPermissionGranted(context)) {
            startSpecialPermissionActivity(context, Constants.SpecialPermission.DND)
            false
        } else {
            true
        }
    }
}