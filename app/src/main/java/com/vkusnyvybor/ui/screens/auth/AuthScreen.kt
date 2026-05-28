package com.vkusnyvybor.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.R
import com.vkusnyvybor.ui.theme.engine.BackgroundTexture
import com.vkusnyvybor.ui.theme.engine.BiohazardStripe
import com.vkusnyvybor.ui.theme.engine.DividerStyle
import com.vkusnyvybor.ui.theme.engine.GridOverlay
import com.vkusnyvybor.ui.theme.engine.HexGridOverlay
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations
import com.vkusnyvybor.ui.theme.engine.NeonDivider
import com.vkusnyvybor.ui.theme.engine.ScanlineOverlay
import com.vkusnyvybor.ui.theme.engine.TerminalDots
import com.vkusnyvybor.ui.theme.engine.ThemeDecorations

/**
 * Экран авторизации.
 *
 * Поток (как описано в auth-service): локального хэша нет → показываем одну
 * кнопку «Войти через Telegram». Регистрация и вход — это один и тот же шаг
 * (OpenID Connect через Telegram), отдельное имя пользователь не вводит:
 * стабильный идентификатор формируется сервисом как SHA256(Telegram ID + соль).
 *
 * Весь визуал завязан на текущую тему приложения (палитра, типографика, формы,
 * фоновые текстуры, glow и тематический разделитель из движка тем).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthorized: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val decorations = LocalThemeDecorations.current

    // Срабатывает, когда сессия успешно сохранилась.
    LaunchedEffect(state.authorized) {
        if (state.authorized) onAuthorized()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    // ── Корневой контейнер: фон темы (текстура + сканлайны) под контентом ──
    Box(modifier = Modifier.fillMaxSize()) {
        ThemedAuthBackground(decorations)

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                // ── Тематический логотип ─────────────────────────────
                AnimatedLogo(themeLogo = decorations.themeLogo, glow = decorations.glowAccent)

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Вкусный Выбор",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Войдите через Telegram, чтобы продолжить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(32.dp))

                // ── Карточка с единственной кнопкой входа ───────────
                AuthCard(
                    state = state,
                    glow = decorations.glowAccent,
                    dividerStyle = decorations.dividerStyle,
                    onTelegramClick = viewModel::startTelegramLogin,
                    onConfirmReturn = viewModel::confirmTelegramReturn,
                    onGuestClick = viewModel::continueAsGuest
                )

                Spacer(Modifier.height(24.dp))

                // ── Подпись ─────────────────────────────────────────
                Text(
                    text = "Защищено протоколом OpenID Connect • Telegram",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Окно входа через Telegram (встроенный WebView) ──────────
        // Открывается поверх экрана, как только получен auth_url. Сам читает
        // ответ callback и отдаёт реальные user_hash + username.
        state.authUrl?.let { url ->
            TelegramAuthWebView(
                url = url,
                onResult = { hash, name -> viewModel.completeTelegramLogin(hash, name) },
                onClose = viewModel::cancelTelegramLogin
            )
        }
    }
}

/**
 * Фоновый слой, повторяющий декорации текущей темы — так экран авторизации
 * выглядит единообразно с остальным приложением под любой кастомной темой.
 * Рисуется поверх глобальных обоев (если они есть) и под контентом.
 */
@Composable
private fun ThemedAuthBackground(decorations: ThemeDecorations) {
    when (decorations.backgroundTexture) {
        BackgroundTexture.GRID -> GridOverlay()
        BackgroundTexture.HEXAGONAL -> HexGridOverlay()
        BackgroundTexture.SCANLINES -> ScanlineOverlay(color = decorations.scanlineColor)
        else -> { /* NONE / NOISE — без дополнительного оверлея */ }
    }
    // Эффект сканлайнов поверх (если тема включает его отдельно от текстуры)
    if (decorations.scanlineEffect && decorations.backgroundTexture != BackgroundTexture.SCANLINES) {
        ScanlineOverlay(color = decorations.scanlineColor)
    }
}

@Composable
private fun AnimatedLogo(themeLogo: (@Composable () -> Unit)?, glow: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "auth_logo")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(140.dp)
            .scale(pulse),
        contentAlignment = Alignment.Center
    ) {
        // Декоративное «свечение» в цвете primary текущей темы.
        // Чуть ярче, если тема просит акцентного свечения (glowAccent).
        val glowAlpha = if (glow) 0.28f else 0.18f
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        if (themeLogo != null) {
            Box(Modifier.size(120.dp)) { themeLogo() }
        } else {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Логотип",
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthCard(
    state: AuthUiState,
    glow: Boolean,
    dividerStyle: DividerStyle,
    onTelegramClick: () -> Unit,
    onConfirmReturn: () -> Unit,
    onGuestClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    // Glow-обводка карточки — тот же приём, что и на карточках главного экрана.
    val glowModifier = if (glow) {
        Modifier.border(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(
                    primary.copy(alpha = 0.5f),
                    primary.copy(alpha = 0.1f),
                    Color.Transparent
                )
            ),
            shape = MaterialTheme.shapes.extraLarge
        )
    } else Modifier

    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .then(glowModifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Главная (и единственная) кнопка — Telegram OAuth ─
            Button(
                onClick = onTelegramClick,
                enabled = !state.loading,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Связываемся со шлюзом…")
                } else {
                    Icon(Icons.Filled.Send, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Войти через Telegram",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Сообщение об ожидании возврата из браузера ──────
            AnimatedVisibility(
                visible = state.awaitingTelegram,
                enter = fadeIn(), exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info, null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Завершите вход в браузере и вернитесь сюда.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onConfirmReturn,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Я подтвердил вход")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Разделитель «или» в стиле текущей темы ──────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemedDivider(style = dividerStyle, modifier = Modifier.weight(1f))
                Text(
                    text = "  или  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                ThemedDivider(style = dividerStyle, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // ── Гостевой режим — запасной/оффлайн-вариант ───────
            TextButton(
                onClick = onGuestClick,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.AccountCircle, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Продолжить как гость")
            }
        }
    }
}

/** Разделитель, форма которого зависит от стиля текущей темы. */
@Composable
private fun ThemedDivider(style: DividerStyle, modifier: Modifier = Modifier) {
    when (style) {
        DividerStyle.NEON_LINE -> NeonDivider(modifier = modifier)
        DividerStyle.BIOHAZARD_STRIPE -> BiohazardStripe(modifier = modifier)
        DividerStyle.TERMINAL_DOTS -> TerminalDots(modifier = modifier)
        DividerStyle.SIMPLE -> HorizontalDivider(
            modifier = modifier,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
