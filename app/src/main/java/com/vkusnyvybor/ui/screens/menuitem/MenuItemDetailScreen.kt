package com.vkusnyvybor.ui.screens.menuitem

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.model.RestaurantColors
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.data.repository.MockRepository
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════
//  ViewModel
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class MenuItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MockRepository,
    val cartStore: CartStore,
    private val favoritesStore: FavoritesStore
) : ViewModel() {

    private val restaurantId: String = savedStateHandle["restaurantId"] ?: ""
    private val itemId: String = savedStateHandle["itemId"] ?: ""

    private val _menuItem = MutableStateFlow<MenuItem?>(null)
    val menuItem: StateFlow<MenuItem?> = _menuItem.asStateFlow()

    private val _restaurant = MutableStateFlow<Restaurant?>(null)
    val restaurant: StateFlow<Restaurant?> = _restaurant.asStateFlow()

    val isFavorite: StateFlow<Boolean> get() = MutableStateFlow(favoritesStore.isFavorite(itemId))

    init {
        val rest = repository.getRestaurantById(restaurantId)
        _restaurant.value = rest
        _menuItem.value = rest?.categories
            ?.flatMap { it.items }
            ?.find { it.id == itemId }
    }

    fun toggleFavorite() {
        _menuItem.value?.let { favoritesStore.toggle(it) }
    }

    fun addToCart() {
        _menuItem.value?.let { cartStore.addItem(it) }
    }

    fun removeFromCart() {
        _menuItem.value?.let { cartStore.removeItem(it.id) }
    }

    fun getQuantity(): Int =
        _menuItem.value?.let { cartStore.getQuantity(it.id) } ?: 0
}

// ══════════════════════════════════════════════════════════════
//  Screen
// ══════════════════════════════════════════════════════════════

@Composable
fun MenuItemDetailScreen(
    restaurantId: String,
    itemId: String,
    onBackClick: () -> Unit,
    viewModel: MenuItemDetailViewModel = hiltViewModel()
) {
    val menuItem by viewModel.menuItem.collectAsStateWithLifecycle()
    val restaurant by viewModel.restaurant.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartStore.items.collectAsStateWithLifecycle()

    val item = menuItem
    val rest = restaurant

    if (item == null || rest == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colors = rest.colors
    val quantity = viewModel.getQuantity()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Hero ──────────────────────────────────────
            HeroSection(
                item = item,
                colors = colors,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onFavoriteToggle = { viewModel.toggleFavorite() }
            )

            // ── Контент ───────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Название и цена
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${item.price}\u20BD",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    if (item.oldPrice != null) {
                        Text(
                            text = "${item.oldPrice}\u20BD",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Surface(
                            color = Color(0xFFE53935).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            val discount = ((1 - item.price.toFloat() / item.oldPrice) * 100).toInt()
                            Text(
                                text = "-${discount}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Ресторан
                Surface(
                    color = colors.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Store, "Ресторан",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = rest.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star, "Рейтинг",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "${item.rating}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                // Описание
                if (item.description.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Описание",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Характеристики
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Характеристики",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    InfoRow("Вес", item.weight.ifEmpty { "—" })
                    InfoRow("Категория", item.category)
                    InfoRow("Рейтинг", "${item.rating} / 5.0")
                }

                // Пищевая ценность (моковые данные)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Пищевая ценность (на порцию)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NutritionChip("Ккал", "${(item.price * 1.2).toInt()}", colors.primary)
                        NutritionChip("Белки", "${(item.price * 0.05).toInt()}г", Color(0xFF4CAF50))
                        NutritionChip("Жиры", "${(item.price * 0.04).toInt()}г", Color(0xFFFFC107))
                        NutritionChip("Углеводы", "${(item.price * 0.08).toInt()}г", Color(0xFF2196F3))
                    }
                }

                // Отступ под кнопку
                Spacer(Modifier.height(100.dp))
            }
        }

        // ── Нижняя кнопка добавить ────────────────────────
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (quantity > 0) {
                    // Контролы количества
                    FilledIconButton(
                        onClick = { viewModel.removeFromCart() },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = colors.primary.copy(alpha = 0.12f),
                            contentColor = colors.primary
                        )
                    ) {
                        Icon(Icons.Filled.Remove, "Убрать")
                    }

                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 32.dp),
                    )

                    Button(
                        onClick = { viewModel.addToCart() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Ещё \u2022 ${item.price}\u20BD",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Button(
                        onClick = { viewModel.addToCart() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        )
                    ) {
                        Icon(Icons.Filled.ShoppingCart, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Добавить в корзину \u2022 ${item.price}\u20BD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Hero секция с градиентом и emoji
// ══════════════════════════════════════════════════════════════

@Composable
private fun HeroSection(
    item: MenuItem,
    colors: RestaurantColors,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "detail_hero")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_shift"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.gradientStart.copy(alpha = 0.15f),
                        colors.gradientEnd.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Декоративные круги
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (offset * 0.2f - 50).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 40.dp, y = 20.dp)
                .clip(CircleShape)
                .background(colors.secondary.copy(alpha = 0.06f))
        )

        // Emoji товара по центру
        val emojiScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "emoji_pulse"
        )

        Text(
            text = getDetailEmoji(item.category),
            fontSize = 96.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 10.dp)
        )

        // Навигация
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    "Избранное",
                    tint = if (isFavorite) Color(0xFFE53935)
                           else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Вспомогательные компоненты
// ══════════════════════════════════════════════════════════════

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NutritionChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getDetailEmoji(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
