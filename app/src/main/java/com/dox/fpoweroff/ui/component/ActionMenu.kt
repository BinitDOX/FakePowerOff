package com.dox.fpoweroff.ui.component

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dox.fpoweroff.ui.data.ActionItem
import com.dox.fpoweroff.ui.data.OverflowMode


// Should be used in a RowScope
@Composable
fun ActionMenu(
    items: List<ActionItem>,
    numIcons: Int = 3, // Includes overflow menu icon, may be overridden by NEVER_OVERFLOW
    menuVisible: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    if (items.isEmpty()) {
        return
    }
    // Decide how many action items to show as icons
    val (appbarActions, overflowActions) = remember(items, numIcons) {
        separateIntoIconAndOverflow(items, numIcons)
    }

    for (i in appbarActions.indices) {
        val item = appbarActions[i]
        key(item.hashCode()) {
            val name = stringResource(item.name)
            if (item.icon != null) {
                IconButton(onClick = item.doAction) {
                    Icon(item.icon, name)
                }
            } else {
                TextButton(
                    onClick = item.doAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    )
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    if (overflowActions.isNotEmpty()) {
        IconButton(onClick = { menuVisible.value = true }) {
            Icon(Icons.Default.MoreVert, "Menu Button")
        }
        DropdownMenu(
            modifier = Modifier.background(MaterialTheme.colorScheme.tertiary),
            expanded = menuVisible.value,
            onDismissRequest = { menuVisible.value = false },
        ) {
            for (i in overflowActions.indices) {
                val item = overflowActions[i]
                key(item.hashCode()) {
                    DropdownMenuItem(
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onTertiary,
                            leadingIconColor = MaterialTheme.colorScheme.onTertiary,
                        ),
                        text = { Text(
                            text = stringResource(item.name),
                            style = MaterialTheme.typography.labelMedium
                        ) },
                        onClick = {
                            menuVisible.value = false
                            item.doAction()
                        },
                        leadingIcon = item.icon?.let {
                            { Icon(it, stringResource(item.name)) }
                        },
                    )
                }
            }
        }
    }
}

private fun separateIntoIconAndOverflow(
    items: List<ActionItem>,
    numIcons: Int
): Pair<List<ActionItem>, List<ActionItem>> {
    var (iconCount, overflowCount, preferIconCount) = Triple(0, 0, 0)
    for (i in items.indices) {
        val item = items[i]
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> iconCount++
            OverflowMode.IF_NECESSARY -> preferIconCount++
            OverflowMode.ALWAYS_OVERFLOW -> overflowCount++
            OverflowMode.NOT_SHOWN -> {}
        }
    }

    val needsOverflow = iconCount + preferIconCount > numIcons || overflowCount > 0
    val actionIconSpace = numIcons - (if (needsOverflow) 1 else 0)

    val iconActions = ArrayList<ActionItem>()
    val overflowActions = ArrayList<ActionItem>()

    var iconsAvailableBeforeOverflow = actionIconSpace - iconCount
    for (i in items.indices) {
        val item = items[i]
        when (item.overflowMode) {
            OverflowMode.NEVER_OVERFLOW -> {
                iconActions.add(item)
            }
            OverflowMode.ALWAYS_OVERFLOW -> {
                overflowActions.add(item)
            }
            OverflowMode.IF_NECESSARY -> {
                if (iconsAvailableBeforeOverflow > 0) {
                    iconActions.add(item)
                    iconsAvailableBeforeOverflow--
                } else {
                    overflowActions.add(item)
                }
            }
            OverflowMode.NOT_SHOWN -> {
                // skip
            }
        }
    }
    return Pair(iconActions, overflowActions)
}