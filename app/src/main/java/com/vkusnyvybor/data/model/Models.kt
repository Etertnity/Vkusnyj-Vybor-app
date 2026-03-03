package com.vkusnyvybor.data.model

import androidx.compose.ui.graphics.Color

data class RestaurantColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val onPrimary: Color = Color.White,
    val gradientStart: Color = primary,
    val gradientEnd: Color = secondary
)

data class Restaurant(
    val id: String,
    val name: String,
    val subtitle: String,
    val slogan: String,
    val logoUrl: String,
    val heroImageUrl: String,
    val rating: Float,
    val deliveryTime: String,
    val deliveryPrice: String,
    val colors: RestaurantColors,
    val categories: List<MenuCategory> = emptyList()
)

data class MenuCategory(
    val id: String,
    val name: String,
    val items: List<MenuItem> = emptyList()
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Int,
    val oldPrice: Int? = null,
    val imageUrl: String,
    val category: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val isAvailable: Boolean = true,
    val weight: String = ""
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1
) {
    val totalPrice: Int get() = menuItem.price * quantity
}

data class Order(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val items: List<CartItem>,
    val totalPrice: Int,
    val date: String,
    val status: OrderStatus
)

enum class OrderStatus {
    PREPARING, DELIVERING, COMPLETED, CANCELLED
}

enum class RestaurantTab(val title: String) {
    MAIN_MENU("Основное меню"),
    PROMOTIONS("Акции")
}
