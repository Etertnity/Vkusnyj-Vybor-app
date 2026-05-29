package com.vkusnyvybor.ui.screens.auth

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vkusnyvybor.ui.localization.LocalStrings
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Встроенное окно входа через Telegram.
 *
 * Зачем нужно: после Telegram OAuth сервис авторизации отвечает на свой
 * `/auth/callback` телом вида `{"success":true,"user_hash":"...","username":"..."}`.
 * Если открывать ссылку во ВНЕШНЕМ браузере, этот ответ остаётся в браузере и
 * до приложения не доходит (Android изолирует приложения). Поэтому открываем
 * ссылку здесь — в WebView, которым владеет само приложение, — и читаем тело
 * страницы напрямую.
 *
 * Дополнительно перехватываем переход на схему `vkusnyvybor://auth/callback`
 * (на случай, если сервер обновят и он станет редиректить через deep link).
 *
 * Telegram иногда блокирует вход из встроенных WebView — если страница не даёт
 * залогиниться, остаётся запасной путь через серверный редирект (deep link).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramAuthWebView(
    url: String,
    onResult: (userHash: String, username: String) -> Unit,
    onClose: () -> Unit
) {
    // Результат отдаём строго один раз (onPageFinished может сработать не раз).
    var delivered by remember { mutableStateOf(false) }

    fun deliver(hash: String, name: String) {
        if (delivered || hash.isBlank()) return
        delivered = true
        onResult(hash, name.ifBlank { "Telegram-пользователь" })
    }

    BackHandler { onClose() }

    val s = LocalStrings.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.telegramLoginTitle) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = s.close)
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val u = request?.url ?: return false
                            return when (u.scheme?.lowercase()) {
                                // Обычные веб-страницы — грузим в самом WebView.
                                "http", "https" -> false

                                // Deep link приложения (если сервер настроен на редирект).
                                "vkusnyvybor" -> {
                                    deliver(
                                        u.getQueryParameter("user_hash") ?: "",
                                        u.getQueryParameter("username") ?: ""
                                    )
                                    true
                                }

                                // tg://, intent://, mailto: и т.п. — WebView их не умеет.
                                // Отдаём системе: tg://resolve откроет нативный Telegram
                                // для подтверждения входа. Без этого — ERR_UNKNOWN_URL_SCHEME.
                                else -> {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, u)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        view?.context?.startActivity(intent)
                                    }
                                    true
                                }
                            }
                        }

                        // Чтение JSON-тела страницы callback.
                        override fun onPageFinished(view: WebView?, pageUrl: String?) {
                            super.onPageFinished(view, pageUrl)
                            view?.evaluateJavascript(
                                "(function(){return document.body ? document.body.innerText : '';})();"
                            ) { raw ->
                                parseCallback(raw)?.let { (hash, name) -> deliver(hash, name) }
                            }
                        }
                    }

                    loadUrl(url)
                }
            }
        )
    }
}

/**
 * Достаёт (user_hash, username) из тела страницы callback, если это успешный
 * JSON. `evaluateJavascript` возвращает innerText, обёрнутый в JSON-строку,
 * поэтому сначала снимаем внешнюю обёртку, затем парсим сам объект.
 */
private fun parseCallback(raw: String?): Pair<String, String>? {
    if (raw.isNullOrBlank() || raw == "null") return null
    val text = runCatching { JSONTokener(raw).nextValue() as? String }.getOrNull() ?: return null
    val obj = runCatching { JSONObject(text) }.getOrNull() ?: return null
    if (!obj.optBoolean("success")) return null
    val hash = obj.optString("user_hash").takeIf { it.isNotBlank() } ?: return null
    val name = obj.optString("username").takeIf { it.isNotBlank() } ?: "Telegram-пользователь"
    return hash to name
}
