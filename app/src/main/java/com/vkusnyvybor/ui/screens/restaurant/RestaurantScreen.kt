package com.vkusnyvybor.ui.screens.restaurant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.model.RestaurantTab
import com.vkusnyvybor.ui.components.MenuItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreen(
    restaurantId: String,
    onBackClick: () -> Unit,
    onItemClick: (String, String) -> Unit = { _, _ -> },
    onCartClick: () -> Unit = {},
    viewModel: RestaurantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartStore.items.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val restaurant = uiState.restaurant

    if (restaurant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        floatingActionButton = {
            val cartSummary by remember {
                derivedStateOf {
                    val count = cartItems.sumOf { it.quantity }
                    val price = cartItems.sumOf { it.totalPrice }
                    count to price
                }
            }

            AnimatedVisibility(
                visible = cartSummary.first > 0,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = onCartClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Filled.ShoppingCart, "Корзина")
                    Spacer(Modifier.width(8.dp))
                    Text("${cartSummary.first} шт \u2022 ${cartSummary.second}\u20BD")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp)
        ) {
            item {
                RestaurantHero(restaurant = restaurant, onBackClick = onBackClick)
            }

            item {
                RestaurantInfo(restaurant = restaurant)
            }

            item {
                TabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            restaurant.categories.forEach { category ->
                item {
                    CategoryHeader(category.name)
                }
                
                itemsIndexed(
                    items = category.items,
                    key = { _, item -> item.id }
                ) { _, menuItem ->
                    val isFav = remember(favoriteIds) { menuItem.id in favoriteIds }
                    val quantity by remember(cartItems) { 
                        derivedStateOf { cartItems.find { it.menuItem.id == menuItem.id }?.quantity ?: 0 } 
                    }

                    MenuItemCard(
                        item = menuItem,
                        accentColor = restaurant.colors.primary,
                        quantity = quantity,
                        onAddToCart = { viewModel.addToCart(menuItem) },
                        onRemoveFromCart = { viewModel.removeFromCart(menuItem.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantHero(restaurant: Restaurant, onBackClick: () -> Unit) {
    val heroGradient = remember(restaurant.colors.gradientStart, restaurant.colors.gradientEnd) {
        Brush.verticalGradient(listOf(restaurant.colors.gradientStart, restaurant.colors.gradientEnd))
    }

    Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(heroGradient)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 12.dp, top = 44.dp)
                .background(Color.Black.copy(0.15f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(restaurant.slogan, color = Color.White.copy(0.8f), style = MaterialTheme.typography.labelLarge)
            Text(restaurant.name, color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(restaurant.subtitle, color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun RestaurantInfo(restaurant: Restaurant) {
    Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SuggestionChip(onClick = {}, label = { Text(restaurant.deliveryTime) }, icon = { Icon(Icons.Filled.AccessTime, null, Modifier.size(16.dp)) })
        SuggestionChip(onClick = {}, label = { Text("${restaurant.rating}") }, icon = { Icon(Icons.Filled.Star, null, Modifier.size(16.dp), tint = Color(0xFFFFB300)) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabSelector(selectedTab: RestaurantTab, onTabSelected: (RestaurantTab) -> Unit, accentColor: Color) {
    SecondaryScrollableTabRow(
        selectedTabIndex = remember(selectedTab) { RestaurantTab.entries.indexOf(selectedTab) },
        containerColor = Color.Transparent,
        contentColor = accentColor,
        edgePadding = 16.dp,
        divider = {}
    ) {
        RestaurantTab.entries.forEach { tab ->
            Tab(selected = selectedTab == tab, onClick = { onTabSelected(tab) }, text = { Text(tab.title) })
        }
    }
}

@Composable
private fun CategoryHeader(name: String) {
    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
}
