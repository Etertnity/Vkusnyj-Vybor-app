package com.vkusnyvybor.data.remote

/**
 * Конфигурация подключения к микросервису авторизации через API Gateway.
 *
 * Важно про пути: шлюз монтирует auth-service под префиксом `auth`, а сам
 * сервис внутри монтирует свой роутер тоже под `/auth` (см. main.py:
 * `app.include_router(auth.router, prefix="/auth")`). Поэтому полный путь к
 * логину через шлюз — `auth/auth/login` (а не `auth/login`). Именно из-за
 * обращения к `auth/login` приложение получало 404/502 (см. AuthApi).
 *
 * Демо-клиент друга (index.html) бьёт ровно в `/auth/auth/login`.
 */
object AuthRemoteConfig {
    /** Production-адрес API Gateway, через который проксируется auth-service. */
    const val PRODUCTION_BASE_URL = "http://62.113.41.127:8001/"

    /** Предыдущий хост (на случай отката). */
    const val LEGACY_BASE_URL = "http://186.246.10.192:8001/"

    /** Локальный эмулятор Android — алиас на localhost хоста. */
    const val EMULATOR_BASE_URL = "http://10.0.2.2:8000/"

    /** Текущий адрес. Переключайте при необходимости. */
    const val BASE_URL = PRODUCTION_BASE_URL
}
