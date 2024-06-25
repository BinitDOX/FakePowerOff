package com.dox.fpoweroff.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dox.fpoweroff.ui.data.RouteItem
import com.dox.fpoweroff.ui.screen.HomeScreen
import com.dox.fpoweroff.ui.screen.SettingsScreen
import com.dox.fpoweroff.ui.theme.FPowerOffTheme
import com.dox.fpoweroff.utility.Constants.START_ROUTE

@Composable
fun MainNavigation(
    startRoute: String? = START_ROUTE,
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute ?: START_ROUTE,
        ) {
            composable(RouteItem.Home.route) {
                HomeScreen(navController)
            }
            composable(RouteItem.Settings.route) {
                SettingsScreen(navController)
            }
        }
    }
}


@Preview
@Composable
private fun MainNavigationPreview() {
    FPowerOffTheme {
        MainNavigation()
    }
}