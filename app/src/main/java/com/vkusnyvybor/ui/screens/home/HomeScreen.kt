package com.vkusnyvybor.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onRestaurantClick: (String) -> Unit,
    onItemClick: (String, String) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Верхняя панель ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "Аватар",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Notifications, "Уведомления")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Settings, "Настройки")
                }
            }
        }

        // ── Заголовок ─────────────────────────────────────────
        Text(
            text = "Главное",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // ── Поисковая строка ──────────────────────────────────
        SearchBarSection(
            query = uiState.searchQuery,
            onQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onClear = { viewModel.clearSearch() }
        )

        // ── Фильтры категорий ─────────────────────────────────
        if (uiState.categories.isNotEmpty()) {
            CategoryFilters(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )
        }

        // ── Контент: поиск или обычный ────────────────────────
        AnimatedContent(
            targetState = uiState.isSearching,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "home_content"
        ) { isSearching ->
            if (isSearching) {
                // Результаты поиска
                SearchResultsSection(
                    results = uiState.searchResults,
                    query = uiState.searchQuery,
                    onItemClick = onItemClick,
                    onAddToCart = { viewModel.addToCart(it) }
                )
            } else {
                // Обычный контент
                Column {
                    Spacer(Modifier.height(8.dp))

                    if (uiState.restaurants.isNotEmpty()) {
                        RestaurantCarousel(
                            restaurants = uiState.restaurants,
                            onRestaurantClick = onRestaurantClick
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    if (uiState.recentOrders.isNotEmpty()) {
                        Text(
                            text = "Последние заказы",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recentOrders) { order ->
                                RecentOrderCard(
                                    order = order,
                                    onClick = { onRestaurantClick(order.restaurantId) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Поисковая строка
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarSection(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                "Найти блюдо или ресторан...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                "Поиск",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, "Очистить")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    )
}

// ══════════════════════════════════════════════════════════════
//  Фильтры категорий
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilters(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        "${getCategoryEmoji(category)} $category",
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Результаты поиска
// ══════════════════════════════════════════════════════════════

@Composable
private fun SearchResultsSection(
    results: List<MenuItem>,
    query: String,
    onItemClick: (String, String) -> Unit,
    onAddToCart: (MenuItem) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))

        if (results.isEmpty()) {
            // Ничего не найдено
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        "Ничего не найдено",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (query.isNotBlank()) {
                        Text(
                            "Попробуйте другой запрос",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            Text(
                "Найдено: ${results.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            results.forEach { item ->
                SearchResultCard(
                    item = item,
                    onClick = { onItemClick(item.restaurantId, item.id) },
                    onAddToCart = { onAddToCart(item) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultCard(
    item: MenuItem,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val accentColor = when (item.restaurantId) {
        "vkusno" -> Color(0xFF1B5E20)
        "bk" -> Color(0xFFEC1C24)
        "rostics" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.primary
    }
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
            Box(
                modifier = Modifier
                    .size(60.dp)
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
                    getCategoryEmoji(item.category),
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    item.name,
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
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        restaurantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "\u2022 ${item.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${item.price}\u20BD",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            FilledIconButton(
                onClick = onAddToCart,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = accentColor,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Add, "Добавить", Modifier.size(18.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Карусель ресторанов (без изменений)
// ══════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RestaurantCarousel(
    restaurants: List<Restaurant>,
    onRestaurantClick: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { restaurants.size })

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            val restaurant = restaurants[page]
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue

            RestaurantBanner(
                restaurant = restaurant,
                modifier = Modifier
                    .graphicsLayer {
                        val scale = lerp(1f, 0.85f, pageOffset.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(1f, 0.5f, pageOffset.coerceIn(0f, 1f))
                    }
                    .fillMaxSize()
                    .clickable { onRestaurantClick(restaurant.id) }
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(restaurants.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "indicator_width"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) restaurants[index].colors.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
private fun RestaurantBanner(
    restaurant: Restaurant,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_gradient")
    val offsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    Card(
        modifier = modifier.shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            restaurant.colors.gradientStart,
                            restaurant.colors.gradientEnd,
                            restaurant.colors.accent.copy(alpha = 0.8f)
                        ),
                        start = Offset(offsetAnim, 0f),
                        end = Offset(offsetAnim + 600f, 400f)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = restaurant.slogan,
                    style = MaterialTheme.typography.titleMedium,
                    color = restaurant.colors.onPrimary.copy(alpha = 0.9f)
                )

                Column {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = restaurant.colors.onPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star, "Рейтинг",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "${restaurant.rating}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            restaurant.deliveryTime,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            restaurant.deliveryPrice,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDED2", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = order.restaurantName,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                text = "${order.totalPrice}\u20BD",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Утилита ───────────────────────────────────────────────────

private fun getCategoryEmoji(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
