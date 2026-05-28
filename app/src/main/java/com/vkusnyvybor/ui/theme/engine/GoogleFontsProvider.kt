package com.vkusnyvybor.ui.theme.engine

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.vkusnyvybor.R

object GoogleFontsProvider {

    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    private val cache = mutableMapOf<String, FontFamily>()

    fun familyOf(fontName: String): FontFamily {
        cache[fontName]?.let { return it }
        val google = GoogleFont(fontName)
        val family = FontFamily(
            Font(googleFont = google, fontProvider = provider, weight = FontWeight.Normal),
            Font(googleFont = google, fontProvider = provider, weight = FontWeight.Medium),
            Font(googleFont = google, fontProvider = provider, weight = FontWeight.SemiBold),
            Font(googleFont = google, fontProvider = provider, weight = FontWeight.Bold)
        )
        cache[fontName] = family
        return family
    }

    fun typographyFor(fontName: String): Typography {
        val family = familyOf(fontName)
        val base = Typography()
        return Typography(
            displayLarge = base.displayLarge.copy(fontFamily = family),
            displayMedium = base.displayMedium.copy(fontFamily = family),
            displaySmall = base.displaySmall.copy(fontFamily = family),
            headlineLarge = base.headlineLarge.copy(fontFamily = family),
            headlineMedium = base.headlineMedium.copy(fontFamily = family),
            headlineSmall = base.headlineSmall.copy(fontFamily = family),
            titleLarge = base.titleLarge.copy(fontFamily = family),
            titleMedium = base.titleMedium.copy(fontFamily = family),
            titleSmall = base.titleSmall.copy(fontFamily = family),
            bodyLarge = base.bodyLarge.copy(fontFamily = family),
            bodyMedium = base.bodyMedium.copy(fontFamily = family),
            bodySmall = base.bodySmall.copy(fontFamily = family),
            labelLarge = base.labelLarge.copy(fontFamily = family),
            labelMedium = base.labelMedium.copy(fontFamily = family),
            labelSmall = base.labelSmall.copy(fontFamily = family),
        )
    }
}
