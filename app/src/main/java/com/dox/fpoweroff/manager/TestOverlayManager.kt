package com.dox.fpoweroff.manager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestOverlayManager @Inject constructor() {

    private val _dismissEventFlow = MutableSharedFlow<Unit>()
    val dismissEventFlow = _dismissEventFlow.asSharedFlow()

    private val _inputStateFlow = MutableSharedFlow<String>()
    val inputStateFlow = _inputStateFlow.asSharedFlow()

    var isTestModeActive: Boolean = false
        private set

    var testSequence: String? = null

    fun startTest(sequence: String) {
        testSequence = sequence
        isTestModeActive = true
    }

    fun stopTest() {
        isTestModeActive = false
        testSequence = null
    }

    suspend fun reportDismissal() {
        _dismissEventFlow.emit(Unit)
    }

    suspend fun reportInput(input: String) {
        _inputStateFlow.emit(input)
    }
}