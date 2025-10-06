package com.dox.fpoweroff.ui.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class RouteItem(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : RouteItem("dashboard", "Dashboard", Icons.Outlined.Dashboard)
    data object Tutorial : RouteItem("tutorial", "Tutorial", Icons.Outlined.School)
    data object AdvancedSettings : RouteItem("advanced_settings", "Advanced", Icons.Outlined.Tune)
    data object About : RouteItem("about", "About", Icons.AutoMirrored.Outlined.HelpOutline)
}

val bottomNavItems = listOf(
    RouteItem.Dashboard,
    RouteItem.Tutorial,
    RouteItem.AdvancedSettings,
    RouteItem.About
)