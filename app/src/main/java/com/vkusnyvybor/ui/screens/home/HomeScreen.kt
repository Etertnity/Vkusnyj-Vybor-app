package com.vkusnyvybor.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.model.RestaurantColors
import com.vkusnyvybor.ui.components.ProductBottomSheet
import com.vkusnyvybor.ui.theme.engine.LocalThemeDecorations
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onItemClick: (String, String) -> Unit = { _, _ -> },
    onCartClick: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(LocalContext.current as ViewModelStoreOwner)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartStore.items.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoritesStore.favoriteIds.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Высота липкой панели для корректного скролла
    val stickyHeaderHeightPx = with(density) { 64.dp.roundToPx() }

    var selectedProduct by remember { mutableStateOf<MenuItem?>(null) }
    val totalCount by remember { derivedStateOf { cartItems.sumOf { it.quantity } } }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Фиксированный поиск ────
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 2.dp
            ) {
                Column {
                    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    Spacer(Modifier.height(12.dp))
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onClear = { viewModel.clearSearch() },
                        cartCount = totalCount,
                        onCartClick = onCartClick,
                        onProfileClick = onProfileClick
                    )
                    Spacer(Modifier.height(8.dp))
                    // ── Адрес предприятия ────
                    AddressBar()
                    Spacer(Modifier.height(8.dp))
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Поиск ──────────────────────────────
                if (uiState.isSearching) {
                    items(uiState.searchResults, key = { "search_${it.id}" }) { item ->
                        MenuItemRow(
                            item = item,
                            restaurantColors = getColorsForRestaurant(item.restaurantId),
                            showRestaurantName = true,
                            onAddClick = { selectedProduct = item },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    return@LazyColumn
                }

                // ── 1: Material 3 Carousel (С эффектом сжатия) ────────────────────
                item(key = "carousel") {
                    Spacer(Modifier.height(16.dp))
                    RestaurantCarouselM3(
                        restaurants = uiState.restaurants,
                        onRestaurantSelected = { index -> viewModel.onRestaurantChanged(index) }
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // ── 2: STICKY чипсы категорий ────────────────
                stickyHeader(key = "category_sticky") {
                    val categories = uiState.menuCategories.map { it.name }
                    if (categories.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.background,
                            shadowElevation = 3.dp
                        ) {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                CategoryAnchors(
                                    categories = categories,
                                    onCategoryClick = { categoryName ->
                                        val idx = findCategoryListIndex(uiState.menuCategories, categoryName)
                                        scope.launch {
                                            // Скроллим так, чтобы заголовок был под чипсами
                                            listState.animateScrollToItem(idx, -stickyHeaderHeightPx)
                                        }
                                    }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // ── 3: Блюда ресторана ──────────────
                val restaurant = uiState.selectedRestaurant
                if (restaurant != null) {
                    uiState.menuCategories.forEach { category ->
                        item(key = "header_${category.id}") {
                            CategoryHeader(
                                name = category.name,
                                emoji = getCategoryEmoji(category.name),
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(items = category.items, key = { "item_${it.id}" }) { menuItem ->
                            MenuItemRow(
                                item = menuItem,
                                restaurantColors = restaurant.colors,
                                showRestaurantName = false,
                                onAddClick = { selectedProduct = menuItem },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB корзины
        AnimatedVisibility(
            visible = totalCount > 0,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp)
        ) {
            BadgedBox(badge = { Badge { Text("$totalCount") } }) {
                FloatingActionButton(
                    onClick = onCartClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { Icon(Icons.Filled.ShoppingCart, null) }
            }
        }

        selectedProduct?.let { product ->
            ProductBottomSheet(
                item = product,
                onDismiss = { selectedProduct = null },
                onAddToCart = { item, config ->
                    viewModel.cartStore.addItemWithConfig(item, config)
                    selectedProduct = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantCarouselM3(
    restaurants: List<Restaurant>,
    onRestaurantSelected: (Int) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { restaurants.size }
    val themeDecorations = LocalThemeDecorations.current
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(pagerState.currentPage) {
        onRestaurantSelected(pagerState.currentPage)
    }

    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { index ->
            val restaurant = restaurants[index]
            val themes = remember(restaurant.id) { getThematicEmojis(restaurant.id) }

            // Градиент: brand-цвета + примесь primary из темы
            val themeTint = primaryColor.copy(alpha = 0.2f)
            val gradient = Brush.linearGradient(
                listOf(
                    blendColors(restaurant.colors.gradientStart, themeTint, 0.15f),
                    blendColors(restaurant.colors.gradientEnd, themeTint, 0.25f)
                )
            )

            val borderModifier = if (themeDecorations.glowAccent) {
                Modifier.border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.5f),
                            primaryColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                )
            } else Modifier

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .then(borderModifier),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(Modifier.fillMaxSize().background(gradient)) {
                    // Декоративный круг — цвет из темы
                    Box(
                        Modifier
                            .size(120.dp)
                            .offset(x = 200.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.08f))
                    )

                    // Тематические emoji
                    if (themes.size >= 2) {
                        Text(
                            themes[0], fontSize = 36.sp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 24.dp)
                                .rotate(15f)
                        )
                        Text(
                            themes[1], fontSize = 44.sp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 32.dp, end = 48.dp)
                                .rotate(-8f)
                        )
                    }

                    // Сканлайн-эффект (если тема поддерживает)
                    if (themeDecorations.scanlineEffect) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            themeDecorations.scanlineColor,
                                            Color.Transparent,
                                            themeDecorations.scanlineColor,
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = 400f
                                    )
                                )
                        )
                    }

                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            restaurant.slogan,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(0.85f)
                        )
                        Column {
                            Text(
                                restaurant.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Color.White.copy(0.25f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Star, null, Modifier.size(14.dp), tint = Color.White)
                                        Spacer(Modifier.width(4.dp))
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
                                    color = Color.White.copy(0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Индикаторы
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(restaurants.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "dot"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

/** Смешивает два цвета с заданным весом (0 = base, 1 = blend). */
private fun blendColors(base: Color, blend: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = base.red * (1 - r) + blend.red * r,
        green = base.green * (1 - r) + blend.green * r,
        blue = base.blue * (1 - r) + blend.blue * r,
        alpha = 1f
    )
}

private fun findCategoryListIndex(categories: List<com.vkusnyvybor.data.model.MenuCategory>, name: String): Int {
    var count = 1 // Offset for carousel
    for (cat in categories) {
        if (cat.name == name) return count + 1
        count += 1 + cat.items.size
    }
    return 0
}

@Composable
private fun SearchBar(query: String, onQueryChanged: (String) -> Unit, onClear: () -> Unit, cartCount: Int, onCartClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Найти блюдо...") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Filled.Close, null) } },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )
        // Иконка профиля
        FilledIconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(Icons.Filled.Person, "Профиль", modifier = Modifier.size(22.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryAnchors(categories: List<String>, onCategoryClick: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { name ->
            AssistChip(
                onClick = { onCategoryClick(name) },
                label = { Text("${getCategoryEmoji(name)} $name") },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
private fun CategoryHeader(name: String, emoji: String, accentColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(emoji, fontSize = 24.sp)
        Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuItemRow(item: MenuItem, restaurantColors: RestaurantColors, showRestaurantName: Boolean, onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(onClick = onAddClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Text(getCategoryEmoji(item.category), fontSize = 28.sp) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (showRestaurantName) { Text(getRestaurantName(item.restaurantId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline) }
                if (item.description.isNotEmpty()) { Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.price}\u20BD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (item.oldPrice != null) { Text("${item.oldPrice}\u20BD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textDecoration = TextDecoration.LineThrough) }
                    }
                    Surface(
                        onClick = onAddClick,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Адрес предприятия (заглушка)
// ══════════════════════════════════════════════════════════════

@Composable
private fun AddressBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "В предприятии",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)
                )
                Text(
                    "Выберите предприятие",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = { /* TODO: открыть выбор предприятия */ },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Выбрать",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun getThematicEmojis(id: String): List<String> = when (id) { "vkusno" -> listOf("\uD83C\uDF5F", "\uD83C\uDF54"); "bk" -> listOf("\uD83D\uDC51", "\uD83C\uDF54"); "rostics" -> listOf("\uD83C\uDF57", "\uD83C\uDF2F"); else -> emptyList() }
private fun getRestaurantName(id: String): String = when (id) { "vkusno" -> "Вкусно и точка"; "bk" -> "Бургер Кинг"; "rostics" -> "Rostics"; else -> id }
private fun getCategoryEmoji(category: String): String = when (category) { "Бургеры" -> "\uD83C\uDF54"; "Гарниры" -> "\uD83C\uDF5F"; "Снэки" -> "\uD83C\uDF57"; "Напитки" -> "\uD83E\uDD64"; "Десерты" -> "\uD83E\uDD67"; "Роллы", "Твистеры" -> "\uD83C\uDF2F"; "Курица", "Курица" -> "\uD83C\uDF57"; else -> "\uD83C\uDF74" }
private fun getColorsForRestaurant(id: String): RestaurantColors = when (id) { "vkusno" -> RestaurantColors(Color(0xFF1B5E20), Color(0xFFFF6D00), Color(0xFFFFA726), gradientStart = Color(0xFF2E7D32), gradientEnd = Color(0xFF1B5E20)); "bk" -> RestaurantColors(Color(0xFFEC1C24), Color(0xFFFDBD10), Color(0xFF0066B2), gradientStart = Color(0xFFED7902), gradientEnd = Color(0xFFEC1C24)); "rostics" -> RestaurantColors(Color(0xFFD32F2F), Color(0xFFFFFFFF), Color(0xFFB71C1C), gradientStart = Color(0xFFE53935), gradientEnd = Color(0xFFC62828)); else -> RestaurantColors(Color(0xFF6750A4), Color(0xFF625B71), Color(0xFF7D5260)) }