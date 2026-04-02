package com.vkusnyvybor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations
import com.vkusnyvybor.ui.theme.engine.ThemeEngine

@Composable
fun VkusnyVyborTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeConfig by ThemeEngine.currentTheme.collectAsState()
    val isDark = when (themeConfig.forceDark) { true -> true; false -> false; null -> darkTheme }

    val colorScheme = when {
        themeConfig.useDynamicColor && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> themeConfig.darkScheme
        else -> themeConfig.lightScheme
    }

    val typography = themeConfig.typography ?: AppTypography
    val shapes = themeConfig.shapes ?: MaterialTheme.shapes

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalThemeDecorations provides themeConfig.decorations) {
        MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes, content = content)
    }
}
