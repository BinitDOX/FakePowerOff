package com.dox.fpoweroff.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.service.event.DialogCloseEvent
import com.dox.fpoweroff.service.event.PowerMenuOverrideEvent
import com.dox.fpoweroff.utility.Constants.DETECT_KEYWORDS_KEY
import com.dox.fpoweroff.utility.Constants.DETECT_PACKAGE_NAME_KEY
import com.dox.fpoweroff.utility.Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY
import com.dox.fpoweroff.utility.Constants.DND_MODE_ENABLED_KEY
import com.dox.fpoweroff.utility.Constants.F_POWER_OFF_ENABLED_KEY
import com.dox.fpoweroff.utility.Constants.LOCK_DEVICE_ENABLED_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val permissionManager: PermissionManager
): ViewModel() {
    private val _fPowerOffEnabled = MutableStateFlow("")
    val fPowerOffEnabled = _fPowerOffEnabled.asStateFlow()

    private val _dialogCloseTriggerSequence = MutableStateFlow("")
    val dialogCloseTriggerSequence = _dialogCloseTriggerSequence.asStateFlow()

    private val _dndModeEnabled = MutableStateFlow("")
    val dndModeEnabled = _dndModeEnabled.asStateFlow()

    private val _lockDeviceEnabled = MutableStateFlow("")
    val lockDeviceEnabled = _lockDeviceEnabled.asStateFlow()

    private val _detectPackageName = MutableStateFlow("")
    val detectPackageName = _detectPackageName.asStateFlow()

    private val _detectKeywords = MutableStateFlow("")
    val detectKeywords = _detectKeywords.asStateFlow()

    init {
        getSettings()
    }


    fun setFPowerOffEnabled(value: String) {
        if(value.toBoolean()) {
            if(permissionManager.checkAccessibilityPermission(context))
                _fPowerOffEnabled.value = value
        } else {
            _fPowerOffEnabled.value = value
        }
    }

    fun setDialogCloseTriggerSequence(value: String) {
        _dialogCloseTriggerSequence.value = value
    }

    fun setDNDModeEnabled(value: String) {
        if(value.toBoolean()) {
            if(permissionManager.checkDNDPermission(context))
                _dndModeEnabled.value = value
        } else {
            _dndModeEnabled.value = value
        }
    }

    fun setLockDeviceEnabled(value: String) {
        _lockDeviceEnabled.value = value
    }

    fun setDetectPackageName(value: String) {
        _detectPackageName.value = value
    }

    fun setDetectKeywords(value: String) {
        _detectKeywords.value = value
    }

    private fun getSettings() {
        _fPowerOffEnabled.value = sharedPreferencesManager.get(F_POWER_OFF_ENABLED_KEY) ?: ""
        _dialogCloseTriggerSequence.value = sharedPreferencesManager.get(DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY) ?: ""
        _dndModeEnabled.value = sharedPreferencesManager.get(DND_MODE_ENABLED_KEY) ?: ""
        _lockDeviceEnabled.value = sharedPreferencesManager.get(LOCK_DEVICE_ENABLED_KEY) ?: ""
        _detectPackageName.value = sharedPreferencesManager.get(DETECT_PACKAGE_NAME_KEY) ?: ""
        _detectKeywords.value = sharedPreferencesManager.get(DETECT_KEYWORDS_KEY) ?: ""
    }

    fun saveFPowerOffEnabled() {
        sharedPreferencesManager.save(F_POWER_OFF_ENABLED_KEY, _fPowerOffEnabled.value)
        PowerMenuOverrideEvent.fPowerOffEnabled = _fPowerOffEnabled.value.toBoolean()
    }

    fun saveDialogCloseTriggerSequence(){
        sharedPreferencesManager.save(DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY, _dialogCloseTriggerSequence.value)
        DialogCloseEvent.dialogCloseTriggerSequence = _dialogCloseTriggerSequence.value
    }

    fun saveDNDModeEnabled(){
        sharedPreferencesManager.save(DND_MODE_ENABLED_KEY, _dndModeEnabled.value)
        PowerMenuOverrideEvent.dndModeEnabled = _dndModeEnabled.value.toBoolean()
    }

    fun saveLockDeviceEnabled(){
        sharedPreferencesManager.save(LOCK_DEVICE_ENABLED_KEY, _lockDeviceEnabled.value)
        PowerMenuOverrideEvent.lockDeviceEnabled = _lockDeviceEnabled.value.toBoolean()
    }

    fun saveDetectPackageName() {
        sharedPreferencesManager.save(DETECT_PACKAGE_NAME_KEY, _detectPackageName.value)
        PowerMenuOverrideEvent.detectPackageName = _detectPackageName.value
    }

    fun saveDetectKeywords() {
        sharedPreferencesManager.save(DETECT_KEYWORDS_KEY, _detectKeywords.value)
        PowerMenuOverrideEvent.detectKeywords = _detectKeywords.value.split(',').map { it.trim() }
    }
}
