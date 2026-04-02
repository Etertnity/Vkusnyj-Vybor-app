package com.vkusnyvybor.ui.theme.engine

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ThemeConfig(
    val id: String,
    val name: String,
    val description: String = "",
    val previewColors: List<Color> = emptyList(),
    val lightScheme: ColorScheme,
    val darkScheme: ColorScheme,
    val typography: Typography? = null,
    val shapes: Shapes? = null,
    val useDynamicColor: Boolean = false,
    val forceDark: Boolean? = null,
    val decorations: ThemeDecorations = ThemeDecorations()
)

data class ThemeDecorations(
    val scanlineEffect: Boolean = false,
    val scanlineColor: Color = Color.White.copy(alpha = 0.03f),
    val glowAccent: Boolean = false,
    val glowColor: Color = Color.Transparent,
    val backgroundTexture: BackgroundTexture = BackgroundTexture.NONE,
    val cardStyle: CardStyle = CardStyle.ROUNDED,
    val dividerStyle: DividerStyle = DividerStyle.SIMPLE,
    val themeLogo: @Composable (() -> Unit)? = null // Слот для программной отрисовки логотипа
)

enum class BackgroundTexture { NONE, NOISE, GRID, SCANLINES, HEXAGONAL }
enum class CardStyle { ROUNDED, CUT_CORNER, SHARP }
enum class DividerStyle { SIMPLE, NEON_LINE, BIOHAZARD_STRIPE, TERMINAL_DOTS }

// Shapes... (CyberShapes, TerminalShapes, UmbrellaShapes остаются прежними)
val CyberShapes = Shapes(
    extraSmall = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
    small = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
    medium = CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp),
    large = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp),
    extraLarge = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp)
)

val TerminalShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(6.dp)
)

val UmbrellaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = CutCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
    large = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
    extraLarge = CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
)

val LocalThemeDecorations = compositionLocalOf { ThemeDecorations() }
