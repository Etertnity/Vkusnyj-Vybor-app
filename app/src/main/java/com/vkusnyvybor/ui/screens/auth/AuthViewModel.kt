package com.vkusnyvybor.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vkusnyvybor.data.repository.AuthMode
import com.vkusnyvybor.data.repository.AuthRepository
import com.vkusnyvybor.data.repository.AuthSession
import com.vkusnyvybor.data.repository.AuthSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

/**
 * Состояние экрана авторизации/регистрации.
 *
 * @property mode        режим — Login или Register (визуальное переключение)
 * @property name        отображаемое имя пользователя (для гостевого режима)
 * @property pendingState state, выданный сервисом при инициировании Telegram-входа
 * @property authUrl     ссылка, которую нужно открыть в браузере для входа
 * @property awaitingTelegram признак того, что мы ждём возвращения из браузера
 * @property error       сообщение об ошибке (если есть)
 * @property loading     индикатор сетевой активности
 * @property authorized  итоговый признак успешной авторизации
 */
data class AuthUiState(
    val mode: AuthFormMode = AuthFormMode.LOGIN,
    val name: String = "",
    val pendingState: String? = null,
    val authUrl: String? = null,
    val awaitingTelegram: Boolean = false,
    val error: String? = null,
    val loading: Boolean = false,
    val authorized: Boolean = false
)

enum class AuthFormMode { LOGIN, REGISTER }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val sessionStore: AuthSessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Реактивно следим за сессией. Это покрывает три случая:
        //  1) сессия уже была сохранена ранее — сразу пускаем дальше;
        //  2) пользователь вернулся из браузера по deep link
        //     (vkusnyvybor://auth/callback) — MainActivity положил реальный
        //     user_hash + username в стор, и экран сам уходит на Home;
        //  3) гостевой вход.
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        authorized = true,
                        awaitingTelegram = false,
                        loading = false
                    )
                }
            }
        }
    }

    fun setMode(mode: AuthFormMode) {
        _uiState.value = _uiState.value.copy(mode = mode, error = null)
    }

    fun setName(value: String) {
        _uiState.value = _uiState.value.copy(name = value, error = null)
    }

    /**
     * Запускает Telegram OpenID Connect поток. Запрашивает у микросервиса
     * `/auth/login` и передаёт URL во вью для открытия в браузере.
     */
    fun startTelegramLogin() {
        val current = _uiState.value
        if (current.loading) return

        _uiState.value = current.copy(loading = true, error = null)
        viewModelScope.launch {
            repository.startTelegramLogin().fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        authUrl = resp.auth_url,
                        pendingState = resp.state,
                        awaitingTelegram = true
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Не удалось связаться с сервисом авторизации: ${e.message}"
                    )
                }
            )
        }
    }

    /** Сбрасывает URL после того, как браузер был открыт. */
    fun consumeAuthUrl() {
        _uiState.value = _uiState.value.copy(authUrl = null)
    }

    /**
     * Вызывается из встроенного WebView, когда из ответа callback получены
     * НАСТОЯЩИЕ данные пользователя. Сохраняем их в сессию — профиль покажет
     * реальное имя, а user_hash станет идентификатором для шлюза.
     */
    fun completeTelegramLogin(userHash: String, username: String) {
        if (userHash.isBlank()) return
        sessionStore.save(
            AuthSession(
                userHash = userHash,
                username = username.ifBlank { "Telegram-пользователь" },
                mode = AuthMode.TELEGRAM
            )
        )
        _uiState.value = _uiState.value.copy(
            authUrl = null,
            awaitingTelegram = false,
            authorized = true
        )
    }

    /** Пользователь закрыл окно входа, не завершив авторизацию. */
    fun cancelTelegramLogin() {
        _uiState.value = _uiState.value.copy(
            authUrl = null,
            awaitingTelegram = false,
            loading = false
        )
    }

    /**
     * Пользователь вернулся из браузера и подтверждает, что вошёл.
     * В production-окружении сюда придёт callback из API Gateway с готовым
     * user_hash; в локальной интеграции просто создаём отметку о сессии,
     * сохраняя state, выданный сервисом.
     */
    fun confirmTelegramReturn() {
        val state = _uiState.value.pendingState ?: return
        val hash = sha256(state)
        val name = _uiState.value.name.ifBlank { "Telegram-пользователь" }
        sessionStore.save(AuthSession(userHash = hash, username = name, mode = AuthMode.TELEGRAM))
        _uiState.value = _uiState.value.copy(authorized = true, awaitingTelegram = false)
    }

    /** Гостевой режим — для пробы и оффлайн-сценария. */
    fun continueAsGuest() {
        val name = _uiState.value.name.ifBlank { "Гость" }
        val hash = sha256("guest-${UUID.randomUUID()}")
        sessionStore.save(AuthSession(userHash = hash, username = name, mode = AuthMode.GUEST))
        _uiState.value = _uiState.value.copy(authorized = true)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
