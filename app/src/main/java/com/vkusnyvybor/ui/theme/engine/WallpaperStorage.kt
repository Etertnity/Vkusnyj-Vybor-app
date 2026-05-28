package com.vkusnyvybor.ui.theme.engine

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Копирует выбранное пользователем изображение во внутреннее хранилище приложения,
 * чтобы путь оставался валидным между запусками и после удаления из галереи.
 */
object WallpaperStorage {

    private const val DIR_NAME = "wallpapers"

    private fun dir(context: Context): File {
        val d = File(context.filesDir, DIR_NAME)
        if (!d.exists()) d.mkdirs()
        return d
    }

    fun importImage(context: Context, source: Uri, themeId: String): String? {
        return runCatching {
            val ext = guessExtension(context, source) ?: "img"
            val target = File(dir(context), "wp_${themeId}_${System.currentTimeMillis()}.$ext")
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching null
            target.absolutePath
        }.getOrNull()
    }

    fun delete(path: String?) {
        if (path.isNullOrEmpty()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    fun deleteAllForTheme(context: Context, themeId: String) {
        runCatching {
            dir(context).listFiles { f -> f.name.startsWith("wp_${themeId}_") }?.forEach { it.delete() }
        }
    }

    private fun guessExtension(context: Context, uri: Uri): String? {
        val mime = context.contentResolver.getType(uri) ?: return null
        return when {
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("jpeg", ignoreCase = true) || mime.contains("jpg", ignoreCase = true) -> "jpg"
            mime.contains("webp", ignoreCase = true) -> "webp"
            else -> "img"
        }
    }
}
