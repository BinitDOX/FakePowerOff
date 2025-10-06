package com.dox.fpoweroff.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dox.fpoweroff.ui.component.BottomBar
import com.dox.fpoweroff.ui.data.RouteItem
import com.dox.fpoweroff.ui.screen.AboutScreen
import com.dox.fpoweroff.ui.screen.AdvancedSettingsScreen
import com.dox.fpoweroff.ui.screen.DashboardScreen
import com.dox.fpoweroff.ui.screen.TutorialScreen
import com.dox.fpoweroff.ui.theme.FPowerOffTheme
import com.dox.fpoweroff.utility.Constants.START_ROUTE

@Composable
fun MainNavigation(
    startRoute: String = START_ROUTE,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(RouteItem.Dashboard.route) {
                DashboardScreen(navController)
            }
            composable(RouteItem.Tutorial.route) {
                TutorialScreen(navController)
            }
            composable(RouteItem.AdvancedSettings.route) {
                AdvancedSettingsScreen(navController)
            }
            composable(RouteItem.About.route) {
                AboutScreen(navController)
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