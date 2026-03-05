package com.vkusnyvybor.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.ui.components.QuantitySelector
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onRestaurantClick: (String) -> Unit,
    onItemClick: (String, String) -> Unit = { _, _ -> },
    onOrderClick: (String) -> Unit = {},
    onCartClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(LocalContext.current as ViewModelStoreOwner)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartStore.items.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        LoadingScreen()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(Modifier.height(16.dp))

                AnimatedGreeting()
                
                SearchBarSection(
                    query = uiState.searchQuery,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onClear = { viewModel.clearSearch() }
                )

                if (uiState.categories.isNotEmpty()) {
                    CategoryFilters(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { viewModel.onCategorySelected(it) }
                    )
                }

                AnimatedContent(
                    targetState = uiState.isSearching,
                    transitionSpec = { 
                        (fadeIn(tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(tween(90)))
                    },
                    label = "home_content"
                ) { isSearching ->
                    if (isSearching) {
                        Column {
                            SearchResultsSection(
                                results = uiState.searchResults,
                                query = uiState.searchQuery,
                                onItemClick = onItemClick,
                                onAddToCart = { viewModel.addToCart(it) },
                                onRemoveFromCart = { viewModel.removeFromCart(it.id) },
                                getQuantity = { viewModel.cartStore.getQuantity(it) }
                            )
                            Spacer(Modifier.height(120.dp))
                        }
                    } else {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            
                            if (uiState.restaurants.isNotEmpty()) {
                                RestaurantCarouselOptimized(
                                    restaurants = uiState.restaurants,
                                    onRestaurantClick = onRestaurantClick
                                )
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            if (uiState.allMenuItems.isNotEmpty()) {
                                QuickPicksSection(
                                    items = remember(uiState.allMenuItems) {
                                        uiState.allMenuItems.shuffled().take(5)
                                    },
                                    onItemClick = onItemClick,
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onRemoveFromCart = { viewModel.removeFromCart(it.id) },
                                    getQuantity = { viewModel.cartStore.getQuantity(it) }
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
                                    items(uiState.recentOrders, key = { it.id }) { order ->
                                        RecentOrderCard(
                                            order = order,
                                            onClick = { onOrderClick(order.id) }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(120.dp))
                        }
                    }
                }
            }

            val totalCount by remember { derivedStateOf { cartItems.sumOf { it.quantity } } }
            val totalPrice by remember { derivedStateOf { cartItems.sumOf { it.totalPrice } } }

            AnimatedVisibility(
                visible = totalCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = onCartClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ShoppingCart, "Корзина")
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Корзина \u2022 $totalCount шт",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "${totalPrice}\u20BD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Загрузка...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AnimatedGreeting() {
    val greetingText = "Рады вас видеть! 😊"
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        greetingText.forEachIndexed { index, _ ->
            visibleText = greetingText.take(index + 1)
            delay(50)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val infiniteTransition = rememberInfiniteTransition(label = "wave")
            val waveRotation by infiniteTransition.animateFloat(
                initialValue = -15f, targetValue = 15f,
                animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "wave"
            )
            Text(text = "👋", fontSize = 24.sp, modifier = Modifier.graphicsLayer { rotationZ = waveRotation })
            Spacer(Modifier.width(8.dp))
            Text(text = "Привет!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = visibleText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            minLines = 1
        )
    }
}

@Composable
private fun SearchBarSection(query: String, onQueryChanged: (String) -> Unit, onClear: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Найти блюдо...") },
        leadingIcon = { Icon(Icons.Filled.Search, null) },
        trailingIcon = {
            if (query.isNotEmpty()) IconButton(onClick = onClear) { Icon(Icons.Filled.Close, null) }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilters(categories: List<String>, selectedCategory: String?, onCategorySelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text("${getCategoryEmoji(category)} $category") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantCarouselOptimized(
    restaurants: List<Restaurant>,
    onRestaurantClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    val itemWidth = remember(screenWidth) { (screenWidth * 0.82f).coerceAtMost(320.dp) }
    val carouselState = rememberCarouselState { restaurants.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        preferredItemWidth = itemWidth,
        modifier = Modifier.fillMaxWidth().height(210.dp),
        itemSpacing = 16.dp,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) { index ->
        val restaurant = restaurants[index]
        RestaurantBannerCard(
            restaurant = restaurant,
            onClick = { onRestaurantClick(restaurant.id) },
            modifier = Modifier.fillMaxHeight().maskClip(MaterialTheme.shapes.extraLarge)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestaurantBannerCard(restaurant: Restaurant, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val themes = remember(restaurant.id) { getThematicEmojis(restaurant.id) }
    val gradient = remember(restaurant.colors.gradientStart, restaurant.colors.gradientEnd) {
        Brush.linearGradient(colors = listOf(restaurant.colors.gradientStart, restaurant.colors.gradientEnd))
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(gradient)) {
            val infiniteTransition = rememberInfiniteTransition(label = "emoji")
            val floatAnim by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 12f,
                animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "float"
            )

            Box(Modifier.size(120.dp).offset(x = 200.dp, y = (-30).dp).clip(CircleShape).background(Color.White.copy(0.08f)))

            if (themes.size >= 2) {
                Text(
                    themes[0], fontSize = 38.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp, end = 25.dp)
                        .graphicsLayer { 
                            translationY = floatAnim
                            rotationZ = 12f
                        }
                )
                Text(
                    themes[1], fontSize = 48.sp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 30.dp, end = 55.dp)
                        .graphicsLayer { 
                            translationY = -floatAnim
                            rotationZ = -8f
                        }
                )
            }

            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Text(restaurant.slogan, style = MaterialTheme.typography.labelMedium, color = restaurant.colors.onPrimary.copy(0.8f))
                Column {
                    Text(restaurant.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = restaurant.colors.onPrimary)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = Color.White.copy(0.2f), shape = MaterialTheme.shapes.small) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, Modifier.size(14.dp), tint = Color.White)
                                Text("${restaurant.rating}", style = MaterialTheme.typography.labelMedium, color = Color.White)
                            }
                        }
                        Text(restaurant.deliveryTime, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.9f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPicksSection(
    items: List<MenuItem>, 
    onItemClick: (String, String) -> Unit, 
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    getQuantity: (String) -> Int
) {
    Column {
        Text("\uD83C\uDF1F Попробуйте сегодня", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.id }) { item ->
                val quantity = getQuantity(item.id)
                QuickPickCard(item, quantity, { onItemClick(item.restaurantId, item.id) }, { onAddToCart(item) }, { onRemoveFromCart(item) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickPickCard(item: MenuItem, quantity: Int, onClick: () -> Unit, onAddToCart: () -> Unit, onRemoveFromCart: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val restaurantColor = remember(item.restaurantId) { getRestaurantColor(item.restaurantId) }
    
    ElevatedCard(onClick = onClick, modifier = Modifier.width(160.dp), shape = MaterialTheme.shapes.large) {
        Column {
            Box(Modifier.fillMaxWidth().height(100.dp).background(restaurantColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                Text(getCategoryEmoji(item.category), fontSize = 44.sp)
            }
            Column(Modifier.padding(12.dp)) {
                Text(item.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(getRestaurantName(item.restaurantId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("${item.price}\u20BD", fontWeight = FontWeight.Bold, color = restaurantColor)
                    
                    QuantitySelector(
                        quantity = quantity,
                        onIncrease = onAddToCart,
                        onDecrease = onRemoveFromCart,
                        usePrimaryForButtons = true,
                        accentColor = restaurantColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    results: List<MenuItem>, 
    query: String, 
    onItemClick: (String, String) -> Unit, 
    onAddToCart: (MenuItem) -> Unit,
    onRemoveFromCart: (MenuItem) -> Unit,
    getQuantity: (String) -> Int
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (results.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            results.forEach { item ->
                val quantity = getQuantity(item.id)
                SearchResultCard(item, quantity, { onItemClick(item.restaurantId, item.id) }, { onAddToCart(item) }, { onRemoveFromCart(item) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultCard(item: MenuItem, quantity: Int, onClick: () -> Unit, onAddToCart: () -> Unit, onRemoveFromCart: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val restaurantColor = remember(item.restaurantId) { getRestaurantColor(item.restaurantId) }
    
    ElevatedCard(onClick = onClick, shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(MaterialTheme.shapes.medium).background(restaurantColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                Text(getCategoryEmoji(item.category), fontSize = 32.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(getRestaurantName(item.restaurantId), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${item.price}\u20BD", style = MaterialTheme.typography.titleMedium, color = restaurantColor, fontWeight = FontWeight.Bold)
            }
            
            QuantitySelector(
                quantity = quantity,
                onIncrease = onAddToCart,
                onDecrease = onRemoveFromCart,
                usePrimaryForButtons = true,
                accentColor = restaurantColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentOrderCard(order: Order, onClick: () -> Unit) {
    val restaurantColor = remember(order.restaurantId) { getRestaurantColor(order.restaurantId) }
    ElevatedCard(onClick = onClick, modifier = Modifier.width(160.dp), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(12.dp), Arrangement.spacedBy(4.dp)) {
            Text(getRestaurantEmoji(order.restaurantId), fontSize = 24.sp)
            Text(order.restaurantName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${order.totalPrice}\u20BD", color = restaurantColor, fontWeight = FontWeight.Bold)
            Text(order.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private fun getThematicEmojis(id: String): List<String> = when (id) {
    "vkusno" -> listOf("\uD83C\uDF5F", "\uD83C\uDF54")
    "bk" -> listOf("\uD83D\uDC51", "\uD83C\uDF54")
    "rostics" -> listOf("\uD83C\uDF57", "\uD83C\uDF2F")
    else -> emptyList()
}

private fun getRestaurantColor(id: String): Color = when (id) {
    "vkusno" -> Color(0xFF1B5E20)
    "bk" -> Color(0xFFEC1C24)
    "rostics" -> Color(0xFFD32F2F)
    else -> Color(0xFF6750A4)
}

private fun getRestaurantName(id: String): String = when (id) {
    "vkusno" -> "Вкусно и точка"
    "bk" -> "Бургер Кинг"
    "rostics" -> "Rostics"
    else -> id
}

private fun getRestaurantEmoji(id: String): String = when (id) {
    "vkusno" -> "\uD83C\uDF54"
    "bk" -> "\uD83D\uDD25"
    "rostics" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}

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
