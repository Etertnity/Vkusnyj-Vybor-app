package com.vkusnyvybor.data.repository

import com.vkusnyvybor.data.model.CartItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdersStore @Inject constructor() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun seedIfEmpty(initialOrders: List<Order>) {
        if (_orders.value.isEmpty()) {
            _orders.value = initialOrders
        }
    }

    fun placeOrder(items: List<CartItem>): Order {
        val grouped = items.groupBy { it.menuItem.restaurantId }
        val restaurantId = grouped.keys.firstOrNull() ?: "unknown"
        val restaurantName = when (restaurantId) {
            "vkusno" -> "Вкусно и точка"
            "bk" -> "Бургер Кинг"
            "rostics" -> "Rostics"
            else -> restaurantId
        }

        val order = Order(
            id = "o${System.currentTimeMillis()}",
            restaurantId = restaurantId,
            restaurantName = if (grouped.size > 1) "Сборный заказ" else restaurantName,
            items = items,
            totalPrice = items.sumOf { it.totalPrice },
            date = "Сегодня",
            status = OrderStatus.COMPLETED
        )

        _orders.update { current -> listOf(order) + current }
        return order
    }

    fun getOrderById(id: String): Order? =
        _orders.value.find { it.id == id }
}
