package com.vkusnyvybor.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
    data object ThemePicker : Screen("theme_picker")
    data object OrderHistory : Screen("order_history")
    data object MenuItem : Screen("menu_item/{restaurantId}/{itemId}") {
        fun createRoute(restaurantId: String, itemId: String) = "menu_item/$restaurantId/$itemId"
    }
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
}
