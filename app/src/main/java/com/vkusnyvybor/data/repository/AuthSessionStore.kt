package com.vkusnyvybor.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальное хранилище сессии пользователя.
 *
 * После прохождения авторизации через Telegram (микросервис из архива)
 * сюда складывается user_hash + отображаемое имя. Используется навигацией,
 * чтобы решить — показать ли экран авторизации или сразу пустить в приложение.
 */
@Singleton
class AuthSessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(load())
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    fun isAuthenticated(): Boolean = _session.value != null

    fun save(session: AuthSession) {
        prefs.edit()
            .putString(KEY_USER_HASH, session.userHash)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_MODE, session.mode.name)
            .apply()
        _session.value = session
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private fun load(): AuthSession? {
        val hash = prefs.getString(KEY_USER_HASH, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: "Пользователь"
        val mode = prefs.getString(KEY_MODE, AuthMode.TELEGRAM.name)
            ?.let { runCatching { AuthMode.valueOf(it) }.getOrNull() }
            ?: AuthMode.TELEGRAM
        return AuthSession(userHash = hash, username = username, mode = mode)
    }

    companion object {
        private const val PREFS_NAME = "vkusny_auth_prefs"
        private const val KEY_USER_HASH = "user_hash"
        private const val KEY_USERNAME = "username"
        private const val KEY_MODE = "mode"
    }
}

enum class AuthMode { TELEGRAM, GUEST }

data class AuthSession(
    val userHash: String,
    val username: String,
    val mode: AuthMode = AuthMode.TELEGRAM
)
