package com.vkusnyvybor.ui.screens.restaurant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuCategory
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.model.RestaurantColors
import com.vkusnyvybor.data.model.RestaurantTab
import com.vkusnyvybor.ui.components.MenuItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreen(
    restaurantId: String,
    onBackClick: () -> Unit,
    onItemClick: (String, String) -> Unit = { _, _ -> },
    viewModel: RestaurantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartStore.items.collectAsStateWithLifecycle()
    val restaurant = uiState.restaurant

    if (restaurant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Кастомный collapsing header не через TopAppBar —
            // просто кнопка назад поверх контента
        },
        floatingActionButton = {
            // FAB корзины (если есть товары)
            val totalCount = cartItems.sumOf { it.quantity }
            val totalPrice = cartItems.sumOf { it.totalPrice }

            AnimatedVisibility(
                visible = totalCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { /* navigate to cart */ },
                    containerColor = restaurant.colors.primary,
                    contentColor = restaurant.colors.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.ShoppingCart, "Корзина")
                    Spacer(Modifier.width(8.dp))
                    Text("$totalCount шт • ${totalPrice}₽")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp)
        ) {
            // ── Hero header с градиентом ──────────────────────
            item {
                RestaurantHero(
                    restaurant = restaurant,
                    onBackClick = onBackClick
                )
            }

            // ── Информация ────────────────────────────────────
            item {
                RestaurantInfo(restaurant = restaurant)
            }

            // ── Табы: Основное меню / Акции ───────────────────
            item {
                TabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    accentColor = restaurant.colors.primary
                )
            }

            // ── Категории и товары ────────────────────────────
            val categories = restaurant.categories
            categories.forEach { category ->
                item {
                    CategoryHeader(category.name)
                }
                items(
                    items = category.items,
                    key = { it.id }
                ) { menuItem ->
                    val isFav = menuItem.id in uiState.favorites
                    val displayItem = menuItem.copy(isFavorite = isFav)
                    val quantity = viewModel.getCartQuantity(menuItem.id)

                    MenuItemCard(
                        item = displayItem,
                        restaurantColors = restaurant.colors,
                        quantity = quantity,
                        onAddToCart = { viewModel.addToCart(menuItem) },
                        onRemoveFromCart = { viewModel.removeFromCart(menuItem.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(menuItem.id) },
                        onClick = { onItemClick(restaurant.id, menuItem.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Кнопка "ещё N позиций" ───────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text("ещё 50+ позиций")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RestaurantHero(
    restaurant: Restaurant,
    onBackClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        restaurant.colors.gradientStart,
                        restaurant.colors.gradientEnd,
                    )
                )
            )
    ) {
        // Декоративные круги с анимацией
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (offset * 0.3f).dp - 50.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        // Кнопка назад
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 8.dp, top = 40.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.2f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Назад",
                tint = Color.White
            )
        }

        // Информация в hero
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            // Здесь был бы логотип ресторана через Coil
            Text(
                text = restaurant.slogan,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = restaurant.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RestaurantInfo(restaurant: Restaurant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {},
            label = { Text(restaurant.deliveryTime) },
            leadingIcon = {
                Icon(Icons.Filled.Schedule, "Время", Modifier.size(16.dp))
            }
        )
        AssistChip(
            onClick = {},
            label = { Text(restaurant.deliveryPrice) },
            leadingIcon = {
                Icon(Icons.Filled.DeliveryDining, "Доставка", Modifier.size(16.dp))
            }
        )
        AssistChip(
            onClick = {},
            label = { Text("${restaurant.rating}") },
            leadingIcon = {
                Icon(Icons.Filled.Star, "Рейтинг", Modifier.size(16.dp))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabSelector(
    selectedTab: RestaurantTab,
    onTabSelected: (RestaurantTab) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = if (selectedTab == RestaurantTab.MAIN_MENU) "утреннее меню/основное" else "актуальные предложения",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            RestaurantTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    label = { Text(tab.title) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.15f),
                        selectedLabelColor = accentColor,
                        selectedLeadingIconColor = accentColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant,
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
