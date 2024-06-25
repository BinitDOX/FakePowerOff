package com.dox.fpoweroff.service.event

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageButton
import androidx.core.content.ContextCompat.getSystemService
import com.dox.fpoweroff.R
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.service.EventListenerService
import com.dox.fpoweroff.utility.Constants
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class PowerMenuOverrideEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) {
    companion object {
        var fPowerOffEnabled = false
        var dndModeEnabled = false
        var lockDeviceEnabled = false

        var emptyDialog: Dialog? = null
    }

    init {
        fPowerOffEnabled = sharedPreferencesManager.get(Constants.F_POWER_OFF_ENABLED_KEY).toBoolean()
        dndModeEnabled = sharedPreferencesManager.get(Constants.DND_MODE_ENABLED_KEY).toBoolean()
        lockDeviceEnabled = sharedPreferencesManager.get(Constants.LOCK_DEVICE_ENABLED_KEY).toBoolean()
    }

    private var powerMenuOpen = false
    private val powerMenuKeywords = listOf("power off", "restart", "emergency", "poweroff")

    fun handlePowerMenuEvent(context: Context, event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        if (!fPowerOffEnabled) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == Constants.SYSTEM_UI_PACKAGE) {
            try {
                val parentNodeInfo = event.source ?: return
                val nodeQueue = mutableListOf<AccessibilityNodeInfo>()
                nodeQueue.add(parentNodeInfo)
                while (nodeQueue.isNotEmpty()) {
                    val currentNode = nodeQueue.removeAt(0)
                    for (i in 0 until currentNode.childCount) {
                        nodeQueue.add(currentNode.getChild(i))
                    }
                    val text = currentNode.text?.toString()
                    val tooltipText = currentNode.tooltipText?.toString()
                    val hintText = currentNode.hintText?.toString()
                    val contentDescription = currentNode.contentDescription?.toString()

                    if (!powerMenuOpen && (
                        (tooltipText != null && powerMenuKeywords.any { tooltipText.contains(it, ignoreCase = true) }) ||
                        (hintText != null && powerMenuKeywords.any { hintText.contains(it, ignoreCase = true) }) ||
                        (contentDescription != null && powerMenuKeywords.any { contentDescription.contains(it, ignoreCase = true) }) ||
                        (text != null && powerMenuKeywords.any { text.contains(it, ignoreCase = true)})
                    )){
                        powerMenuOpen = true
                        Timber.d("[${::handlePowerMenuEvent.name}] Detected")

                        performGlobalAction(GLOBAL_ACTION_BACK)
                        val dialog = Dialog(context)
                        dialog.setContentView(R.layout.power_off_menu)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
                        dialog.setOnDismissListener { powerMenuOpen = false }

                        val powerOffButton = dialog.findViewById<ImageButton>(R.id.btn_power_off)
                        val restartButton = dialog.findViewById<ImageButton>(R.id.btn_restart)
                        val emergencyButton = dialog.findViewById<ImageButton>(R.id.btn_emergency)

                        powerOffButton.setOnClickListener {
                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        restartButton.setOnClickListener {
                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        emergencyButton.setOnClickListener {
                            beginShutdownSequence(context, performGlobalAction)
                            dialog.dismiss()
                        }

                        dialog.show()
                        break
                    }
                }
            } catch (e: Exception) {
                Timber.e("[${::handlePowerMenuEvent.name}] Error: $e")
            }
        }
    }

    private fun beginShutdownSequence(context: Context, performGlobalAction: (Int) -> Unit) {
        val showShutdownDialog = showShutdownDialog(context)
        turnOnDNDMode(context)
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000L)
            vibrateDevice(context)
            lockDevice(performGlobalAction)
            delay(2000L)
            showShutdownDialog.dismiss()
            emptyDialog = showEmptyDialog(context)
        }
    }

    private fun lockDevice(performGlobalAction: (Int) -> Unit){
        if(lockDeviceEnabled){
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }

    private fun showEmptyDialog(context: Context) : Dialog {
        val emptyDialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)

        // Make the dialog full screen
        emptyDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        emptyDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        emptyDialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        emptyDialog.window?.setBackgroundDrawableResource(android.R.color.black)
        emptyDialog.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

        // Disable outside touch to dismiss
        emptyDialog.setCancelable(false)
        emptyDialog.setCanceledOnTouchOutside(false)

        emptyDialog.show()
        return emptyDialog
    }

    private fun showShutdownDialog(context: Context) : Dialog {
        // TODO: Make different dialog styles? Or maybe just choose from screenshot a static picture.
        val shutdownDialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        shutdownDialog.setContentView(R.layout.shutdown_dialog)

        // Make the dialog full screen
        shutdownDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        shutdownDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        shutdownDialog.window?.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY)
        shutdownDialog.window?.setBackgroundDrawableResource(android.R.color.black)
        shutdownDialog.window?.attributes?.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES


        // Disable outside touch to dismiss
        shutdownDialog.setCancelable(false)
        shutdownDialog.setCanceledOnTouchOutside(false)

        shutdownDialog.show()
        return shutdownDialog
    }

    private fun turnOnDNDMode(context: Context) {
        if(dndModeEnabled){
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }

    private fun vibrateDevice(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(700, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(700, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

}