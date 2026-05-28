package com.vkusnyvybor.data.remote

import retrofit2.http.GET

/**
 * Retrofit-интерфейс для общения с микросервисом авторизации (VV_Authorization_Service).
 *
 * Сам сервис уже подключён к API Gateway и реализует поток Telegram OpenID Connect:
 *  1) `/auth/login`     — выдаёт ссылку на oauth.telegram.org и одноразовый state
 *  2) `/auth/callback`  — обрабатывается сервером; возвращает user_hash после успеха
 *
 * Клиент мобильного приложения отвечает только за инициирование входа: получает
 * `auth_url`, открывает его в браузере и далее доверяет работе сервиса/гейтвея.
 */
interface AuthApi {

    // Полный путь через шлюз: BASE_URL + "auth/auth/login"
    //   = http://62.113.41.127:8001/auth/auth/login
    // (префикс шлюза `auth` + внутренний маршрут сервиса `/auth/login`).
    // Раньше тут было "auth/login" — отсюда 404/502 на телефоне.
    @GET("auth/auth/login")
    suspend fun login(): LoginResponse

    // Health сервиса проксируется шлюзом под тем же префиксом `auth`.
    @GET("auth/health")
    suspend fun health(): HealthResponse
}

data class LoginResponse(
    val auth_url: String,
    val state: String
)

data class HealthResponse(
    val status: String
)
