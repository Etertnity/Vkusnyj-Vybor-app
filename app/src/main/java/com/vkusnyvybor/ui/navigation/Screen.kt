package com.vkusnyvybor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Все маршруты приложения.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Map : Screen("map")
    data object Cart : Screen("cart")
    data object Favorites : Screen("favorites")
    data object Profile : Screen("profile")
    data object Restaurant : Screen("restaurant/{restaurantId}") {
        fun createRoute(restaurantId: String) = "restaurant/$restaurantId"
    }
    data object MenuItem : Screen("menu_item/{itemId}") {
        fun createRoute(itemId: String) = "menu_item/$itemId"
    }
}

/**
 * Элементы нижней навигации.
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    RESTAURANTS(
        Screen.Home.route,
        "Рестораны",
        Icons.Filled.Restaurant,
        Icons.Outlined.Restaurant
    ),
    MAP(
        Screen.Map.route,
        "Карта",
        Icons.Filled.LocationOn,
        Icons.Outlined.LocationOn
    ),
    CART(
        Screen.Cart.route,
        "Корзина",
        Icons.Filled.ShoppingCart,
        Icons.Outlined.ShoppingCart
    ),
    FAVORITES(
        Screen.Favorites.route,
        "Избранные",
        Icons.Filled.Favorite,
        Icons.Outlined.FavoriteBorder
    ),
    PROFILE(
        Screen.Profile.route,
        "Профиль",
        Icons.Filled.Person,
        Icons.Outlined.Person
    )
}
