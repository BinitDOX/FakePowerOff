package com.dox.fpoweroff.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeManager @Inject constructor() {
    private val _practiceInput = MutableStateFlow("")
    val practiceInput = _practiceInput.asStateFlow()

    var practiceSequence: String? = null
    var onPracticeSuccessListener: (() -> Unit)? = null

    val isPracticeModeActive: Boolean
        get() = practiceSequence != null

    fun startPractice(sequence: String, onSuccess: () -> Unit) {
        practiceSequence = sequence
        onPracticeSuccessListener = onSuccess
        _practiceInput.value = ""
    }

    fun stopPractice() {
        practiceSequence = null
        onPracticeSuccessListener = null
        _practiceInput.value = ""
    }

    fun updateInput(input: String) {
        _practiceInput.value = input
    }
}