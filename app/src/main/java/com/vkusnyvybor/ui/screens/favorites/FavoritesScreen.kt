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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    val favoritesStore: FavoritesStore,
    val cartStore: CartStore
) : ViewModel() {
    val favoriteItems = favoritesStore.favoriteItems
    fun removeFavorite(item: MenuItem) = favoritesStore.toggle(item)
    fun addToCart(item: MenuItem) = cartStore.addItem(item)
}

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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Избранное",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
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
                                        label = { Text("Все") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                                items(restaurants) { (id, name) ->
                                    FilterChip(
                                        selected = selectedFilter == id,
                                        onClick = { selectedFilter = if (selectedFilter == id) null else id },
                                        label = { Text(name) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items = filteredFavorites, key = { it.id }) { item ->
                                FavoriteItemCard(
                                    item = item,
                                    onRemove = { viewModel.removeFavorite(item) },
                                    onAddToCart = { viewModel.addToCart(item) },
                                    onClick = { onItemClick(item.restaurantId, item.id) }
                                )
                            }
                            item { Spacer(Modifier.height(120.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            Text("Пока ничего нет", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Нажимайте \u2764\uFE0F на блюдах", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteItemCard(
    item: MenuItem,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val restaurantName = when (item.restaurantId) {
        "vkusno" -> "Вкусно и точка"
        "bk" -> "Бургер Кинг"
        "rostics" -> "Rostics"
        else -> ""
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(primaryColor.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForCategory(item.category), fontSize = 32.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(restaurantName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${item.price}\u20BD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Favorite, null, tint = Color(0xFFE53935))
                }
                FilledIconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun getEmojiForCategory(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Chicken", "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
