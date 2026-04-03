package com.vkusnyvybor.ui.screens.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vkusnyvybor.ui.theme.engine.*

// ══════════════════════════════════════════════════════════════
//  Конструктор пользовательских тем
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeConstructorScreen(
    onBackClick: () -> Unit = {}
) {
    var themeName by remember { mutableStateOf("Моя тема") }

    // Цвета
    var primaryColor by remember { mutableStateOf(Color(0xFF6750A4)) }
    var backgroundColor by remember { mutableStateOf(Color(0xFF121212)) }
    var surfaceColor by remember { mutableStateOf(Color(0xFF1E1E2E)) }
    var accentColor by remember { mutableStateOf(Color(0xFFFF00E5)) }
    var textColor by remember { mutableStateOf(Color(0xFFE0E0E8)) }
    var cardColor by remember { mutableStateOf(Color(0xFF2A2A3A)) }

    // Форма кнопок
    var selectedShape by remember { mutableStateOf(CardStyle.ROUNDED) }

    // Эффекты
    var scanlines by remember { mutableStateOf(false) }
    var glowBorder by remember { mutableStateOf(false) }
    var gridOverlay by remember { mutableStateOf(false) }

    // Изображения (URI из галереи)
    var bgImageUri by remember { mutableStateOf<Uri?>(null) }
    var cardBgImageUri by remember { mutableStateOf<Uri?>(null) }
    var buttonImageUri by remember { mutableStateOf<Uri?>(null) }
    var logoImageUri by remember { mutableStateOf<Uri?>(null) }

    // Ползунки
    var bgOpacity by remember { mutableStateOf(30f) }
    var cardBgOpacity by remember { mutableStateOf(30f) }
    var buttonImageSize by remember { mutableStateOf(24f) }
    var logoSize by remember { mutableStateOf(40f) }

    // Пикеры изображений
    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> bgImageUri = uri }
    val cardBgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> cardBgImageUri = uri }
    val btnPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> buttonImageUri = uri }
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> logoImageUri = uri }

    // Показать/скрыть color picker
    var showColorPicker by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Конструктор тем") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                actions = {
                    TextButton(onClick = {
                        // Сохранение темы
                        val customTheme = buildCustomTheme(
                            name = themeName,
                            primary = primaryColor,
                            background = backgroundColor,
                            surface = surfaceColor,
                            accent = accentColor,
                            text = textColor,
                            card = cardColor,
                            cardStyle = selectedShape,
                            scanlines = scanlines,
                            glow = glowBorder,
                            grid = gridOverlay
                        )
                        ThemeEngine.registerTheme(customTheme)
                        ThemeEngine.setTheme(customTheme.id)
                        onBackClick()
                    }) {
                        Text("Сохранить", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Название ──────────────────────────────────
            SectionCard("Название темы") {
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            // ── Цвета ─────────────────────────────────────
            SectionCard("Цвета") {
                val colors = listOf(
                    "Primary" to primaryColor,
                    "Background" to backgroundColor,
                    "Surface" to surfaceColor,
                    "Accent" to accentColor,
                    "Text" to textColor,
                    "Card" to cardColor
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { (label, color) ->
                                ColorSlot(
                                    label = label,
                                    color = color,
                                    onClick = { showColorPicker = label },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Форма кнопок ──────────────────────────────
            SectionCard("Форма элементов") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    ShapeOption("Скруглённые", CardStyle.ROUNDED, selectedShape) { selectedShape = it }
                    ShapeOption("Срезанные", CardStyle.CUT_CORNER, selectedShape) { selectedShape = it }
                    ShapeOption("Острые", CardStyle.SHARP, selectedShape) { selectedShape = it }
                }
            }

            // ── Фон приложения ────────────────────────────
            SectionCard("Фон приложения") {
                ImagePickerRow(
                    label = "Фоновое изображение",
                    hasImage = bgImageUri != null,
                    onPick = { bgPicker.launch("image/*") },
                    onClear = { bgImageUri = null }
                )
                if (bgImageUri != null) {
                    SliderRow("Прозрачность", bgOpacity, 5f, 100f) { bgOpacity = it }
                }
            }

            // ── Логотип/Паттерн ───────────────────────────
            SectionCard("Логотип / Водяной знак") {
                Text(
                    "Логотип отображается поверх фона (например, эмблема Umbrella Corp)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(8.dp))
                ImagePickerRow(
                    label = "Загрузить логотип",
                    hasImage = logoImageUri != null,
                    onPick = { logoPicker.launch("image/*") },
                    onClear = { logoImageUri = null }
                )
                if (logoImageUri != null) {
                    SliderRow("Размер", logoSize, 20f, 120f) { logoSize = it }
                }
            }

            // ── Карточка товара ────────────────────────────
            SectionCard("Карточка товара") {
                ImagePickerRow(
                    label = "Фон карточки",
                    hasImage = cardBgImageUri != null,
                    onPick = { cardBgPicker.launch("image/*") },
                    onClear = { cardBgImageUri = null }
                )
                if (cardBgImageUri != null) {
                    SliderRow("Прозрачность", cardBgOpacity, 5f, 100f) { cardBgOpacity = it }
                }
            }

            // ── Кнопка «+» ───────────────────────────────
            SectionCard("Кнопка добавления") {
                ImagePickerRow(
                    label = "Иконка кнопки «+»",
                    hasImage = buttonImageUri != null,
                    onPick = { btnPicker.launch("image/*") },
                    onClear = { buttonImageUri = null }
                )
                if (buttonImageUri != null) {
                    SliderRow("Размер иконки", buttonImageSize, 16f, 48f) { buttonImageSize = it }
                }
            }

            // ── Эффекты ───────────────────────────────────
            SectionCard("Эффекты") {
                EffectToggle("Сканлайны (горизонтальные полоски)", scanlines) { scanlines = it }
                EffectToggle("Glow-рамка (свечение)", glowBorder) { glowBorder = it }
                EffectToggle("Сетка (фоновый паттерн)", gridOverlay) { gridOverlay = it }
            }

            // ── Превью ────────────────────────────────────
            SectionCard("Превью") {
                ThemePreviewMini(
                    primary = primaryColor,
                    background = backgroundColor,
                    surface = surfaceColor,
                    text = textColor,
                    card = cardColor,
                    shape = selectedShape,
                    glowBorder = glowBorder
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Color picker dialog
    showColorPicker?.let { label ->
        ColorPickerDialog(
            label = label,
            initialColor = when (label) {
                "Primary" -> primaryColor; "Background" -> backgroundColor
                "Surface" -> surfaceColor; "Accent" -> accentColor
                "Text" -> textColor; "Card" -> cardColor; else -> primaryColor
            },
            onColorSelected = { color ->
                when (label) {
                    "Primary" -> primaryColor = color; "Background" -> backgroundColor = color
                    "Surface" -> surfaceColor = color; "Accent" -> accentColor = color
                    "Text" -> textColor = color; "Card" -> cardColor = color
                }
                showColorPicker = null
            },
            onDismiss = { showColorPicker = null }
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  Компоненты конструктора
// ══════════════════════════════════════════════════════════════

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ColorSlot(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ShapeOption(label: String, style: CardStyle, selected: CardStyle, onSelect: (CardStyle) -> Unit) {
    val isSelected = style == selected
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "shape_color"
    )
    Surface(
        onClick = { onSelect(style) },
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImagePickerRow(label: String, hasImage: Boolean, onPick: () -> Unit, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (hasImage) {
                Text("Изображение загружено", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = onPick, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Icon(Icons.Filled.Image, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (hasImage) "Заменить" else "Загрузить", style = MaterialTheme.typography.labelMedium)
            }
            if (hasImage) {
                IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Close, "Удалить", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(100.dp))
        Slider(value = value, onValueChange = onChange, valueRange = min..max, modifier = Modifier.weight(1f))
        Text("${value.toInt()}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp))
    }
}

@Composable
private fun EffectToggle(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChanged(!checked) }.padding(vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

// ══════════════════════════════════════════════════════════════
//  Мини-превью темы
// ══════════════════════════════════════════════════════════════

@Composable
private fun ThemePreviewMini(primary: Color, background: Color, surface: Color, text: Color, card: Color, shape: CardStyle, glowBorder: Boolean) {
    val borderMod = if (glowBorder) Modifier.border(1.dp, primary.copy(0.5f), RoundedCornerShape(12.dp)) else Modifier
    val cornerRadius = when (shape) { CardStyle.ROUNDED -> 12.dp; CardStyle.CUT_CORNER -> 4.dp; CardStyle.SHARP -> 2.dp }

    Box(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .then(borderMod)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Поиск
            Box(Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(cornerRadius)).background(surface).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                Text("Найти блюдо...", fontSize = 11.sp, color = text.copy(0.4f))
            }
            // Карусель
            Box(Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(cornerRadius)).background(primary.copy(0.3f)), contentAlignment = Alignment.BottomStart) {
                Text("  Ресторан", fontSize = 12.sp, color = text, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            }
            // Чипы
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Бургеры", "Снэки").forEach { chip ->
                    Box(Modifier.clip(RoundedCornerShape(cornerRadius)).background(primary.copy(0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(chip, fontSize = 9.sp, color = primary)
                    }
                }
            }
            // Карточки
            repeat(2) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(card)
                        .then(if (glowBorder) Modifier.border(0.5.dp, primary.copy(0.3f), RoundedCornerShape(cornerRadius)) else Modifier)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text(if (it == 0) "Биг Спешал" else "Чизбургер", fontSize = 11.sp, color = text)
                        Box(Modifier.size(24.dp).clip(RoundedCornerShape(cornerRadius / 2)).background(primary), contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Color Picker Dialog (простой — сетка предустановленных цветов)
// ══════════════════════════════════════════════════════════════

@Composable
private fun ColorPickerDialog(label: String, initialColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val presets = listOf(
        // Ряд 1 — яркие
        Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA), Color(0xFF5E35B1),
        Color(0xFF3949AB), Color(0xFF1E88E5), Color(0xFF039BE5), Color(0xFF00ACC1),
        // Ряд 2 — зелёные/жёлтые
        Color(0xFF00897B), Color(0xFF43A047), Color(0xFF7CB342), Color(0xFFC0CA33),
        Color(0xFFFDD835), Color(0xFFFFB300), Color(0xFFFB8C00), Color(0xFFE65100),
        // Ряд 3 — неоновые
        Color(0xFF00F0FF), Color(0xFFFF00E5), Color(0xFF39FF14), Color(0xFFFFE500),
        Color(0xFF6750A4), Color(0xFFCE1126), Color(0xFF00C853), Color(0xFFFFAB00),
        // Ряд 4 — тёмные/нейтральные
        Color(0xFF0A0A12), Color(0xFF121212), Color(0xFF1A1A1A), Color(0xFF1E1E2E),
        Color(0xFF2A2A3A), Color(0xFF333333), Color(0xFF555555), Color(0xFF888888),
        // Ряд 5 — светлые
        Color(0xFFE0E0E8), Color(0xFFF5F0EB), Color(0xFFFFF8F7), Color(0xFFF0FBFF),
        Color(0xFFF4FFF0), Color(0xFFFFFFFF), Color(0xFFBBBBBB), Color(0xFF9999AA),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите цвет: $label") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.chunked(8).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { color ->
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (color == initialColor) 3.dp else 1.dp,
                                        if (color == initialColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

// ══════════════════════════════════════════════════════════════
//  Построение ThemeConfig из параметров конструктора
// ══════════════════════════════════════════════════════════════

private fun buildCustomTheme(
    name: String, primary: Color, background: Color, surface: Color,
    accent: Color, text: Color, card: Color, cardStyle: CardStyle,
    scanlines: Boolean, glow: Boolean, grid: Boolean
): ThemeConfig {
    val id = "custom_${name.lowercase().replace(" ", "_").take(20)}_${System.currentTimeMillis() % 10000}"

    return ThemeConfig(
        id = id,
        name = name,
        description = "Пользовательская тема",
        previewColors = listOf(primary, accent, background, card),
        lightScheme = androidx.compose.material3.lightColorScheme(
            primary = primary, onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.2f), onPrimaryContainer = primary,
            secondary = accent, onSecondary = Color.White,
            secondaryContainer = accent.copy(alpha = 0.2f), onSecondaryContainer = accent,
            background = Color.White, onBackground = Color(0xFF1A1A1A),
            surface = Color(0xFFF5F5F5), onSurface = Color(0xFF1A1A1A),
            surfaceVariant = Color(0xFFE8E8E8), onSurfaceVariant = Color(0xFF444444),
            outline = Color(0xFF888888), outlineVariant = Color(0xFFCCCCCC)
        ),
        darkScheme = androidx.compose.material3.darkColorScheme(
            primary = primary, onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.3f), onPrimaryContainer = primary,
            secondary = accent, onSecondary = Color.Black,
            secondaryContainer = accent.copy(alpha = 0.3f), onSecondaryContainer = accent,
            background = background, onBackground = text,
            surface = surface, onSurface = text,
            surfaceVariant = card, onSurfaceVariant = text.copy(alpha = 0.7f),
            outline = text.copy(alpha = 0.3f), outlineVariant = text.copy(alpha = 0.15f)
        ),
        shapes = when (cardStyle) {
            CardStyle.CUT_CORNER -> CyberShapes
            CardStyle.SHARP -> TerminalShapes
            CardStyle.ROUNDED -> null
        },
        decorations = ThemeDecorations(
            scanlineEffect = scanlines,
            scanlineColor = primary.copy(alpha = 0.03f),
            glowAccent = glow,
            glowColor = primary.copy(alpha = 0.08f),
            backgroundTexture = if (grid) BackgroundTexture.GRID else BackgroundTexture.NONE,
            cardStyle = cardStyle,
            dividerStyle = when (cardStyle) {
                CardStyle.CUT_CORNER -> DividerStyle.NEON_LINE
                CardStyle.SHARP -> DividerStyle.TERMINAL_DOTS
                else -> DividerStyle.SIMPLE
            }
        )
    )
}
