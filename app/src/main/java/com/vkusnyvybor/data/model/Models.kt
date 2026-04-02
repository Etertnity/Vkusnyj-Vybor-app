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
    val logoUrl: String = "",
    val heroImageUrl: String = "",
    val rating: Float,
    val deliveryTime: String,
    val deliveryPrice: String = "",
    val colors: RestaurantColors,
    val categories: List<MenuCategory> = emptyList()
)

data class MenuCategory(
    val id: String,
    val name: String,
    val items: List<MenuItem> = emptyList()
)

/**
 * Размер порции (Маленький, Средний, Большой).
 */
data class SizeOption(
    val id: String,
    val name: String,       // "Маленький", "Средний", "Большой"
    val priceAdd: Int = 0   // доплата к базовой цене
)

/**
 * Ингредиент/модификатор который можно убрать или добавить.
 */
data class ItemModifier(
    val id: String,
    val name: String,       // "Салат", "Соус", "Лук", "Сыр"
    val included: Boolean = true,  // входит по умолчанию
    val priceAdd: Int = 0   // доплата если добавляется дополнительно
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Int,
    val oldPrice: Int? = null,
    val imageUrl: String = "",
    val category: String,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val isAvailable: Boolean = true,
    val weight: String = "",
    val sizes: List<SizeOption> = emptyList(),
    val modifiers: List<ItemModifier> = emptyList()
)

/**
 * Конфигурация товара в корзине (выбранный размер + модификаторы).
 */
data class CartItemConfig(
    val selectedSizeId: String? = null,
    val removedModifiers: Set<String> = emptySet(), // IDs убранных ингредиентов (которые были включены)
    val addedModifiers: Set<String> = emptySet()    // IDs добавленных топпингов (которых не было)
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int = 1,
    val config: CartItemConfig = CartItemConfig()
) {
    val sizeAdd: Int get() = menuItem.sizes.find { it.id == config.selectedSizeId }?.priceAdd ?: 0
    
    val modifiersAdd: Int get() = menuItem.modifiers
        .filter { it.id in config.addedModifiers }
        .sumOf { it.priceAdd }

    val totalPrice: Int get() = (menuItem.price + sizeAdd + modifiersAdd) * quantity
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
