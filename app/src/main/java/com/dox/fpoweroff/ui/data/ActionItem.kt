package com.dox.fpoweroff.ui.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionItem(
    @StringRes val name: Int,
    val icon: ImageVector? = null,
    val overflowMode: OverflowMode = OverflowMode.IF_NECESSARY,
    val doAction: () -> Unit,
) {
    operator fun invoke() = doAction()
}

enum class OverflowMode {
    NEVER_OVERFLOW, IF_NECESSARY, ALWAYS_OVERFLOW, NOT_SHOWN
}