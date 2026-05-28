package com.vkusnyvybor.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vkusnyvybor.ui.theme.engine.*
import java.io.File

/**
 * Интерактивный конструктор тем.
 * @param editThemeId — если не null, загружаем существующую тему для редактирования.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeConstructorScreen(
    editThemeId: String? = null,
    onBackClick: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val existingSpec = editThemeId?.let { ThemeEngine.getCustomSpec(it) }

    val themeId = remember { existingSpec?.id ?: "custom_${System.currentTimeMillis()}" }

    var themeName by remember { mutableStateOf(existingSpec?.name ?: "Моя тема") }
    var primary by remember { mutableStateOf(existingSpec?.primary?.let { Color(it.toInt()) } ?: Color(0xFF6750A4)) }
    var background by remember { mutableStateOf(existingSpec?.background?.let { Color(it.toInt()) } ?: Color(0xFF0D0D14)) }
    var surface by remember { mutableStateOf(existingSpec?.surface?.let { Color(it.toInt()) } ?: Color(0xFF1E1E2E)) }
    var cardColor by remember { mutableStateOf(existingSpec?.cardColor?.let { Color(it.toInt()) } ?: Color(0xFF2A2A3A)) }
    var textColor by remember { mutableStateOf(existingSpec?.textColor?.let { Color(it.toInt()) } ?: Color(0xFFE0E0E8)) }
    var priceColor by remember { mutableStateOf(existingSpec?.priceColor?.let { Color(it.toInt()) } ?: Color(0xFF6750A4)) }
    var cardStyle by remember { mutableStateOf(existingSpec?.cardStyle ?: CardStyle.ROUNDED) }
    var scanlines by remember { mutableStateOf(existingSpec?.scanlines ?: false) }
    var glow by remember { mutableStateOf(existingSpec?.glow ?: false) }

    // Google Font: имя как на fonts.google.com
    var fontName by remember { mutableStateOf(existingSpec?.customFontName ?: "") }

    // Кастомный радиус
    var customRadius by remember { mutableStateOf(existingSpec?.customCornerRadius ?: 14f) }
    var useCustomRadius by remember { mutableStateOf(existingSpec?.customCornerRadius != null) }

    // Wallpaper
    var wallpaperPath by remember { mutableStateOf(existingSpec?.wallpaperPath) }
    var wallpaperOpacity by remember { mutableStateOf((existingSpec?.wallpaperOpacity ?: 0.3f) * 100f) }
    var wallpaperScale by remember { mutableStateOf((existingSpec?.wallpaperScale ?: 1f) * 100f) }
    var wallpaperOffsetX by remember { mutableStateOf((existingSpec?.wallpaperOffsetX ?: 0f) * 100f) }
    var wallpaperOffsetY by remember { mutableStateOf((existingSpec?.wallpaperOffsetY ?: 0f) * 100f) }

    var cardOpacity by remember { mutableStateOf(100f) }
    var btnOpacity by remember { mutableStateOf(100f) }

    var selectedElement by remember { mutableStateOf<String?>(null) }
    var colorPickerTarget by remember { mutableStateOf<String?>(null) }
    var showFontHelp by remember { mutableStateOf(false) }
    var showShapeHelp by remember { mutableStateOf(false) }

    val cornerRadius = if (useCustomRadius) customRadius.dp else when (cardStyle) {
        CardStyle.ROUNDED -> 14.dp; CardStyle.CUT_CORNER -> 4.dp; CardStyle.SHARP -> 2.dp
    }
    val btnRadius = if (useCustomRadius) (customRadius * 0.5f).dp else when (cardStyle) {
        CardStyle.ROUNDED -> 8.dp; CardStyle.CUT_CORNER -> 2.dp; CardStyle.SHARP -> 1.dp
    }

    // Для превью подменяем FontFamily: если задано имя Google Font — грузим, иначе системный
    val fontFamily: FontFamily = remember(fontName) {
        if (fontName.isBlank()) FontFamily.Default
        else runCatching { GoogleFontsProvider.familyOf(fontName.trim()) }.getOrDefault(FontFamily.Default)
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val path = WallpaperStorage.importImage(ctx, uri, themeId)
            if (path != null) {
                WallpaperStorage.delete(wallpaperPath)
                wallpaperPath = path
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editThemeId != null) "Редактировать тему" else "Конструктор тем") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    TextButton(onClick = {
                        val spec = CustomThemeSpec(
                            id = themeId,
                            name = themeName.ifBlank { "Моя тема" },
                            primary = primary.toArgb().toLong() and 0xFFFFFFFFL,
                            background = background.toArgb().toLong() and 0xFFFFFFFFL,
                            surface = surface.toArgb().toLong() and 0xFFFFFFFFL,
                            cardColor = cardColor.toArgb().toLong() and 0xFFFFFFFFL,
                            textColor = textColor.toArgb().toLong() and 0xFFFFFFFFL,
                            priceColor = priceColor.toArgb().toLong() and 0xFFFFFFFFL,
                            cardStyle = cardStyle,
                            scanlines = scanlines,
                            glow = glow,
                            customFontName = fontName.trim().takeIf { it.isNotBlank() },
                            customCornerRadius = if (useCustomRadius) customRadius else null,
                            wallpaperPath = wallpaperPath,
                            wallpaperOpacity = (wallpaperOpacity / 100f).coerceIn(0f, 1f),
                            wallpaperScale = (wallpaperScale / 100f).coerceIn(0.25f, 4f),
                            wallpaperOffsetX = (wallpaperOffsetX / 100f).coerceIn(-1f, 1f),
                            wallpaperOffsetY = (wallpaperOffsetY / 100f).coerceIn(-1f, 1f)
                        )
                        ThemeEngine.saveCustomTheme(spec)
                        ThemeEngine.setTheme(spec.id)
                        onBackClick()
                    }) { Text("Сохранить", fontWeight = FontWeight.Bold) }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = themeName, onValueChange = { themeName = it },
                label = { Text("Название темы") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium
            )

            // ══════════════════════════════════════════════
            //  ИНТЕРАКТИВНЫЙ КАРКАС (tap-to-edit)
            // ══════════════════════════════════════════════
            Box(
                Modifier.fillMaxWidth().padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(background)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            ) {
                // Фоновое превью обоев (если есть)
                val wpPreview = wallpaperPath
                if (!wpPreview.isNullOrEmpty() && File(wpPreview).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(File(wpPreview)).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .alpha((wallpaperOpacity / 100f).coerceIn(0f, 1f))
                    )
                }

                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Поиск
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(surface)
                            .border(if (selectedElement == "search") 2.dp else 0.dp, primary, RoundedCornerShape(cornerRadius))
                            .clickable { selectedElement = "search" }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Найти блюдо...", fontSize = 11.sp, color = textColor.copy(0.4f), fontFamily = fontFamily, modifier = Modifier.weight(1f))
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(btnRadius)).background(surface), contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 14.sp)
                        }
                    }
                    // Адрес
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(cardColor.copy(cardOpacity / 100f))
                            .border(if (selectedElement == "address") 2.dp else 0.dp, primary, RoundedCornerShape(cornerRadius))
                            .clickable { selectedElement = "address" }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📍", fontSize = 12.sp)
                        Column(Modifier.weight(1f)) {
                            Text("В предприятии", fontSize = 8.sp, color = textColor.copy(0.6f), fontFamily = fontFamily)
                            Text("Выберите предприятие", fontSize = 10.sp, color = textColor, fontFamily = fontFamily)
                        }
                        Box(Modifier.clip(RoundedCornerShape(btnRadius)).background(primary).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("Выбрать", fontSize = 9.sp, color = Color.White, fontFamily = fontFamily)
                        }
                    }
                    // Карусель
                    Box(
                        Modifier.fillMaxWidth().height(100.dp)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))))
                            .border(if (selectedElement == "carousel") 2.dp else 0.dp, primary, RoundedCornerShape(cornerRadius))
                            .then(if (glow) Modifier.border(1.dp, primary.copy(0.4f), RoundedCornerShape(cornerRadius)) else Modifier)
                            .clickable { selectedElement = "carousel" }
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text("Всё как мы любим", fontSize = 9.sp, color = Color.White.copy(0.7f), fontFamily = fontFamily)
                            Column {
                                Text("Вкусно и точка", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = fontFamily)
                                Text("★ 4.5  25-35 мин", fontSize = 8.sp, color = Color.White.copy(0.8f), fontFamily = fontFamily)
                            }
                        }
                        Text("🍔", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 40.dp, bottom = 12.dp))
                        Text("🍟", fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 8.dp))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Box(Modifier.size(16.dp, 4.dp).clip(CircleShape).background(primary))
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.size(4.dp).clip(CircleShape).background(textColor.copy(0.2f)))
                        Spacer(Modifier.width(4.dp))
                        Box(Modifier.size(4.dp).clip(CircleShape).background(textColor.copy(0.2f)))
                    }
                    Row(
                        Modifier.clickable { selectedElement = "chips" }
                            .border(if (selectedElement == "chips") 2.dp else 0.dp, primary, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("🍔 Бургеры", "🍗 Снэки").forEach { chip ->
                            Box(Modifier.clip(RoundedCornerShape(cornerRadius)).background(primary.copy(0.15f)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(chip, fontSize = 9.sp, color = primary, fontFamily = fontFamily)
                            }
                        }
                    }
                    Row(
                        Modifier.clickable { selectedElement = "category" }
                            .border(if (selectedElement == "category") 2.dp else 0.dp, primary, RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🍔", fontSize = 14.sp)
                        Text("Бургеры", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = priceColor, fontFamily = fontFamily)
                        Box(Modifier.weight(1f).height(1.dp).background(priceColor.copy(0.2f)))
                    }
                    listOf("Биг Спешал" to "345₽", "Чизбургер" to "189₽").forEach { (name, price) ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(cardColor.copy(cardOpacity / 100f))
                                .then(if (glow) Modifier.border(0.5.dp, primary.copy(0.3f), RoundedCornerShape(cornerRadius)) else Modifier)
                                .border(if (selectedElement == "card") 2.dp else 0.dp, primary, RoundedCornerShape(cornerRadius))
                                .clickable { selectedElement = "card" }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(40.dp).clip(RoundedCornerShape(btnRadius)).background(primary.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Text("🍔", fontSize = 18.sp)
                            }
                            Column(Modifier.weight(1f)) {
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor, fontFamily = fontFamily)
                                Text("Описание товара", fontSize = 9.sp, color = textColor.copy(0.5f), fontFamily = fontFamily)
                                Spacer(Modifier.height(4.dp))
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text(price, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = priceColor, fontFamily = fontFamily)
                                    Box(
                                        Modifier.size(24.dp).clip(RoundedCornerShape(btnRadius))
                                            .background(primary.copy(btnOpacity / 100f))
                                            .clickable { selectedElement = "button" },
                                        contentAlignment = Alignment.Center
                                    ) { Text("+", fontSize = 14.sp, color = Color.White) }
                                }
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                        Box(
                            Modifier.size(40.dp).clip(RoundedCornerShape(cornerRadius))
                                .background(primary.copy(0.25f))
                                .border(if (selectedElement == "fab") 2.dp else 0.dp, primary, RoundedCornerShape(cornerRadius))
                                .clickable { selectedElement = "fab" },
                            contentAlignment = Alignment.Center
                        ) { Text("🛒", fontSize = 18.sp) }
                    }
                }
            }

            // ══════════════════════════════════════════════
            //  ПАНЕЛЬ РЕДАКТИРОВАНИЯ (под каркасом)
            // ══════════════════════════════════════════════
            if (selectedElement != null) {
                ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(
                                when (selectedElement) {
                                    "search" -> "Поиск"; "address" -> "Адрес"; "carousel" -> "Карусель"
                                    "chips" -> "Фильтры"; "category" -> "Заголовок"; "card" -> "Карточка товара"
                                    "button" -> "Кнопка +"; "fab" -> "Корзина"; else -> ""
                                },
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { selectedElement = null }, Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Close, null, Modifier.size(18.dp))
                            }
                        }
                        Text("Цвета", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorDot("Primary", primary) { colorPickerTarget = "primary" }
                            ColorDot("Фон", background) { colorPickerTarget = "background" }
                            ColorDot("Surface", surface) { colorPickerTarget = "surface" }
                            ColorDot("Карточка", cardColor) { colorPickerTarget = "card" }
                            ColorDot("Текст", textColor) { colorPickerTarget = "text" }
                            ColorDot("Цена", priceColor) { colorPickerTarget = "price" }
                        }
                        if (selectedElement == "card" || selectedElement == "button") {
                            Text("Прозрачность", style = MaterialTheme.typography.labelLarge)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Slider(
                                    value = if (selectedElement == "card") cardOpacity else btnOpacity,
                                    onValueChange = { if (selectedElement == "card") cardOpacity = it else btnOpacity = it },
                                    valueRange = 0f..100f, modifier = Modifier.weight(1f)
                                )
                                Text("${(if (selectedElement == "card") cardOpacity else btnOpacity).toInt()}%", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Нажмите на элемент в каркасе для редактирования",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }
            }

            // ══════════════════════════════════════════════
            //  ГЛОБАЛЬНЫЕ НАСТРОЙКИ ТЕМЫ
            // ══════════════════════════════════════════════

            SectionHeader("Шрифт (Google Fonts)") { showFontHelp = !showFontHelp }
            if (showFontHelp) InfoBox(
                "• Введите точное название шрифта с fonts.google.com (с большой буквы, например: Roboto, Montserrat, Inter, Playfair Display).\n" +
                "• Шрифт загрузится через системный провайдер Google при первом использовании — нужен интернет.\n" +
                "• Оставьте поле пустым, чтобы использовать системный шрифт.\n" +
                "• Если шрифт не появился — проверьте название на fonts.google.com."
            )
            OutlinedTextField(
                value = fontName, onValueChange = { fontName = it },
                label = { Text("Название шрифта, например Roboto") },
                placeholder = { Text("Пусто = системный") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.medium
            )

            SectionHeader("Форма (углы)") { showShapeHelp = !showShapeHelp }
            if (showShapeHelp) InfoBox(
                "• Тип углов: скруглённые, срезанные (диагональ) или острые.\n" +
                "• Включите «Свой радиус», чтобы задать точное значение в dp — оно применится ко всем карточкам, кнопкам и чипам.\n" +
                "• Если «Свой радиус» выключен — используется пресет по типу углов.\n" +
                "• Рекомендуемые значения: 0 dp (острые), 4–8 dp (лёгкие), 12–20 dp (мягкие), 24+ dp (капсулы)."
            )
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Скругл." to CardStyle.ROUNDED, "Срезан." to CardStyle.CUT_CORNER, "Острые" to CardStyle.SHARP).forEach { (label, style) ->
                        FilterChip(selected = cardStyle == style, onClick = { cardStyle = style }, label = { Text(label, fontSize = 11.sp) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCustomRadius, onCheckedChange = { useCustomRadius = it })
                    Text("Свой радиус", fontSize = 12.sp)
                }
                if (useCustomRadius) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = customRadius, onValueChange = { customRadius = it },
                            valueRange = 0f..48f, modifier = Modifier.weight(1f)
                        )
                        Text("${customRadius.toInt()} dp", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            SectionHeader("Эффекты")
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = scanlines, onCheckedChange = { scanlines = it })
                    Text("Сканлайны", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = glow, onCheckedChange = { glow = it })
                    Text("Glow", fontSize = 12.sp)
                }
            }

            // ══════════════════════════════════════════════
            //  ОБОИ
            // ══════════════════════════════════════════════
            SectionHeader("Обои (PNG/JPG)")
            InfoBox(
                "• Выберите изображение из галереи — оно будет скопировано во внутреннее хранилище приложения, поэтому продолжит работать даже после удаления из галереи.\n" +
                "• Прозрачность: 0–100% (рекомендуем 20–40%, чтобы не мешать контенту).\n" +
                "• Масштаб: 50–300% — растягивайте или увеличивайте картинку.\n" +
                "• Смещение X/Y: двигайте изображение по экрану."
            )
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { imagePicker.launch("image/*") }) {
                        Icon(Icons.Filled.Image, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (wallpaperPath == null) "Выбрать картинку" else "Заменить")
                    }
                    if (wallpaperPath != null) {
                        OutlinedButton(onClick = {
                            WallpaperStorage.delete(wallpaperPath)
                            wallpaperPath = null
                        }) {
                            Icon(Icons.Filled.Delete, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Убрать")
                        }
                    }
                }
                val wpLocal = wallpaperPath
                if (!wpLocal.isNullOrEmpty() && File(wpLocal).exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(File(wpLocal)).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))
                    )
                    LabeledSlider("Прозрачность", wallpaperOpacity, 0f..100f, "%") { wallpaperOpacity = it }
                    LabeledSlider("Масштаб", wallpaperScale, 50f..300f, "%") { wallpaperScale = it }
                    LabeledSlider("Смещение X", wallpaperOffsetX, -100f..100f, "%") { wallpaperOffsetX = it }
                    LabeledSlider("Смещение Y", wallpaperOffsetY, -100f..100f, "%") { wallpaperOffsetY = it }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    colorPickerTarget?.let { target ->
        ColorPickerSheet(
            currentColor = when (target) { "primary" -> primary; "background" -> background; "surface" -> surface; "card" -> cardColor; "text" -> textColor; "price" -> priceColor; else -> primary },
            onColorSelected = { color ->
                when (target) { "primary" -> { primary = color; priceColor = color }; "background" -> background = color; "surface" -> surface = color; "card" -> cardColor = color; "text" -> textColor = color; "price" -> priceColor = color }
                colorPickerTarget = null
            },
            onDismiss = { colorPickerTarget = null }
        )
    }
}

// ── Общие UI-компоненты ─────────────────────────────────────────

@Composable
private fun SectionHeader(text: String, onHelpClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (onHelpClick != null) {
            IconButton(onClick = onHelpClick, Modifier.size(28.dp)) {
                Icon(Icons.Filled.Info, "Справка", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InfoBox(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String, value: Float, range: ClosedFloatingPointRange<Float>, suffix: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${value.toInt()}$suffix", style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun ColorDot(label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(color).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape).clickable(onClick = onClick))
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun ColorPickerSheet(currentColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val presets = listOf(
        Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF5E35B1),
        Color(0xFF3949AB), Color(0xFF1E88E5), Color(0xFF039BE5), Color(0xFF00ACC1),
        Color(0xFF00897B), Color(0xFF43A047), Color(0xFF7CB342), Color(0xFFFDD835),
        Color(0xFFFFB300), Color(0xFFFB8C00), Color(0xFFE65100), Color(0xFFCE1126),
        Color(0xFF00F0FF), Color(0xFFFF00E5), Color(0xFF39FF14), Color(0xFFFFE500),
        Color(0xFF6750A4), Color(0xFF00C853), Color(0xFFFFAB00), Color(0xFFFF4444),
        Color(0xFF0A0A12), Color(0xFF121212), Color(0xFF1A1A1A), Color(0xFF1E1E2E),
        Color(0xFF2A2A3A), Color(0xFF333333), Color(0xFF555555), Color(0xFF888888),
        Color(0xFFE0E0E8), Color(0xFFF5F0EB), Color(0xFFF0FBFF), Color(0xFFF4FFF0),
        Color(0xFFFFFFFF), Color(0xFFBBBBBB), Color(0xFF9999AA), Color(0xFF444455),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.chunked(8).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { color ->
                            Box(Modifier.size(32.dp).clip(CircleShape).background(color)
                                .border(if (color == currentColor) 3.dp else 0.5.dp, if (color == currentColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable { onColorSelected(color) })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

