package com.vkusnyvybor.data.repository

import com.vkusnyvybor.data.remote.AuthApi
import com.vkusnyvybor.data.remote.LoginResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Тонкая обёртка над AuthApi.
 *
 * Логика авторизации полностью остаётся внутри микросервиса
 * (`VV_Authorization_Service`) — он обменивает code на ID-Token, проверяет
 * подпись и синхронизирует хэш с API Gateway. Клиент только инициирует поток.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi
) {

    /** Инициировать новый вход. Возвращает URL для открытия в браузере. */
    suspend fun startTelegramLogin(): Result<LoginResponse> = runCatching { api.login() }

    /** Проверить доступность сервиса (для отладки/индикатора в UI). */
    suspend fun ping(): Boolean = runCatching { api.health().status == "healthy" }.getOrDefault(false)
}
