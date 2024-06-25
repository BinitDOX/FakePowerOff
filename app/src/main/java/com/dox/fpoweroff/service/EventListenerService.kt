package com.dox.fpoweroff.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dox.fpoweroff.service.event.DialogCloseEvent
import com.dox.fpoweroff.service.event.PowerMenuOverrideEvent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EventListenerService : AccessibilityService() {
    @Inject
    lateinit var powerMenuOverrideEvent: PowerMenuOverrideEvent

    @Inject
    lateinit var dialogCloseEvent: DialogCloseEvent

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                powerMenuOverrideEvent.handlePowerMenuEvent(this, event, ::performGlobalAction)
            }
            else -> Timber.w("[${::onAccessibilityEvent.name}]" +
                    " Event type not handled: ${event.eventType}")
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null) {
            dialogCloseEvent.handleTriggerEvent(event)
        }
        return super.onKeyEvent(event)
    }


    // For Debugging
    private fun printEvent(event: AccessibilityEvent) {
        val parentNodeInfo = event.source
        if(parentNodeInfo != null) {
            Timber.d("[G] -------------------------------")
            printNodeInfoWithPath(parentNodeInfo)
        }
    }

    private fun printNodeInfoWithPath(nodeInfo: AccessibilityNodeInfo, path: String = "") {
        for (i in 0 until nodeInfo.childCount) {
            val childNode = nodeInfo.getChild(i)
            if (childNode != null) {
                val childPath = "$path.$i"
                val isInputField = childNode.isEditable // || childNode.isTextEntryKey
                Timber.d("[V] Path:[$childPath] Info:[${childNode.viewIdResourceName} ${childNode.text}" +
                        " ${childNode.contentDescription}] IsClickable:${childNode.isCheckable||childNode.isClickable}" +
                        " IsInput:${isInputField}")
                printNodeInfoWithPath(childNode, childPath)
            }
        }
    }

    override fun onInterrupt() {}
}
