package com.vkusnyvybor.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════
//  ViewModel
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    val favoritesStore: FavoritesStore,
    val cartStore: CartStore
) : ViewModel() {

    val favoriteItems = favoritesStore.favoriteItems

    fun removeFavorite(item: MenuItem) = favoritesStore.toggle(item)
    fun addToCart(item: MenuItem) = cartStore.addItem(item)
}

// ══════════════════════════════════════════════════════════════
//  Screen
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onRestaurantClick: (String) -> Unit = {},
    onItemClick: (String, String) -> Unit = { _, _ -> },
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val isEmpty = favorites.isEmpty()

    var selectedFilter by remember { mutableStateOf<String?>(null) }

    val restaurants = favorites
        .map { it.restaurantId }
        .distinct()
        .map { id ->
            id to when (id) {
                "vkusno" -> "Вкусно и точка"
                "bk" -> "Бургер Кинг"
                "rostics" -> "Rostics"
                else -> id
            }
        }

    val filteredFavorites = if (selectedFilter != null) {
        favorites.filter { it.restaurantId == selectedFilter }
    } else {
        favorites
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Заголовок ─────────────────────────────────────
        Text(
            text = "Избранное",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        AnimatedContent(
            targetState = isEmpty,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "fav_content",
            modifier = Modifier.weight(1f)
        ) { isEmptyState ->
            if (isEmptyState) {
                EmptyFavoritesState()
            } else {
                Column {
                    // ── Фильтры ───────────────────────────
                    if (restaurants.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == null,
                                    onClick = { selectedFilter = null },
                                    label = { Text("Все (${favorites.size})") },
                                    leadingIcon = if (selectedFilter == null) {
                                        { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                            items(restaurants) { (id, name) ->
                                val count = favorites.count { it.restaurantId == id }
                                val accentColor = getRestaurantColor(id)
                                FilterChip(
                                    selected = selectedFilter == id,
                                    onClick = {
                                        selectedFilter = if (selectedFilter == id) null else id
                                    },
                                    label = { Text("$name ($count)") },
                                    leadingIcon = if (selectedFilter == id) {
                                        { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor.copy(alpha = 0.15f),
                                        selectedLabelColor = accentColor,
                                        selectedLeadingIconColor = accentColor
                                    )
                                )
                            }
                        }
                    }

                    // ── Список ────────────────────────────
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredFavorites,
                            key = { it.id }
                        ) { item ->
                            FavoriteItemCard(
                                item = item,
                                onRemove = { viewModel.removeFavorite(item) },
                                onAddToCart = { viewModel.addToCart(item) },
                                onClick = { onItemClick(item.restaurantId, item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Пустое состояние
// ══════════════════════════════════════════════════════════════

@Composable
private fun EmptyFavoritesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "empty_fav")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Icon(
                Icons.Outlined.FavoriteBorder,
                "Нет избранных",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Пока ничего нет",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Нажимайте \u2764\uFE0F на блюдах в меню ресторанов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Карточка избранного товара
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteItemCard(
    item: MenuItem,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val accentColor = getRestaurantColor(item.restaurantId)
    val restaurantName = when (item.restaurantId) {
        "vkusno" -> "Вкусно и точка"
        "bk" -> "Бургер Кинг"
        "rostics" -> "Rostics"
        else -> ""
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Изображение
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.08f),
                                accentColor.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getEmojiForCategory(item.category),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Инфо
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor)
                    )
                    Text(
                        text = restaurantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${item.price}\u20BD",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            // Кнопки
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Удалить из избранного
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        "Убрать",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Добавить в корзину
                FilledIconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Add, "В корзину", Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Утилиты ───────────────────────────────────────────────────

private fun getRestaurantColor(restaurantId: String): Color = when (restaurantId) {
    "vkusno" -> Color(0xFF1B5E20)
    "bk" -> Color(0xFFEC1C24)
    "rostics" -> Color(0xFFD32F2F)
    else -> Color(0xFF6750A4)
}

private fun getEmojiForCategory(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
