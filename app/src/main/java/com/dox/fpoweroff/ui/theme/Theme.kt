@file:Suppress("DEPRECATION")

package com.dox.fpoweroff.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = AccentColorDark,
    secondary = PurpleGrey80,
    tertiary = BarsColorDark,

    background = BackgroundColorDark,

    onPrimary = BarsColorDark,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onSurfaceVariant = Color.White,
    onBackground = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentColorLight,
    secondary = PurpleGrey40,
    tertiary = BarsColorLight,

    background = BackgroundColorLight,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onSurfaceVariant =Color.White,
    onBackground = Color.Black,
)

@Composable
fun FPowerOffTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        val systemUIController = rememberSystemUiController()
        val statusBarColor = colorScheme.tertiary
        val navigationBarColor = colorScheme.tertiary

        SideEffect {
            systemUIController.setStatusBarColor(statusBarColor)
            systemUIController.setNavigationBarColor(navigationBarColor)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}