package com.vkusnyvybor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.OrderStatus
import com.vkusnyvybor.data.repository.OrdersStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    ordersStore: OrdersStore,
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val orders by ordersStore.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заказы") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        null,
                        Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "У вас пока нет заказов",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderCard(order = order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        order.restaurantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(order.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${order.date} • ${order.items.sumOf { it.quantity }} тов.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${order.totalPrice} ₽",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun StatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.COMPLETED -> Color(0xFF4CAF50)
        OrderStatus.DELIVERING -> Color(0xFF2196F3)
        OrderStatus.PREPARING -> Color(0xFFFF9800)
        OrderStatus.CANCELLED -> Color(0xFFF44336)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = when (status) {
                OrderStatus.COMPLETED -> "Выполнен"
                OrderStatus.DELIVERING -> "Доставка"
                OrderStatus.PREPARING -> "Готовится"
                OrderStatus.CANCELLED -> "Отменён"
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
