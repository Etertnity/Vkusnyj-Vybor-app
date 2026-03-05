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

    val accentColor = when (order.restaurantId) {
        "vkusno" -> Color(0xFF1B5E20)
        "bk" -> Color(0xFFEC1C24)
        "rostics" -> Color(0xFFD32F2F)
        else -> Color(0xFF6750A4)
    }

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
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
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
                            "${order.totalPrice}\u20BD",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
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
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
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
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок ресторана
            item {
                Surface(
                    color = accentColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Store, "Ресторан", tint = accentColor, modifier = Modifier.size(20.dp))
                        Text(order.restaurantName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = accentColor)
                        Spacer(Modifier.weight(1f))
                        Surface(
                            color = when (order.status) {
                                com.vkusnyvybor.data.model.OrderStatus.COMPLETED -> Color(0xFF4CAF50)
                                com.vkusnyvybor.data.model.OrderStatus.PREPARING -> Color(0xFFFFC107)
                                com.vkusnyvybor.data.model.OrderStatus.DELIVERING -> Color(0xFF2196F3)
                                com.vkusnyvybor.data.model.OrderStatus.CANCELLED -> Color(0xFFE53935)
                            }.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                when (order.status) {
                                    com.vkusnyvybor.data.model.OrderStatus.COMPLETED -> "Выполнен"
                                    com.vkusnyvybor.data.model.OrderStatus.PREPARING -> "Готовится"
                                    com.vkusnyvybor.data.model.OrderStatus.DELIVERING -> "В пути"
                                    com.vkusnyvybor.data.model.OrderStatus.CANCELLED -> "Отменён"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "${order.items.sumOf { it.quantity }} позиций",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Товары заказа
            items(order.items, key = { it.menuItem.id }) { cartItem ->
                OrderItemCard(cartItem = cartItem, accentColor = accentColor)
            }
        }
    }
}

@Composable
private fun OrderItemCard(cartItem: CartItem, accentColor: Color) {
    val item = cartItem.menuItem

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accentColor.copy(alpha = 0.08f), accentColor.copy(alpha = 0.15f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmoji(item.category), style = MaterialTheme.typography.headlineSmall)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.weight.isNotEmpty()) {
                    Text(item.weight, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${cartItem.totalPrice}\u20BD",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                if (cartItem.quantity > 1) {
                    Text(
                        "${cartItem.quantity} \u00D7 ${item.price}\u20BD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun getEmoji(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
