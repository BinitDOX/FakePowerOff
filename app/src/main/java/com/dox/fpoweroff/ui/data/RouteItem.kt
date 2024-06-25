package com.dox.fpoweroff.ui.data

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class RouteItem(val route: String, val arguments: List<NamedNavArgument> = listOf()) {
    data object Home : RouteItem("home")
    data object Settings : RouteItem("settings")
}

val routeItems = listOf(
    RouteItem.Home,
    RouteItem.Settings
)