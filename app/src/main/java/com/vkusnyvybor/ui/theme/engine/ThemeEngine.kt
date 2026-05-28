package com.vkusnyvybor.ui.theme.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeEngine {

    private const val PREFS_NAME = "vkusny_theme_prefs"
    private const val KEY_THEME_ID = "selected_theme_id"
    private const val KEY_CUSTOM_SPECS = "custom_theme_specs"
    private const val DEFAULT_THEME_ID = "material_you"
    private val BUILT_IN_IDS = setOf("material_you", "umbrella", "cyberpunk", "marathon")

    private lateinit var prefs: SharedPreferences

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    private val _currentThemeId = MutableStateFlow(DEFAULT_THEME_ID)
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    private val _currentTheme = MutableStateFlow(BuiltInThemes.materialYou)
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    private val _themes = mutableMapOf<String, ThemeConfig>()
    private val _customSpecs = mutableMapOf<String, CustomThemeSpec>()
    val availableThemes: List<ThemeConfig> get() = _themes.values.toList()

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        registerTheme(BuiltInThemes.materialYou)
        registerTheme(BuiltInThemes.umbrellaCorp)
        registerTheme(BuiltInThemes.cyberpunk)
        registerTheme(BuiltInThemes.marathon)
        loadCustomThemes()
        val savedId = prefs.getString(KEY_THEME_ID, DEFAULT_THEME_ID) ?: DEFAULT_THEME_ID
        applyTheme(savedId)
    }

    fun registerTheme(theme: ThemeConfig) { _themes[theme.id] = theme }

    /** Сохраняет пользовательскую тему: регистрирует ThemeConfig и персистит spec. */
    fun saveCustomTheme(spec: CustomThemeSpec) {
        _customSpecs[spec.id] = spec
        _themes[spec.id] = buildThemeFromSpec(spec)
        persistCustomThemes()
        if (_currentThemeId.value == spec.id) applyTheme(spec.id)
    }

    fun getCustomSpec(themeId: String): CustomThemeSpec? = _customSpecs[themeId]

    fun setTheme(themeId: String) {
        applyTheme(themeId)
        prefs.edit().putString(KEY_THEME_ID, themeId).apply()
    }

    fun getTheme(themeId: String): ThemeConfig? = _themes[themeId]

    fun isCustomTheme(themeId: String): Boolean = themeId !in BUILT_IN_IDS

    fun removeTheme(themeId: String) {
        if (themeId in BUILT_IN_IDS) return
        _customSpecs.remove(themeId)
        _themes.remove(themeId)
        if (::appContext.isInitialized) WallpaperStorage.deleteAllForTheme(appContext, themeId)
        persistCustomThemes()
        if (_currentThemeId.value == themeId) setTheme(DEFAULT_THEME_ID)
    }

    private fun applyTheme(themeId: String) {
        val theme = _themes[themeId] ?: _themes[DEFAULT_THEME_ID] ?: return
        _currentThemeId.value = theme.id
        _currentTheme.value = theme
    }

    private fun loadCustomThemes() {
        val raw = prefs.getString(KEY_CUSTOM_SPECS, null) ?: return
        deserializeSpecs(raw).forEach { spec ->
            _customSpecs[spec.id] = spec
            _themes[spec.id] = buildThemeFromSpec(spec)
        }
    }

    private fun persistCustomThemes() {
        prefs.edit().putString(KEY_CUSTOM_SPECS, serializeSpecs(_customSpecs.values)).apply()
    }
}
