package com.vkusnyvybor.ui.theme.engine

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeEngine {

    private const val PREFS_NAME = "vkusny_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"
    private const val DEFAULT_THEME_ID = "material_you"

    private lateinit var prefs: SharedPreferences

    private val _currentThemeId = MutableStateFlow(DEFAULT_THEME_ID)
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    private val _currentTheme = MutableStateFlow(BuiltInThemes.materialYou)
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    private val _themes = mutableMapOf<String, ThemeConfig>()
    val availableThemes: List<ThemeConfig> get() = _themes.values.toList()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        registerTheme(BuiltInThemes.materialYou)
        registerTheme(BuiltInThemes.umbrellaCorp)
        registerTheme(BuiltInThemes.cyberpunk)
        registerTheme(BuiltInThemes.marathon)

        val savedId = prefs.getString(KEY_THEME_ID, DEFAULT_THEME_ID) ?: DEFAULT_THEME_ID
        applyTheme(savedId)
    }

    fun registerTheme(theme: ThemeConfig) {
        _themes[theme.id] = theme
    }

    fun setTheme(themeId: String) {
        applyTheme(themeId)
        prefs.edit().putString(KEY_THEME_ID, themeId).apply()
    }

    fun getTheme(themeId: String): ThemeConfig? = _themes[themeId]

    private fun applyTheme(themeId: String) {
        val theme = _themes[themeId] ?: _themes[DEFAULT_THEME_ID] ?: return
        _currentThemeId.value = theme.id
        _currentTheme.value = theme
    }
}
