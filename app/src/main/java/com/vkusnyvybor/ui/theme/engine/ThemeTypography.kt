package com.vkusnyvybor.ui.theme.engine

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vkusnyvybor.R

// ══════════════════════════════════════════════════════════════
//  Шрифтовые семейства — требуют .ttf в res/font/
// ══════════════════════════════════════════════════════════════

// Marathon — терминальный моноширинный
val MarathonDisplayFont = FontFamily(
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold),
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal)
)
val MarathonBodyFont = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

// Cyberpunk — футуристический геометрический
val CyberpunkDisplayFont = FontFamily(
    Font(R.font.orbitron_bold, FontWeight.Bold)
)
val CyberpunkBodyFont = FontFamily(
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold)
)

// Umbrella — корпоративный военный
val UmbrellaDisplayFont = FontFamily(
    Font(R.font.oswald_bold, FontWeight.Bold)
)
val UmbrellaBodyFont = FontFamily(
    Font(R.font.source_sans_regular, FontWeight.Normal),
    Font(R.font.source_sans_semibold, FontWeight.SemiBold)
)

// ══════════════════════════════════════════════════════════════
//  Typography для каждой темы
// ══════════════════════════════════════════════════════════════

fun marathonTypography() = Typography(
    displayLarge = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = MarathonDisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    titleSmall = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp),
    bodyLarge = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = MarathonBodyFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

fun cyberpunkTypography() = Typography(
    displayLarge = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = 2.sp),
    displayMedium = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 1.5.sp),
    displaySmall = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 1.sp),
    headlineLarge = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.5.sp),
    headlineSmall = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = CyberpunkDisplayFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 1.5.sp),
    labelMedium = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = CyberpunkBodyFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.sp),
)

fun umbrellaTypography() = Typography(
    displayLarge = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = UmbrellaDisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.5.sp),
    labelMedium = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = UmbrellaBodyFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
)
