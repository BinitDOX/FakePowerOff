package com.dox.fpoweroff.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dox.fpoweroff.manager.KeywordDetectionManager
import com.dox.fpoweroff.manager.PermissionManager
import com.dox.fpoweroff.manager.PracticeManager
import com.dox.fpoweroff.manager.SharedPreferencesManager
import com.dox.fpoweroff.service.event.DialogCloseEvent
import com.dox.fpoweroff.service.event.PowerMenuOverrideEvent
import com.dox.fpoweroff.utility.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecommendationLevel {
    MOST_RECOMMENDED, RECOMMENDED, NONE
}

data class RecommendedKeyword(
    val keyword: String,
    val level: RecommendationLevel
)

@HiltViewModel
class TutorialViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val permissionManager: PermissionManager,
    private val practiceManager: PracticeManager,
    private val keywordDetectionManager: KeywordDetectionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState = _uiState.asStateFlow()

    private var detectionJob: Job? = null

    init {
        checkAccessibilityPermission()
        practiceManager.practiceInput
            .onEach { input -> _uiState.update { it.copy(practiceInput = input) } }
            .launchIn(viewModelScope)
        keywordDetectionManager.detectedKeywords
            .onEach { keywords ->
                // Process and categorize the keywords here
                val recommended = keywords.map { keyword ->
                    val level = when {
                        keyword.contains("emergency", ignoreCase = true) -> RecommendationLevel.MOST_RECOMMENDED
                        keyword.contains("sos", ignoreCase = true) -> RecommendationLevel.MOST_RECOMMENDED
                        keyword.contains("power off", ignoreCase = true) ||
                                keyword.contains("shutdown", ignoreCase = true) ||
                                keyword.contains("restart", ignoreCase = true) -> RecommendationLevel.RECOMMENDED
                        else -> RecommendationLevel.NONE
                    }
                    RecommendedKeyword(keyword, level)
                }.sortedBy { it.level.ordinal }

                _uiState.update { it.copy(recommendedKeywords = recommended) }
            }
            .launchIn(viewModelScope)
    }

    fun checkAccessibilityPermission() {
        _uiState.update { it.copy(isAccessibilityEnabled = permissionManager.checkAccessibilityPermission(context, false)) }
    }

    fun onSequenceCharAdded(char: Char) {
        if (_uiState.value.recoverySequence.length < 10) {
            _uiState.update { it.copy(recoverySequence = it.recoverySequence + char) }
        }
    }

    fun onSequenceCharDeleted() {
        _uiState.update { it.copy(recoverySequence = it.recoverySequence.dropLast(1)) }
    }

    fun startPractice() {
        practiceManager.startPractice(
            sequence = _uiState.value.recoverySequence,
            onSuccess = {
                _uiState.update { it.copy(practiceAttempts = it.practiceAttempts + 1) }
            }
        )
    }

    fun stopPractice() {
        practiceManager.stopPractice()
    }

    fun resetPractice() {
        _uiState.update { it.copy(practiceAttempts = 0, practiceInput = "") }
        startPractice()
    }

    fun startKeywordDetection() {
        detectionJob?.cancel()
        detectionJob = viewModelScope.launch {
            delay(3000L)
            keywordDetectionManager.startDetection()
        }
    }

    fun stopKeywordDetection() {
        detectionJob?.cancel()
        keywordDetectionManager.stopDetection()
    }

    fun onKeywordSelected(keyword: String) {
        _uiState.update { it.copy(selectedKeyword = keyword) }
    }

    fun onTutorialComplete() {
        val finalSequence = _uiState.value.recoverySequence
        val finalKeyword = _uiState.value.selectedKeyword

        sharedPreferencesManager.save(Constants.DIALOG_CLOSE_TRIGGER_SEQUENCE_KEY, finalSequence)
        sharedPreferencesManager.save(Constants.DETECT_KEYWORDS_KEY, finalKeyword)
        sharedPreferencesManager.save(Constants.TUTORIAL_COMPLETED_KEY, "true")
        sharedPreferencesManager.save(Constants.F_POWER_OFF_ENABLED_KEY, "true")

        DialogCloseEvent.dialogCloseTriggerSequence = finalSequence
        PowerMenuOverrideEvent.detectKeywords = finalKeyword.split(',').map { it.trim() }
        PowerMenuOverrideEvent.fPowerOffEnabled = true
    }

    fun onFinalSimulationCompleted() {
        _uiState.update { it.copy(isFinalSimulationCompleted = true) }
    }
}

data class TutorialUiState(
    val isAccessibilityEnabled: Boolean = false,
    val recoverySequence: String = "",
    val practiceAttempts: Int = 0,
    val practiceInput: String = "",
    val selectedKeyword: String = "",
    val recommendedKeywords: List<RecommendedKeyword> = emptyList(),
    val isFinalSimulationCompleted: Boolean = false
)