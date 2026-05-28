package com.vkusnyvybor.ui.screens.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vkusnyvybor.ui.theme.engine.ThemeConfig
import com.vkusnyvybor.ui.theme.engine.ThemeEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerScreen(
    onBackClick: () -> Unit = {},
    onConstructorClick: () -> Unit = {},
    onEditTheme: (String) -> Unit = {}
) {
    val currentThemeId by ThemeEngine.currentThemeId.collectAsState()
    var themes by remember { mutableStateOf(ThemeEngine.availableThemes) }
    var deleteDialog by remember { mutableStateOf<String?>(null) }

    // Перечитываем список при смене текущей темы (в т.ч. после сохранения новой из конструктора)
    LaunchedEffect(currentThemeId) { themes = ThemeEngine.availableThemes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тема оформления") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Выберите тему для приложения", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(themes, key = { it.id }) { theme ->
                val isCustom = ThemeEngine.isCustomTheme(theme.id)
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    isCustom = isCustom,
                    onSelect = { ThemeEngine.setTheme(theme.id) },
                    onEdit = { onEditTheme(theme.id) },
                    onDelete = { deleteDialog = theme.id }
                )
            }

            // Создать тему
            item {
                Spacer(Modifier.height(4.dp))
                ElevatedCard(
                    onClick = onConstructorClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp)) }
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Создать свою тему", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Цвета, формы, шрифты, эффекты", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }

    // Диалог удаления
    deleteDialog?.let { themeId ->
        AlertDialog(
            onDismissRequest = { deleteDialog = null },
            icon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Удалить тему?") },
            text = { Text("Тема будет удалена. Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    ThemeEngine.removeTheme(themeId)
                    themes = ThemeEngine.availableThemes
                    deleteDialog = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = null }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun ThemeCard(theme: ThemeConfig, isSelected: Boolean, isCustom: Boolean, onSelect: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(), label = "border"
    )
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
            .border(if (isSelected) 2.dp else 0.dp, borderColor, MaterialTheme.shapes.large)
            .clickable { onSelect() },
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(theme.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isCustom) {
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                                Text("Своя", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                            }
                        }
                    }
                    if (theme.description.isNotEmpty()) Text(theme.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isCustom) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Edit, "Редактировать", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Delete, "Удалить", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    if (isSelected) {
                        Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(28.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
            }
            if (theme.previewColors.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    theme.previewColors.forEach { color ->
                        Box(Modifier.size(28.dp).clip(CircleShape).background(color).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (theme.useDynamicColor) ThemeBadge("Monet", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                if (theme.decorations.scanlineEffect) ThemeBadge("Эффекты", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun ThemeBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, shape = MaterialTheme.shapes.small) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
