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
import androidx.compose.material.icons.filled.Check
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
    onBackClick: () -> Unit = {}
) {
    val currentThemeId by ThemeEngine.currentThemeId.collectAsState()
    val themes = remember { ThemeEngine.availableThemes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тема оформления") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Выберите тему для приложения",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(themes, key = { it.id }) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == currentThemeId,
                    onSelect = { ThemeEngine.setTheme(theme.id) }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Создать свою тему",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Скоро вы сможете создавать и делиться собственными темами",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: ThemeConfig,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(),
        label = "border"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .clickable { onSelect() },
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        theme.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (theme.description.isNotEmpty()) {
                        Text(
                            theme.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Check,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Превью цветов
            if (theme.previewColors.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    theme.previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                    }
                }
            }

            // Бейджи
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (theme.useDynamicColor) {
                    ThemeBadge("Monet", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                }
                if (theme.forceDark == true) {
                    ThemeBadge("Тёмная", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (theme.decorations.scanlineEffect) {
                    ThemeBadge("Эффекты", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

@Composable
private fun ThemeBadge(text: String, containerColor: Color, contentColor: Color) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
