package com.vkusnyvybor.ui.theme.engine

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object BuiltInThemes {
    val materialYou = ThemeConfig(
        id = "material_you", name = "Material You",
        description = "Динамические цвета из обоев устройства",
        previewColors = listOf(Color(0xFF6750A4), Color(0xFF625B71), Color(0xFF7D5260), Color(0xFFEADDFF)),
        useDynamicColor = true, lightScheme = lightColorScheme(), darkScheme = darkColorScheme()
    )

    private val uRed = Color(0xFFCE1126)
    val umbrellaCorp = ThemeConfig(
        id = "umbrella", name = "Umbrella Corp",
        description = "Resident Evil — военный шрифт, срезанные углы",
        previewColors = listOf(uRed, Color(0xFF1A1A1A), Color(0xFFF5F0EB), Color(0xFF8B0000)),
        typography = umbrellaTypography(), shapes = UmbrellaShapes,
        lightScheme = lightColorScheme(primary=Color(0xFFB01020),onPrimary=Color.White,primaryContainer=Color(0xFFFFDAD6),onPrimaryContainer=Color(0xFF410002),secondary=Color(0xFF6B6B6B),onSecondary=Color.White,secondaryContainer=Color(0xFFE8E8E8),onSecondaryContainer=Color(0xFF1F1F1F),tertiary=Color(0xFF8B0000),onTertiary=Color.White,background=Color(0xFFFFF8F7),onBackground=Color(0xFF1A1A1A),surface=Color(0xFFFFF8F7),onSurface=Color(0xFF1A1A1A),surfaceVariant=Color(0xFFF4DDDB),onSurfaceVariant=Color(0xFF534341),outline=Color(0xFF857370),outlineVariant=Color(0xFFD8C2BF)),
        darkScheme = darkColorScheme(primary=uRed,onPrimary=Color.White,primaryContainer=Color(0xFF5C0011),onPrimaryContainer=Color(0xFFFFDAD6),secondary=Color(0xFF8C8C8C),onSecondary=Color.Black,secondaryContainer=Color(0xFF3A3A3A),onSecondaryContainer=Color(0xFFD4D4D4),tertiary=Color(0xFFFF4444),onTertiary=Color.Black,background=Color(0xFF1A1A1A),onBackground=Color(0xFFF5F0EB),surface=Color(0xFF2A2A2A),onSurface=Color(0xFFF5F0EB),surfaceVariant=Color(0xFF333333),onSurfaceVariant=Color(0xFFAAAAAA),outline=Color(0xFF555555),outlineVariant=Color(0xFF3D3D3D)),
        decorations = ThemeDecorations(scanlineEffect=true,scanlineColor=uRed.copy(alpha=0.02f),glowAccent=true,glowColor=uRed.copy(alpha=0.08f),cardStyle=CardStyle.CUT_CORNER,dividerStyle=DividerStyle.BIOHAZARD_STRIPE,
            themeLogo = { UmbrellaLogo() }
        )
    )

    private val cCyan = Color(0xFF00F0FF); private val cMagenta = Color(0xFFFF00E5); private val cYellow = Color(0xFFFFE500)
    val cyberpunk = ThemeConfig(
        id = "cyberpunk", name = "Cyberpunk",
        description = "Неоновый киберпанк — циан, маджента, срезанные углы",
        previewColors = listOf(cCyan, cMagenta, cYellow, Color(0xFF0A0A12)),
        typography = cyberpunkTypography(), shapes = CyberShapes,
        lightScheme = lightColorScheme(primary=Color(0xFF006C7A),onPrimary=Color.White,primaryContainer=Color(0xFFA2EEFF),onPrimaryContainer=Color(0xFF001F26),secondary=Color(0xFF8B006D),onSecondary=Color.White,secondaryContainer=Color(0xFFFFD7F0),onSecondaryContainer=Color(0xFF370028),tertiary=Color(0xFF6D5E00),onTertiary=Color.White,background=Color(0xFFF0FBFF),onBackground=Color(0xFF0A0A12),surface=Color(0xFFF0FBFF),onSurface=Color(0xFF0A0A12),surfaceVariant=Color(0xFFD8EFF5),onSurfaceVariant=Color(0xFF3F484C),outline=Color(0xFF6F797C),outlineVariant=Color(0xFFBFC8CC)),
        darkScheme = darkColorScheme(primary=cCyan,onPrimary=Color(0xFF00222D),primaryContainer=Color(0xFF003844),onPrimaryContainer=cCyan,secondary=cMagenta,onSecondary=Color(0xFF2D0028),secondaryContainer=Color(0xFF44003C),onSecondaryContainer=cMagenta,tertiary=cYellow,onTertiary=Color(0xFF222200),background=Color(0xFF0A0A12),onBackground=Color(0xFFE0E0E8),surface=Color(0xFF12121E),onSurface=Color(0xFFE0E0E8),surfaceVariant=Color(0xFF1A1A28),onSurfaceVariant=Color(0xFF9999AA),outline=Color(0xFF444455),outlineVariant=Color(0xFF333344)),
        decorations = ThemeDecorations(scanlineEffect=true,scanlineColor=cCyan.copy(alpha=0.03f),glowAccent=true,glowColor=cCyan.copy(alpha=0.05f),backgroundTexture=BackgroundTexture.GRID,cardStyle=CardStyle.CUT_CORNER,dividerStyle=DividerStyle.NEON_LINE,
            themeLogo = { CyberpunkLogo() }
        )
    )

    private val mGreen = Color(0xFF00C853); private val mAmber = Color(0xFFFFAB00)
    val marathon = ThemeConfig(
        id = "marathon", name = "Marathon",
        description = "Ретрофутуризм — зелёный терминал, моноширинный шрифт",
        previewColors = listOf(mGreen, mAmber, Color(0xFF0D0D14), Color(0xFF1A1A2E)),
        typography = marathonTypography(), shapes = TerminalShapes,
        lightScheme = lightColorScheme(primary=Color(0xFF006C2E),onPrimary=Color.White,primaryContainer=Color(0xFF7FFF9E),onPrimaryContainer=Color(0xFF00210A),secondary=Color(0xFF7A5900),onSecondary=Color.White,secondaryContainer=Color(0xFFFFDEA6),onSecondaryContainer=Color(0xFF271900),tertiary=Color(0xFF00695C),onTertiary=Color.White,background=Color(0xFFF4FFF0),onBackground=Color(0xFF0D0D14),surface=Color(0xFFF4FFF0),onSurface=Color(0xFF0D0D14),surfaceVariant=Color(0xFFDAE8D5),onSurfaceVariant=Color(0xFF3F4A3B),outline=Color(0xFF6F7B6A),outlineVariant=Color(0xFFBEC9B9)),
        darkScheme = darkColorScheme(primary=mGreen,onPrimary=Color(0xFF003916),primaryContainer=Color(0xFF005224),onPrimaryContainer=mGreen,secondary=mAmber,onSecondary=Color(0xFF221800),secondaryContainer=Color(0xFF332400),onSecondaryContainer=mAmber,tertiary=Color(0xFF64FFDA),onTertiary=Color(0xFF00382B),background=Color(0xFF0D0D14),onBackground=Color(0xFFD0E8D0),surface=Color(0xFF141420),onSurface=Color(0xFFD0E8D0),surfaceVariant=Color(0xFF1E1E2E),onSurfaceVariant=Color(0xFF88AA88),outline=Color(0xFF3A5A3A),outlineVariant=Color(0xFF2A3A2A)),
        decorations = ThemeDecorations(scanlineEffect=true,scanlineColor=mGreen.copy(alpha=0.04f),glowAccent=true,glowColor=mGreen.copy(alpha=0.06f),backgroundTexture=BackgroundTexture.SCANLINES,cardStyle=CardStyle.SHARP,dividerStyle=DividerStyle.TERMINAL_DOTS,
            themeLogo = { MarathonLogo() }
        )
    )
}
