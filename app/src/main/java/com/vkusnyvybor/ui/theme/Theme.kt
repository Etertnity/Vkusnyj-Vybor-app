package com.vkusnyvybor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations
import com.vkusnyvybor.ui.theme.engine.ThemeEngine
import java.io.File

@Composable
fun VkusnyVyborTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeConfig by ThemeEngine.currentTheme.collectAsState()
    val isDark = when (themeConfig.forceDark) { true -> true; false -> false; null -> darkTheme }

    val baseColorScheme = when {
        themeConfig.useDynamicColor && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> themeConfig.darkScheme
        else -> themeConfig.lightScheme
    }

    // Когда включены обои — делаем фон прозрачным, чтобы картинка просвечивала
    // сквозь дефолтные Scaffold/Surface (у них containerColor = colorScheme.background).
    val hasWallpaper = !themeConfig.decorations.wallpaperPath.isNullOrEmpty()
    val colorScheme = if (hasWallpaper) baseColorScheme.copy(background = Color.Transparent) else baseColorScheme

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
        MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes) {
            WallpaperHost(
                path = themeConfig.decorations.wallpaperPath,
                opacity = themeConfig.decorations.wallpaperOpacity,
                scale = themeConfig.decorations.wallpaperScale,
                offsetXFraction = themeConfig.decorations.wallpaperOffsetX,
                offsetYFraction = themeConfig.decorations.wallpaperOffsetY,
                baseBackground = baseColorScheme.background
            ) { content() }
        }
    }
}

@Composable
private fun WallpaperHost(
    path: String?,
    opacity: Float,
    scale: Float,
    offsetXFraction: Float,
    offsetYFraction: Float,
    baseBackground: Color,
    content: @Composable () -> Unit
) {
    if (path.isNullOrEmpty() || !File(path).exists()) {
        content()
        return
    }
    BoxWithConstraints(
        Modifier.fillMaxSize().background(baseBackground)
    ) {
        val density = LocalDensity.current
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val ctx = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(File(path)).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.coerceIn(0.25f, 4f),
                    scaleY = scale.coerceIn(0.25f, 4f),
                    translationX = wPx * offsetXFraction.coerceIn(-1f, 1f),
                    translationY = hPx * offsetYFraction.coerceIn(-1f, 1f)
                )
                .alpha(opacity.coerceIn(0f, 1f))
        )
        Box(Modifier.fillMaxSize()) { content() }
    }
}
