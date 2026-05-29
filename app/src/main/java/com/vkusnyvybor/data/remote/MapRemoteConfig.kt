package com.vkusnyvybor.data.remote

/**
 * Конфигурация встроенной карты выбора предприятия.
 */
object MapRemoteConfig {

    const val BASE_URL: String = "http://62.113.41.127:8001/map"

    /**
     * Ваш новый ключ Яндекс.Карт (JS API 2.1).
     */
    const val YANDEX_API_KEY: String = "27d62433-719e-4a3a-a71c-0e4ea67c9d8e"

    /** 
     * Используем домен в качестве Origin. Это поможет Яндексу 
     * корректно определять Referer и считать статистику.
     */
    const val BASE_ORIGIN: String = "http://62.113.41.127:8001"

    const val AUTH_HEADER: String = "Authorization"
    const val AUTH_SCHEME: String = "Bearer"
}
