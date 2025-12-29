package com.dox.fpoweroff.service.event

import android.view.KeyEvent
import com.dox.fpoweroff.manager.OverlayManager
import com.dox.fpoweroff.manager.PracticeManager // <-- IMPORT
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.manager.ShizukuManager
import com.dox.fpoweroff.manager.TestOverlayManager
import com.dox.fpoweroff.service.EventListenerService
import com.dox.fpoweroff.utility.Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
import com.dox.fpoweroff.utility.Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DialogCloseEvent @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val overlayManager: OverlayManager,
    private val practiceManager: PracticeManager,
    private val testOverlayManager: TestOverlayManager,
    private val shizukuManager: ShizukuManager
) {
    companion object {
        var dialogCloseTriggerSequence: String? = null
    }

    init {
        dialogCloseTriggerSequence = sharedPreferencesManager
            .get(DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY) ?: DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
    }

    private var triggerState = 0
    private var triggerResetJob: Job? = null

    fun handleTriggerEvent(event: KeyEvent) {
        if (event.action != KeyEvent.ACTION_UP) return

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            when {
                // Prioritize Test Mode
                testOverlayManager.isTestModeActive -> handleTestTriggerEvent(event.keyCode)
                practiceManager.isPracticeModeActive -> handlePracticeTriggerEvent(event.keyCode)
                else -> {
                    if (!dialogCloseTriggerSequence.isNullOrBlank()) {
                        handleRealTriggerEvent(event.keyCode, dialogCloseTriggerSequence!!)
                    }
                }
            }
        }
    }

    private fun handleTestTriggerEvent(keyCode: Int) {
        val sequence = testOverlayManager.testSequence ?: return
        val expectedChar = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) 'U' else 'D'

        if (triggerState >= sequence.length || sequence[triggerState] != expectedChar) {
            triggerState = 0
            CoroutineScope(Dispatchers.Main).launch { testOverlayManager.reportInput("") }
        }

        if (triggerState < sequence.length && sequence[triggerState] == expectedChar) {
            triggerState++
            val currentInput = sequence.substring(0, triggerState)
            CoroutineScope(Dispatchers.Main).launch { testOverlayManager.reportInput(currentInput) }

            if (triggerState == sequence.length) {
                CoroutineScope(Dispatchers.Main).launch { testOverlayManager.reportDismissal() }
                triggerState = 0
            }
        }
        resetTriggerAfterDelay()
    }

    private fun handlePracticeTriggerEvent(keyCode: Int) {
        // A sequence must exist and not be empty to proceed
        val sequence = practiceManager.practiceSequence
        if (sequence.isNullOrEmpty()) {
            return // Do nothing if there's no sequence to practice
        }

        val expectedChar = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) 'U' else 'D'

        // Prevent crashing if triggerState somehow gets out of bounds
        if (triggerState >= sequence.length) {
            triggerState = 0
            practiceManager.updateInput("")
            return
        }

        if (sequence[triggerState] == expectedChar) {
            triggerState++
            val currentInput = sequence.substring(0, triggerState)
            practiceManager.updateInput(currentInput)

            if (triggerState == sequence.length) {
                practiceManager.onPracticeSuccessListener?.invoke()
                triggerState = 0
                practiceManager.updateInput("") // Clear UI after success
            }
        } else {
            // Wrong key press, reset
            triggerState = 0
            practiceManager.updateInput("")
        }

        resetTriggerAfterDelay()
    }

    private fun handleRealTriggerEvent(keyCode: Int, triggerSequence: String) {
        val expectedChar = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) 'U' else 'D'

        if (triggerSequence[triggerState].uppercaseChar() == expectedChar) {
            triggerState++
            if (triggerState == triggerSequence.length) {
                triggerState = 0
                CoroutineScope(Dispatchers.IO).launch {
                    shizukuManager.disableVoidMode()
                }
                overlayManager.hideOverlay(EventListenerService.getServiceContext() ?: return)
            }
        } else {
            triggerState = 0
        }

        resetTriggerAfterDelay()
    }

    private fun resetTriggerAfterDelay() {
        triggerResetJob?.cancel()
        triggerResetJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            if (triggerState > 0) {
                triggerState = 0
                if (practiceManager.isPracticeModeActive) practiceManager.updateInput("")
                if (testOverlayManager.isTestModeActive) testOverlayManager.reportInput("")
            }
        }
    }
}