package com.dox.fpoweroff.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordDetectionManager @Inject constructor() {
    private val _detectedKeywords = MutableStateFlow<Set<String>>(emptySet())
    val detectedKeywords = _detectedKeywords.asStateFlow()

    var isDetectionModeActive: Boolean = false
        private set

    fun startDetection() {
        isDetectionModeActive = true
        _detectedKeywords.value = emptySet()
    }

    fun stopDetection() {
        isDetectionModeActive = false
    }

    fun reportKeywords(keywords: Set<String>) {
        _detectedKeywords.value += keywords
    }
}