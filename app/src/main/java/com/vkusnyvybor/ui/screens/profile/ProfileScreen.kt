package com.vkusnyvybor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {}
) {
    val themeLogo = LocalThemeDecorations.current.themeLogo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Аватар и имя ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (themeLogo != null) {
                        Box(Modifier.size(100.dp)) {
                            themeLogo()
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Пользователь",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "+7 (999) 123-45-67",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SectionHeader("Заказы")
            ProfileMenuItem(
                icon = Icons.Outlined.Receipt,
                title = "Мои заказы",
                subtitle = "История и текущие заказы",
                onClick = onOrdersClick
            )
            ProfileMenuItem(
                icon = Icons.Outlined.LocationOn,
                title = "Адреса",
                subtitle = "Сохранённые предприятия",
                onClick = { }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader("Настройки")
            ProfileMenuItem(
                icon = Icons.Outlined.Palette,
                title = "Тема оформления",
                subtitle = "Material You, Cyberpunk, Umbrella...",
                onClick = onThemeClick
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Notifications,
                title = "Уведомления",
                subtitle = "Настроить оповещения",
                onClick = { }
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Language,
                title = "Язык",
                subtitle = "Русский",
                onClick = { }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader("О приложении")
            ProfileMenuItem(
                icon = Icons.Outlined.Info,
                title = "Версия",
                subtitle = "1.0.0",
                onClick = { }
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Description,
                title = "Пользовательское соглашение",
                onClick = { }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Выйти из аккаунта")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
