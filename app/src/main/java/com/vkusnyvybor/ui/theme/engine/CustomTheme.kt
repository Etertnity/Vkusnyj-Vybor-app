package com.vkusnyvybor.ui.theme.engine

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

/**
 * Сериализуемое описание пользовательской темы.
 * Всё, что нужно, чтобы пересобрать ThemeConfig после перезапуска приложения.
 */
data class CustomThemeSpec(
    val id: String,
    val name: String,
    val primary: Long,           // ARGB
    val background: Long,
    val surface: Long,
    val cardColor: Long,
    val textColor: Long,
    val priceColor: Long,
    val cardStyle: CardStyle,
    val scanlines: Boolean,
    val glow: Boolean,
    val customFontName: String? = null,
    val customCornerRadius: Float? = null,
    val wallpaperPath: String? = null,
    val wallpaperOpacity: Float = 0.3f,
    val wallpaperScale: Float = 1f,
    val wallpaperOffsetX: Float = 0f,
    val wallpaperOffsetY: Float = 0f
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("name", name)
        put("primary", primary); put("background", background); put("surface", surface)
        put("cardColor", cardColor); put("textColor", textColor); put("priceColor", priceColor)
        put("cardStyle", cardStyle.name)
        put("scanlines", scanlines); put("glow", glow)
        customFontName?.let { put("customFontName", it) }
        customCornerRadius?.let { put("customCornerRadius", it.toDouble()) }
        wallpaperPath?.let { put("wallpaperPath", it) }
        put("wallpaperOpacity", wallpaperOpacity.toDouble())
        put("wallpaperScale", wallpaperScale.toDouble())
        put("wallpaperOffsetX", wallpaperOffsetX.toDouble())
        put("wallpaperOffsetY", wallpaperOffsetY.toDouble())
    }

    companion object {
        fun fromJson(o: JSONObject) = CustomThemeSpec(
            id = o.getString("id"),
            name = o.getString("name"),
            primary = o.getLong("primary"),
            background = o.getLong("background"),
            surface = o.getLong("surface"),
            cardColor = o.getLong("cardColor"),
            textColor = o.getLong("textColor"),
            priceColor = o.getLong("priceColor"),
            cardStyle = runCatching { CardStyle.valueOf(o.getString("cardStyle")) }.getOrDefault(CardStyle.ROUNDED),
            scanlines = o.optBoolean("scanlines", false),
            glow = o.optBoolean("glow", false),
            customFontName = o.optString("customFontName").takeIf { it.isNotEmpty() },
            customCornerRadius = if (o.has("customCornerRadius")) o.getDouble("customCornerRadius").toFloat() else null,
            wallpaperPath = o.optString("wallpaperPath").takeIf { it.isNotEmpty() },
            wallpaperOpacity = o.optDouble("wallpaperOpacity", 0.3).toFloat(),
            wallpaperScale = o.optDouble("wallpaperScale", 1.0).toFloat(),
            wallpaperOffsetX = o.optDouble("wallpaperOffsetX", 0.0).toFloat(),
            wallpaperOffsetY = o.optDouble("wallpaperOffsetY", 0.0).toFloat()
        )
    }
}

/**
 * Сборка ThemeConfig из пользовательского описания.
 */
fun buildThemeFromSpec(spec: CustomThemeSpec): ThemeConfig {
    val primary = Color(spec.primary.toInt())
    val bg = Color(spec.background.toInt())
    val surf = Color(spec.surface.toInt())
    val card = Color(spec.cardColor.toInt())
    val text = Color(spec.textColor.toInt())
    val price = Color(spec.priceColor.toInt())

    val shapes = spec.customCornerRadius?.let { buildCustomShapes(it, spec.cardStyle) }
        ?: when (spec.cardStyle) {
            CardStyle.CUT_CORNER -> CyberShapes
            CardStyle.SHARP -> TerminalShapes
            else -> null
        }

    val typography = spec.customFontName?.let { GoogleFontsProvider.typographyFor(it) }

    return ThemeConfig(
        id = spec.id,
        name = spec.name,
        description = "Пользовательская тема",
        previewColors = listOf(primary, price, bg, card),
        lightScheme = lightColorScheme(
            primary = primary, onPrimary = Color.White,
            primaryContainer = primary.copy(0.2f), onPrimaryContainer = primary,
            secondary = price, onSecondary = Color.White,
            background = Color.White, onBackground = Color(0xFF1A1A1A),
            surface = Color(0xFFF5F5F5), onSurface = Color(0xFF1A1A1A),
            surfaceVariant = Color(0xFFE8E8E8), onSurfaceVariant = Color(0xFF444444),
            outline = Color(0xFF888888), outlineVariant = Color(0xFFCCCCCC)
        ),
        darkScheme = darkColorScheme(
            primary = primary, onPrimary = Color.White,
            primaryContainer = primary.copy(0.3f), onPrimaryContainer = primary,
            secondary = price, onSecondary = Color.Black,
            background = bg, onBackground = text,
            surface = surf, onSurface = text,
            surfaceVariant = card, onSurfaceVariant = text.copy(0.7f),
            outline = text.copy(0.3f), outlineVariant = text.copy(0.15f)
        ),
        typography = typography,
        shapes = shapes,
        decorations = ThemeDecorations(
            scanlineEffect = spec.scanlines, scanlineColor = primary.copy(0.03f),
            glowAccent = spec.glow, glowColor = primary.copy(0.08f),
            cardStyle = spec.cardStyle,
            dividerStyle = when (spec.cardStyle) {
                CardStyle.CUT_CORNER -> DividerStyle.NEON_LINE
                CardStyle.SHARP -> DividerStyle.TERMINAL_DOTS
                else -> DividerStyle.SIMPLE
            },
            customFontName = spec.customFontName,
            customCornerRadius = spec.customCornerRadius,
            wallpaperPath = spec.wallpaperPath,
            wallpaperOpacity = spec.wallpaperOpacity,
            wallpaperScale = spec.wallpaperScale,
            wallpaperOffsetX = spec.wallpaperOffsetX,
            wallpaperOffsetY = spec.wallpaperOffsetY
        )
    )
}

internal fun serializeSpecs(specs: Collection<CustomThemeSpec>): String {
    val arr = JSONArray()
    specs.forEach { arr.put(it.toJson()) }
    return arr.toString()
}

internal fun deserializeSpecs(raw: String?): List<CustomThemeSpec> {
    if (raw.isNullOrEmpty()) return emptyList()
    return runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { CustomThemeSpec.fromJson(arr.getJSONObject(it)) }
    }.getOrDefault(emptyList())
}
