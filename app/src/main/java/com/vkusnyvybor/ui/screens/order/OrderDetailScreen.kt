package com.vkusnyvybor.ui.screens.order

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.model.CartItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ordersStore: com.vkusnyvybor.data.repository.OrdersStore,
    private val cartStore: CartStore
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: ""
    val order: Order? = ordersStore.getOrderById(orderId)

    fun repeatOrder() {
        order?.items?.forEach { cartItem ->
            repeat(cartItem.quantity) {
                cartStore.addItem(cartItem.menuItem)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val order = viewModel.order
    var showRepeatSuccess by remember { mutableStateOf(false) }

    if (order == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Заказ не найден")
        }
        return
    }

    // Вместо жестких цветов ресторанов используем цвета ТЕМЫ
    // Если нужно выделить ресторан, используем primary текущей темы
    val themeAccent = MaterialTheme.colorScheme.primary
    val themeSurface = MaterialTheme.colorScheme.surfaceVariant

    if (showRepeatSuccess) {
        AlertDialog(
            onDismissRequest = { showRepeatSuccess = false },
            icon = {
                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
            },
            title = { Text("Товары добавлены!") },
            text = { Text("${order.items.sumOf { it.quantity }} позиций добавлено в корзину") },
            confirmButton = {
                TextButton(onClick = {
                    showRepeatSuccess = false
                    onCartClick()
                }) { Text("Перейти в корзину") }
            },
            dismissButton = {
                TextButton(onClick = { showRepeatSuccess = false }) { Text("Остаться") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Заказ от ${order.date}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp, 
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Итого", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${order.totalPrice} ₽",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = themeAccent
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.repeatOrder()
                            showRepeatSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = themeAccent)
                    ) {
                        Icon(Icons.Filled.Replay, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Повторить заказ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заголовок ресторана в стиле текущей темы
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Store, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                        Text(
                            order.restaurantName, 
                            style = MaterialTheme.typography.titleSmall, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.weight(1f))
                        StatusBadge(order.status)
                    }
                }
            }

            item {
                Text(
                    "${order.items.sumOf { it.quantity }} позиции в заказе",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(order.items, key = { it.menuItem.id + it.quantity }) { cartItem ->
                OrderItemCard(cartItem = cartItem)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: com.vkusnyvybor.data.model.OrderStatus) {
    val color = when (status) {
        com.vkusnyvybor.data.model.OrderStatus.COMPLETED -> Color(0xFF4CAF50)
        com.vkusnyvybor.data.model.OrderStatus.PREPARING -> Color(0xFFFFC107)
        com.vkusnyvybor.data.model.OrderStatus.DELIVERING -> Color(0xFF2196F3)
        com.vkusnyvybor.data.model.OrderStatus.CANCELLED -> Color(0xFFE53935)
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = when (status) {
                com.vkusnyvybor.data.model.OrderStatus.COMPLETED -> "Выполнен"
                com.vkusnyvybor.data.model.OrderStatus.PREPARING -> "Готовится"
                com.vkusnyvybor.data.model.OrderStatus.DELIVERING -> "В пути"
                com.vkusnyvybor.data.model.OrderStatus.CANCELLED -> "Отменён"
            },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun OrderItemCard(cartItem: CartItem) {
    val item = cartItem.menuItem

    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
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
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmoji(item.category), style = MaterialTheme.typography.headlineSmall)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.name, 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Medium, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis
                )
                if (item.weight.isNotEmpty()) {
                    Text(
                        item.weight, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${cartItem.totalPrice} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (cartItem.quantity > 1) {
                    Text(
                        "${cartItem.quantity} × ${item.price} ₽",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun getEmoji(category: String): String = when (category) {
    "Бургеры" -> "🍔"
    "Гарниры" -> "🍟"
    "Снэки" -> "🍗"
    "Напитки" -> "🥤"
    "Десерты" -> "🍰"
    "Роллы", "Твистеры" -> "🌯"
    "Курица", "Корзинки" -> "🍗"
    else -> "🍴"
}
