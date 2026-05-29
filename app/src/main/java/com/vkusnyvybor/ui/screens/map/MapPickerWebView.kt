package com.vkusnyvybor.ui.screens.map

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.vkusnyvybor.data.remote.MapRemoteConfig
import com.vkusnyvybor.data.repository.SelectedLocation
import org.json.JSONObject
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "MapPicker"
private const val LOCAL_PORT = 18976

/**
 * Встроенная карта выбора предприятия (микросервис VV_Map_Service).
 *
 * Яндекс.Карты JS API 2.1 не рендерит тайлы при data: или file:// origin.
 * Решение: поднимаем крошечный HTTP-сервер на localhost:18976 прямо внутри
 * приложения — он отдаёт один HTML-файл. WebView грузит страницу по
 * http://localhost:18976/, получает настоящий HTTP-origin, и Яндекс
 * нормально отдаёт тайлы.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapPickerWebView(
    onResult: (SelectedLocation) -> Unit,
    modifier: Modifier = Modifier,
    authToken: String? = null
) {
    val scheme = MaterialTheme.colorScheme

    val colorReplacements = remember(scheme) {
        mapOf(
            "__PRIMARY__"    to scheme.primary.toCss(),
            "__ON_PRIMARY__" to scheme.onPrimary.toCss(),
            "__BG__"         to scheme.background.toCss(),
            "__SURFACE__"    to scheme.surface.toCss(),
            "__ON_SURFACE__" to scheme.onSurface.toCss(),
            "__OUTLINE__"    to scheme.outline.toCss()
        )
    }

    val resultHolder = rememberUpdatedState(onResult)
    val delivered = remember { booleanArrayOf(false) }

    // Собираем итоговый HTML один раз — он будет храниться в памяти сервера.
    val htmlRef = remember(colorReplacements, authToken) { arrayOfNulls<String>(1) }

    // Запускаем мини-HTTP-сервер и останавливаем при уходе с экрана.
    val serverRef = remember { arrayOfNulls<MapHttpServer>(1) }

    DisposableEffect(Unit) {
        onDispose {
            serverRef[0]?.stop()
            serverRef[0] = null
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            // Готовим HTML.
            val template = ctx.assets.open("map/map.html")
                .bufferedReader().use { it.readText() }

            var html = template
                .replace("__YANDEX_API_KEY__", MapRemoteConfig.YANDEX_API_KEY)
                .replace("__MAP_BASE_URL__",   MapRemoteConfig.BASE_URL)
                .replace("__AUTH_TOKEN__",     authToken.orEmpty())
                .replace("__AUTH_HEADER__",    MapRemoteConfig.AUTH_HEADER)
                .replace("__AUTH_SCHEME__",    MapRemoteConfig.AUTH_SCHEME)
            colorReplacements.forEach { (k, v) -> html = html.replace(k, v) }
            htmlRef[0] = html

            // Стартуем локальный HTTP-сервер.
            val server = MapHttpServer(LOCAL_PORT) { htmlRef[0] ?: "" }
            server.start()
            serverRef[0] = server

            WebView.setWebContentsDebuggingEnabled(true)

            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.setGeolocationEnabled(true)
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                    ) {
                        Log.e(TAG, "resource error ${error?.errorCode} ${error?.description} url=${request?.url}")
                    }
                    override fun onReceivedHttpError(
                        view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
                    ) {
                        Log.e(TAG, "http error ${errorResponse?.statusCode} url=${request?.url}")
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                        Log.d(TAG, "console: ${msg?.message()} @${msg?.sourceId()}:${msg?.lineNumber()}")
                        return true
                    }
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?, callback: GeolocationPermissions.Callback?
                    ) {
                        callback?.invoke(origin, true, false)
                    }
                }

                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onLocationSelected(json: String) {
                            val loc = parseLocation(json) ?: return
                            if (delivered[0]) return
                            delivered[0] = true
                            post { resultHolder.value(loc) }
                        }
                    },
                    "AndroidMapBridge"
                )

                loadUrl("http://localhost:$LOCAL_PORT/")
            }
        }
    )
}

// ── Минимальный однопоточный HTTP-сервер ────────────────────────────────────

private class MapHttpServer(
    private val port: Int,
    private val htmlProvider: () -> String
) {
    @Volatile private var running = false
    private var serverSocket: ServerSocket? = null
    private var thread: Thread? = null

    fun start() {
        running = true
        serverSocket = ServerSocket(port)
        thread = Thread {
            while (running) {
                try {
                    val client: Socket = serverSocket?.accept() ?: break
                    Thread { handle(client) }.start()
                } catch (_: Exception) { /* сервер остановлен */ }
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun stop() {
        running = false
        try { serverSocket?.close() } catch (_: Exception) {}
    }

    private fun handle(socket: Socket) {
        try {
            socket.use {
                // Читаем запрос (нам достаточно просто принять его).
                val reader = it.getInputStream().bufferedReader()
                val requestLine = reader.readLine() ?: return
                Log.d(TAG, "LocalHTTP: $requestLine")

                val body = htmlProvider().toByteArray(Charsets.UTF_8)
                val response = buildString {
                    append("HTTP/1.1 200 OK\r\n")
                    append("Content-Type: text/html; charset=UTF-8\r\n")
                    append("Content-Length: ${body.size}\r\n")
                    append("Connection: close\r\n")
                    append("\r\n")
                }
                val out = it.getOutputStream()
                out.write(response.toByteArray(Charsets.UTF_8))
                out.write(body)
                out.flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "LocalHTTP error: ${e.message}")
        }
    }
}

// ── Вспомогательные функции ─────────────────────────────────────────────────

private fun Color.toCss(): String {
    val argb = this.toArgb()
    val r = (argb shr 16) and 0xFF
    val g = (argb shr 8) and 0xFF
    val b = argb and 0xFF
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun parseLocation(json: String): SelectedLocation? {
    val o = runCatching { JSONObject(json) }.getOrNull() ?: return null
    val clusterId = o.optInt("clusterId", -1).takeIf { it >= 0 } ?: return null
    val franchiseId = if (o.isNull("franchiseId")) null
    else o.optInt("franchiseId").takeIf { it >= 0 }
    return SelectedLocation(
        clusterId    = clusterId,
        clusterName  = o.optString("clusterName").takeIf  { it.isNotBlank() && it != "null" },
        address      = o.optString("address", ""),
        franchiseId  = franchiseId,
        franchiseName= o.optString("franchiseName").takeIf { it.isNotBlank() && it != "null" },
        latitude     = o.optDouble("latitude",  0.0),
        longitude    = o.optDouble("longitude", 0.0)
    )
}