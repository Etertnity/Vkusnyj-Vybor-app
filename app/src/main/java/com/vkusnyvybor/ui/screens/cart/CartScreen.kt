package com.vkusnyvybor.ui.screens.cart

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.CartItem
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.ui.components.getEmojiForCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartStore @Inject constructor() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()
    val totalPrice: Int get() = _items.value.sumOf { it.totalPrice }
    val totalCount: Int get() = _items.value.sumOf { it.quantity }
    
    fun addItemWithConfig(menuItem: MenuItem, config: com.vkusnyvybor.data.model.CartItemConfig) {
        _items.update { current ->
            val existing = current.find { it.menuItem.id == menuItem.id && it.config == config }
            if (existing != null) {
                current.map { if (it === existing) it.copy(quantity = it.quantity + 1) else it }
            } else {
                current + CartItem(menuItem, 1, config)
            }
        }
    }

    fun addItem(menuItem: MenuItem) = addItemWithConfig(menuItem, com.vkusnyvybor.data.model.CartItemConfig())

    // Совместимость с HomeViewModel и другими: уменьшает кол-во последнего добавленного товара с этим ID
    fun removeItem(menuItemId: String) {
        _items.update { current ->
            val existing = current.findLast { it.menuItem.id == menuItemId }
            if (existing != null) {
                current.mapNotNull { 
                    if (it === existing) {
                        if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
                    } else it 
                }
            } else current
        }
    }

    fun removeCartItem(cartItem: CartItem) {
        _items.update { c -> c.mapNotNull { if (it === cartItem) { if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null } else it } }
    }

    fun deleteItem(cartItem: CartItem) {
        _items.update { c -> c.filter { it !== cartItem } }
    }

    fun clear() { _items.value = emptyList() }
    fun getQuantity(id: String) = _items.value.filter { it.menuItem.id == id }.sumOf { it.quantity }
}

@HiltViewModel
class CartViewModel @Inject constructor(val cartStore: CartStore, private val ordersStore: com.vkusnyvybor.data.repository.OrdersStore) : ViewModel() {
    val items = cartStore.items
    fun addOne(item: CartItem) = cartStore.addItemWithConfig(item.menuItem, item.config)
    fun removeOne(item: CartItem) = cartStore.removeCartItem(item)
    fun deleteItem(item: CartItem) = cartStore.deleteItem(item)
    fun clearCart() = cartStore.clear()
    fun totalPrice() = cartStore.totalPrice
    fun totalCount() = cartStore.totalCount
    fun placeOrder() { val cur = cartStore.items.value; if (cur.isNotEmpty()) { ordersStore.placeOrder(cur); cartStore.clear() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(onBackClick: () -> Unit = {}, viewModel: CartViewModel = hiltViewModel()) {
    val cartItems by viewModel.items.collectAsStateWithLifecycle()
    val isEmpty = cartItems.isEmpty()
    var showClearDialog by remember { mutableStateOf(false) }
    var showCheckoutSuccess by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(onDismissRequest = { showClearDialog = false }, icon = { Icon(Icons.Filled.DeleteSweep, null) }, title = { Text("Очистить корзину?") }, text = { Text("Все товары будут удалены") },
            confirmButton = { TextButton(onClick = { viewModel.clearCart(); showClearDialog = false }) { Text("Очистить") } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Отмена") } })
    }
    if (showCheckoutSuccess) {
        AlertDialog(onDismissRequest = { showCheckoutSuccess = false }, icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) },
            title = { Text("Заказ оформлен!") }, text = { Text("Ваш заказ принят") },
            confirmButton = { TextButton(onClick = { showCheckoutSuccess = false; onBackClick() }) { Text("На главную") } })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Корзина") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                actions = { if (!isEmpty) IconButton(onClick = { showClearDialog = true }) { Icon(Icons.Filled.DeleteOutline, "Очистить", tint = MaterialTheme.colorScheme.error) } }
            )
        },
        bottomBar = {
            AnimatedVisibility(visible = !isEmpty, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                CartBottomBar(viewModel.totalCount(), viewModel.totalPrice()) { viewModel.placeOrder(); showCheckoutSuccess = true }
            }
        }
    ) { padding ->
        AnimatedContent(targetState = isEmpty, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "cart", modifier = Modifier.padding(padding)) { empty ->
            if (empty) EmptyCartState() else CartItemsList(cartItems, { viewModel.addOne(it) }, { viewModel.removeOne(it) }, { viewModel.deleteItem(it) })
        }
    }
}

@Composable
private fun EmptyCartState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.ShoppingCart, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            Text("Корзина пуста", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Добавьте блюда из меню", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun CartItemsList(items: List<CartItem>, onAdd: (CartItem) -> Unit, onRemove: (CartItem) -> Unit, onDelete: (CartItem) -> Unit) {
    val grouped = items.groupBy { it.menuItem.restaurantId }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        grouped.forEach { (rid, ritems) ->
            val name = when (rid) { "vkusno" -> "Вкусно и точка"; "bk" -> "Бургер Кинг"; "rostics" -> "Rostics"; else -> rid }
            item(key = "h_$rid") {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(4.dp, 20.dp).clip(MaterialTheme.shapes.extraSmall).background(MaterialTheme.colorScheme.primary))
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                        Text("${ritems.sumOf { it.quantity }} шт", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
            items(items = ritems) { ci ->
                CartItemCard(ci, { onAdd(ci) }, { onRemove(ci) }, { onDelete(ci) })
            }
        }
    }
}

@Composable
private fun CartItemCard(cartItem: CartItem, onAdd: () -> Unit, onRemove: () -> Unit, onDelete: () -> Unit) {
    val item = cartItem.menuItem
    ElevatedCard(shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text(getEmojiForCategory(item.category), fontSize = 28.sp)
            }
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                // ОТОБРАЖЕНИЕ ИЗМЕНЕНИЙ
                val removed = item.modifiers.filter { it.id in cartItem.config.removedModifiers }.map { it.name }
                val added = item.modifiers.filter { it.id in cartItem.config.addedModifiers }.map { it.name }
                
                if (removed.isNotEmpty()) {
                    Text("Без: ${removed.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                if (added.isNotEmpty()) {
                    Text("Доп: ${added.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }

                if (item.weight.isNotEmpty() && removed.isEmpty() && added.isEmpty()) {
                    Text(item.weight, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                
                Text("${cartItem.totalPrice}\u20BD", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.Close, "Удалить", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledIconButton(onClick = onRemove, Modifier.size(30.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) { Icon(Icons.Filled.Remove, null, Modifier.size(16.dp)) }
                    Text("${cartItem.quantity}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    FilledIconButton(onClick = onAdd, Modifier.size(30.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) { Icon(Icons.Filled.Add, null, Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CartBottomBar(totalCount: Int, totalPrice: Int, onCheckout: () -> Unit) {
    Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Товары ($totalCount)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${totalPrice}\u20BD", style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Обслуживание", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Бесплатно", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Итого", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${totalPrice}\u20BD", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Button(onClick = onCheckout, Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) {
                Icon(Icons.Filled.ShoppingCart, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Оформить заказ \u2022 ${totalPrice}\u20BD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
