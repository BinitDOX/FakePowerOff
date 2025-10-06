package com.dox.fpoweroff.service.event

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageButton
import com.dox.fpoweroff.R
import com.dox.fpoweroff.manager.KeywordDetectionManager
import com.dox.fpoweroff.manager.OverlayManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.manager.TestSequenceManager
import com.dox.fpoweroff.utility.Constants
import timber.log.Timber
import javax.inject.Inject


class PowerMenuOverrideEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val overlayManager: OverlayManager,
    private val keywordDetectionManager: KeywordDetectionManager,
    private val testSequenceManager: TestSequenceManager
) {
    companion object {
        var fPowerOffEnabled = false
        var dndModeEnabled = false
        var lockDeviceEnabled = false
        var detectPackageName = ""
        var detectKeywords = listOf<String>()
    }

    init {
        fPowerOffEnabled = sharedPreferencesManager.get(Constants.F_POWER_OFF_ENABLED_KEY).toBoolean()
        dndModeEnabled = sharedPreferencesManager.get(Constants.DND_MODE_ENABLED_KEY).toBoolean()
        lockDeviceEnabled = sharedPreferencesManager.get(Constants.LOCK_DEVICE_ENABLED_KEY).toBoolean()
        detectPackageName = sharedPreferencesManager.get(Constants.DETECT_PACKAGE_NAME_KEY).toString()
        detectKeywords = sharedPreferencesManager.get(Constants.DETECT_KEYWORDS_KEY)?.split(',')
            ?.map { it.trim() } ?: Constants.DETECT_KEYWORDS_DEFAULT.split(',')
    }

    private var powerMenuOpen = false

    fun handlePowerMenuEvent(context: Context, event: AccessibilityEvent, performGlobalAction: (Int) -> Unit) {
        if (keywordDetectionManager.isDetectionModeActive) {
            handleKeywordDetection(event)
            return
        }

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
                        (tooltipText != null && detectKeywords.any { tooltipText.contains(it, ignoreCase = true) }) ||
                        (hintText != null && detectKeywords.any { hintText.contains(it, ignoreCase = true) }) ||
                        (contentDescription != null && detectKeywords.any { contentDescription.contains(it, ignoreCase = true) }) ||
                        (text != null && detectKeywords.any { text.contains(it, ignoreCase = true)})
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

    private fun handleKeywordDetection(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        if (packageName != Constants.SYSTEM_UI_PACKAGE) return

        try {
            val parentNodeInfo = event.source ?: return
            val foundKeywords = mutableSetOf<String>()
            val nodeQueue = mutableListOf<AccessibilityNodeInfo>()
            nodeQueue.add(parentNodeInfo)

            while (nodeQueue.isNotEmpty()) {
                val currentNode = nodeQueue.removeAt(0)
                for (i in 0 until currentNode.childCount) {
                    nodeQueue.add(currentNode.getChild(i))
                }
                currentNode.text?.toString()?.takeIf { it.isNotBlank() }?.let { foundKeywords.add(it) }
                currentNode.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { foundKeywords.add(it) }
            }

            if (foundKeywords.isNotEmpty()) {
                keywordDetectionManager.reportKeywords(foundKeywords)
            }
        } catch (e: Exception) {
            Timber.e("[${::handleKeywordDetection.name}] Error: $e")
        }
    }

    private fun beginShutdownSequence(context: Context, performGlobalAction: (Int) -> Unit) {
        overlayManager.showShutdownSequence(context)

        if (lockDeviceEnabled) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        if (dndModeEnabled) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }
}