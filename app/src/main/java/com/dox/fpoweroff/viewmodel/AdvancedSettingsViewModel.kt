package com.dox.fpoweroff.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.manager.ShizukuManager
import com.dox.fpoweroff.service.event.PowerMenuOverrideEvent
import com.dox.fpoweroff.utility.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdvancedSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val permissionManager: PermissionManager,
    private val shizukuManager: ShizukuManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _isTestingVoidMode = MutableStateFlow(false)
    val isTestingVoidMode = _isTestingVoidMode.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update { currentState ->
            currentState.copy(
                isDndEnabled = sharedPreferencesManager.get(Constants.DND_MODE_ENABLED_KEY).toBoolean(),
                isLockDeviceEnabled = sharedPreferencesManager.get(Constants.LOCK_DEVICE_ENABLED_KEY).toBoolean(),
                detectPackageName = sharedPreferencesManager.get(Constants.DETECT_PACKAGE_NAME_KEY) ?: Constants.DETECT_PACKAGE_NAME_DEFAULT,
                detectKeywords = sharedPreferencesManager.get(Constants.DETECT_KEYWORDS_KEY) ?: Constants.DETECT_KEYWORDS_DEFAULT,
                isVoidModeEnabled = sharedPreferencesManager.get(Constants.VOID_MODE_ENABLED_KEY).toBoolean()
            )
        }
    }

    fun setDndEnabled(isEnabled: Boolean) {
        var finalState = isEnabled
        if (isEnabled) {
            if (!permissionManager.checkDNDPermission(context)) {
                finalState = false
            }
        }
        _uiState.update { it.copy(isDndEnabled = finalState) }
        saveDndEnabled()
    }

    fun setLockDeviceEnabled(isEnabled: Boolean) {
        _uiState.update { it.copy(isLockDeviceEnabled = isEnabled) }
        saveLockDeviceEnabled()
    }

    fun setVoidModeEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            if (!shizukuManager.isShizukuAvailable()) {
                Toast.makeText(context, "Shizuku not running or not installed", Toast.LENGTH_SHORT).show()
                _uiState.update { it.copy(isVoidModeEnabled = false) }
                return
            }

            if (!shizukuManager.checkPermission()) {
                shizukuManager.requestPermission()
                _uiState.update { it.copy(isVoidModeEnabled = false) }
                return
            }
        }

        _uiState.update { it.copy(isVoidModeEnabled = isEnabled) }
        saveVoidModeEnabled()
    }

    fun toggleVoidModeTest() {
        viewModelScope.launch {
            if (_isTestingVoidMode.value) {
                shizukuManager.disableVoidMode()
                _isTestingVoidMode.emit(false)
            } else {
                if (shizukuManager.checkPermission()) {
                    shizukuManager.enableVoidMode()
                    _isTestingVoidMode.emit(true)
                } else {
                    Toast.makeText(context, "Shizuku permission required for test.", Toast.LENGTH_SHORT).show()
                    shizukuManager.requestPermission()
                }
            }
        }
    }

    fun onDetectPackageNameChanged(packageName: String) {
        _uiState.update { it.copy(detectPackageName = packageName) }
    }

    fun onDetectKeywordsChanged(keywords: String) {
        _uiState.update { it.copy(detectKeywords = keywords) }
    }

    private fun saveDndEnabled() {
        sharedPreferencesManager.save(Constants.DND_MODE_ENABLED_KEY, _uiState.value.isDndEnabled.toString())
        PowerMenuOverrideEvent.dndModeEnabled = _uiState.value.isDndEnabled
    }

    private fun saveLockDeviceEnabled() {
        sharedPreferencesManager.save(Constants.LOCK_DEVICE_ENABLED_KEY, _uiState.value.isLockDeviceEnabled.toString())
        PowerMenuOverrideEvent.lockDeviceEnabled = _uiState.value.isLockDeviceEnabled
    }

    fun saveDetectPackageName() {
        sharedPreferencesManager.save(Constants.DETECT_PACKAGE_NAME_KEY, _uiState.value.detectPackageName)
        PowerMenuOverrideEvent.detectPackageName = _uiState.value.detectPackageName
    }

    fun saveDetectKeywords() {
        sharedPreferencesManager.save(Constants.DETECT_KEYWORDS_KEY, _uiState.value.detectKeywords)
        PowerMenuOverrideEvent.detectKeywords = _uiState.value.detectKeywords.split(',').map { it.trim() }
    }

    private fun saveVoidModeEnabled() {
        sharedPreferencesManager.save(Constants.VOID_MODE_ENABLED_KEY, _uiState.value.isVoidModeEnabled.toString())
        PowerMenuOverrideEvent.voidModeEnabled = _uiState.value.isVoidModeEnabled
    }
}

data class AdvancedSettingsUiState(
    val isDndEnabled: Boolean = false,
    val isLockDeviceEnabled: Boolean = false,
    val detectPackageName: String = Constants.DETECT_PACKAGE_NAME_DEFAULT,
    val detectKeywords: String = Constants.DETECT_KEYWORDS_DEFAULT,
    val isVoidModeEnabled: Boolean = false
)