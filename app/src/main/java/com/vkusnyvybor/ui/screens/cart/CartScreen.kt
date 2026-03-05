package com.vkusnyvybor.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.vkusnyvybor.data.model.CartItem
import com.vkusnyvybor.data.model.MenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

// ══════════════════════════════════════════════════════════════
//  CartStore — глобальное хранилище корзины (singleton)
// ══════════════════════════════════════════════════════════════

@Singleton
class CartStore @Inject constructor() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val totalPrice: Int get() = _items.value.sumOf { it.totalPrice }
    val totalCount: Int get() = _items.value.sumOf { it.quantity }

    fun addItem(menuItem: MenuItem) {
        _items.update { current ->
            val existing = current.find { it.menuItem.id == menuItem.id }
            if (existing != null) {
                current.map {
                    if (it.menuItem.id == menuItem.id) it.copy(quantity = it.quantity + 1)
                    else it
                }
            } else {
                current + CartItem(menuItem, 1)
            }
        }
    }

    fun removeItem(menuItemId: String) {
        _items.update { current ->
            current.mapNotNull {
                if (it.menuItem.id == menuItemId) {
                    if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
                } else it
            }
        }
    }

    fun deleteItem(menuItemId: String) {
        _items.update { current ->
            current.filter { it.menuItem.id != menuItemId }
        }
    }

    fun getQuantity(menuItemId: String): Int =
        _items.value.find { it.menuItem.id == menuItemId }?.quantity ?: 0

    fun clear() { _items.value = emptyList() }
}

// ══════════════════════════════════════════════════════════════
//  ViewModel
// ══════════════════════════════════════════════════════════════

@HiltViewModel
class CartViewModel @Inject constructor(
    val cartStore: CartStore,
    private val ordersStore: com.vkusnyvybor.data.repository.OrdersStore
) : ViewModel() {

    val items = cartStore.items

    fun addOne(item: MenuItem) = cartStore.addItem(item)
    fun removeOne(id: String) = cartStore.removeItem(id)
    fun deleteItem(id: String) = cartStore.deleteItem(id)
    fun clearCart() = cartStore.clear()
    fun totalPrice() = cartStore.totalPrice
    fun totalCount() = cartStore.totalCount

    fun placeOrder() {
        val currentItems = cartStore.items.value
        if (currentItems.isNotEmpty()) {
            ordersStore.placeOrder(currentItems)
            cartStore.clear()
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  CartScreen
// ══════════════════════════════════════════════════════════════

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel()
) {
    val cartItems by viewModel.items.collectAsStateWithLifecycle()
    val isEmpty = cartItems.isEmpty()

    var showClearDialog by remember { mutableStateOf(false) }
    var showCheckoutSuccess by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Filled.DeleteSweep, null) },
            title = { Text("Очистить корзину?") },
            text = { Text("Все добавленные товары будут удалены") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCart()
                    showClearDialog = false
                }) { Text("Очистить") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showCheckoutSuccess) {
        AlertDialog(
            onDismissRequest = {
                showCheckoutSuccess = false
            },
            icon = {
                Icon(
                    Icons.Filled.CheckCircle, null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Заказ оформлен!") },
            text = { Text("Ваш заказ принят и готовится") },
            confirmButton = {
                TextButton(onClick = {
                    showCheckoutSuccess = false
                }) { Text("Отлично") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Заголовок ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Корзина", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                if (!isEmpty) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            Icons.Filled.DeleteOutline, "Очистить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ── Контент ───────────────────────────────────────
            AnimatedContent(
                targetState = isEmpty,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "cart_content",
                modifier = Modifier.weight(1f)
            ) { isCartEmpty ->
                if (isCartEmpty) {
                    EmptyCartState()
                } else {
                    CartItemsList(
                        items = cartItems,
                        onAddOne = { viewModel.addOne(it.menuItem) },
                        onRemoveOne = { viewModel.removeOne(it.menuItem.id) },
                        onDelete = { viewModel.deleteItem(it.menuItem.id) }
                    )
                }
            }
        }

        // ── Нижняя панель ────────────────
        if (!isEmpty) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.8f), MaterialTheme.colorScheme.background)
                        )
                    )
            ) {
                CartBottomBar(
                    totalCount = viewModel.totalCount(),
                    totalPrice = viewModel.totalPrice(),
                    onCheckout = {
                        viewModel.placeOrder()
                        showCheckoutSuccess = true
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyCartState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.ShoppingCart, "Корзина пуста",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Корзина пуста",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Добавьте блюда из меню ресторанов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CartItemsList(
    items: List<CartItem>,
    onAddOne: (CartItem) -> Unit,
    onRemoveOne: (CartItem) -> Unit,
    onDelete: (CartItem) -> Unit
) {
    val grouped = items.groupBy { it.menuItem.restaurantId }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 220.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (restaurantId, restaurantItems) ->
            val restaurantName = when (restaurantId) {
                "vkusno" -> "Вкусно и точка"
                "bk" -> "Бургер Кинг"
                "rostics" -> "Rostics"
                else -> restaurantId
            }
            
            item(key = "header_$restaurantId") {
                RestaurantCartHeader(
                    name = restaurantName,
                    itemCount = restaurantItems.sumOf { it.quantity },
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            items(
                items = restaurantItems,
                key = { it.menuItem.id }
            ) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onAddOne = { onAddOne(cartItem) },
                    onRemoveOne = { onRemoveOne(cartItem) },
                    onDelete = { onDelete(cartItem) }
                )
            }
        }
    }
}

@Composable
private fun RestaurantCartHeader(
    name: String,
    itemCount: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = accentColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "$itemCount шт",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onAddOne: () -> Unit,
    onRemoveOne: () -> Unit,
    onDelete: () -> Unit
) {
    val item = cartItem.menuItem
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(primaryColor.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForCategory(item.category), style = MaterialTheme.typography.headlineSmall)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.weight, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("${cartItem.totalPrice}\u20BD", style = MaterialTheme.typography.titleSmall, color = primaryColor, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledIconButton(
                    onClick = onRemoveOne,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor.copy(0.1f), contentColor = primaryColor)
                ) { Icon(Icons.Filled.Remove, null, Modifier.size(16.dp)) }
                
                Text("${cartItem.quantity}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                
                FilledIconButton(
                    onClick = onAddOne,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor, contentColor = Color.White)
                ) { Icon(Icons.Filled.Add, null, Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun CartBottomBar(
    totalCount: Int,
    totalPrice: Int,
    onCheckout: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Товары ($totalCount)", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${totalPrice}\u20BD", style = MaterialTheme.typography.bodyLarge)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Обслуживание", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Бесплатно", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Итого", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${totalPrice}\u20BD", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Payment, null)
                Spacer(Modifier.width(12.dp))
                Text("Оформить заказ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
