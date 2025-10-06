package com.dox.fpoweroff.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.fpoweroff.R // <-- IMPORT R
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.manager.TestOverlayManager
import com.dox.fpoweroff.manager.TestSequenceManager
import com.dox.fpoweroff.service.event.PowerMenuOverrideEvent
import com.dox.fpoweroff.utility.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val permissionManager: PermissionManager,
    val testOverlayManager: TestOverlayManager,
    val testSequenceManager: TestSequenceManager
) : ViewModel() {

    private val _isTutorialCompleted = MutableStateFlow(false)
    val isTutorialCompleted = _isTutorialCompleted.asStateFlow()

    private val _isFpoEnabled = MutableStateFlow(false)
    val isFpoEnabled = _isFpoEnabled.asStateFlow()

    private val _triggerSequence = MutableStateFlow(Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT)
    val triggerSequence = _triggerSequence.asStateFlow()

    private val _toastEventChannel = Channel<Int>()
    val toastEventFlow = _toastEventChannel.receiveAsFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _isTutorialCompleted.value = sharedPreferencesManager.get(Constants.TUTORIAL_COMPLETED_KEY).toBoolean()
        _isFpoEnabled.value = sharedPreferencesManager.get(Constants.F_POWER_OFF_ENABLED_KEY).toBoolean()
        _triggerSequence.value = sharedPreferencesManager.get(Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY) ?: Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_DEFAULT
    }

    fun setFpoEnabled(isEnabled: Boolean) {
        _isFpoEnabled.value = isEnabled
        sharedPreferencesManager.save(Constants.F_POWER_OFF_ENABLED_KEY, isEnabled.toString())
        PowerMenuOverrideEvent.fPowerOffEnabled = isEnabled
    }

    fun runTestSequence(context: Context) {
        if (checkAccessibilityPermission(context)) {
            testSequenceManager.startTestSequence(
                context = context,
                sequenceToTest = _triggerSequence.value,
                onSuccess = { }
            )
        }
    }

    fun checkAccessibilityPermission(context: Context): Boolean{
        val granted = permissionManager.checkAccessibilityPermission(context, false)
        if(!granted){
            setFpoEnabled(false)
            viewModelScope.launch {
                _toastEventChannel.send(R.string.toast_accessibility_required)
            }
        }
        return granted
    }
}