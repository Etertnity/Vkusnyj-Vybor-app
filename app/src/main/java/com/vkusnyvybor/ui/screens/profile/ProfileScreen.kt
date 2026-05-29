package com.vkusnyvybor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.repository.AuthMode
import com.vkusnyvybor.ui.localization.AppLanguage
import com.vkusnyvybor.ui.localization.LocalStrings
import com.vkusnyvybor.ui.localization.LocalizationEngine
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val themeLogo = LocalThemeDecorations.current.themeLogo
    val session by viewModel.session.collectAsStateWithLifecycle()
    val s = LocalStrings.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageDialog(
            current = LocalizationEngine.current,
            title = s.chooseLanguage,
            onSelect = {
                LocalizationEngine.setLanguage(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.profileTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back)
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
                    session?.username ?: "Пользователь",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (session?.mode) {
                        AuthMode.TELEGRAM -> s.authorizedViaTelegram
                        AuthMode.GUEST -> s.guestMode
                        null -> "+7 (999) 123-45-67"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SectionHeader(s.sectionOrders)
            ProfileMenuItem(
                icon = Icons.Outlined.Receipt,
                title = s.myOrders,
                subtitle = s.myOrdersSub,
                onClick = onOrdersClick
            )
            ProfileMenuItem(
                icon = Icons.Outlined.LocationOn,
                title = s.addresses,
                subtitle = s.addressesSub,
                onClick = { }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader(s.sectionSettings)
            ProfileMenuItem(
                icon = Icons.Outlined.Palette,
                title = s.themeTitle,
                subtitle = "Material You, Cyberpunk, Umbrella...",
                onClick = onThemeClick
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Notifications,
                title = s.notifications,
                subtitle = s.notificationsSub,
                onClick = { }
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Language,
                title = s.language,
                subtitle = LocalizationEngine.current.nativeName,
                onClick = { showLanguageDialog = true }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SectionHeader(s.sectionAbout)
            ProfileMenuItem(
                icon = Icons.Outlined.Info,
                title = s.version,
                subtitle = "1.0.0",
                onClick = { }
            )
            ProfileMenuItem(
                icon = Icons.Outlined.Description,
                title = s.userAgreement,
                onClick = { }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
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
                Text(s.logout)
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

@Composable
private fun LanguageDialog(
    current: AppLanguage,
    title: String,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                AppLanguage.entries.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = lang == current,
                            onClick = { onSelect(lang) }
                        )
                        Text(
                            lang.nativeName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
