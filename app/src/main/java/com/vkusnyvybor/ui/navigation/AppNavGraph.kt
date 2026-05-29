package com.vkusnyvybor.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vkusnyvybor.data.repository.OrdersStore
import com.vkusnyvybor.ui.screens.auth.AuthScreen
import com.vkusnyvybor.ui.screens.cart.CartScreen
import com.vkusnyvybor.ui.screens.home.HomeScreen
import com.vkusnyvybor.ui.screens.map.MapPickerScreen
import com.vkusnyvybor.ui.screens.menuitem.MenuItemDetailScreen
import com.vkusnyvybor.ui.screens.order.OrderDetailScreen
import com.vkusnyvybor.ui.screens.profile.OrderHistoryScreen
import com.vkusnyvybor.ui.screens.profile.ProfileScreen
import com.vkusnyvybor.ui.screens.profile.ThemeConstructorScreen
import com.vkusnyvybor.ui.screens.profile.ThemePickerScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    ordersStore: OrdersStore,
    startDestination: String = Screen.Auth.route
) {
    val d = 300

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(d)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(d)) },
        exitTransition = { fadeOut(tween(d)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(d)) },
        popEnterTransition = { fadeIn(tween(d)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(d)) },
        popExitTransition = { fadeOut(tween(d)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(d)) }
    ) {
        composable(
            Screen.Auth.route,
            enterTransition = { fadeIn(tween(d)) },
            exitTransition = { fadeOut(tween(d)) }
        ) {
            AuthScreen(
                onAuthorized = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route, enterTransition = { fadeIn(tween(d)) }, exitTransition = { fadeOut(tween(d)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(d)) }) {
            HomeScreen(
                onItemClick = { r, i -> navController.navigate(Screen.MenuItem.createRoute(r, i)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onOrderClick = { navController.navigate(Screen.OrderDetail.createRoute(it)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onSelectLocationClick = { navController.navigate(Screen.Map.route) }
            )
        }

        composable(Screen.Map.route) {
            MapPickerScreen(onClose = { navController.popBackStack() })
        }

        composable(Screen.Cart.route) { CartScreen(onBackClick = { navController.popBackStack() }) }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onThemeClick = { navController.navigate(Screen.ThemePicker.route) },
                onOrdersClick = { navController.navigate(Screen.OrderHistory.route) },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                ordersStore = ordersStore,
                onBackClick = { navController.popBackStack() },
                onOrderClick = { navController.navigate(Screen.OrderDetail.createRoute(it)) }
            )
        }

        composable(Screen.ThemePicker.route) {
            ThemePickerScreen(
                onBackClick = { navController.popBackStack() },
                onConstructorClick = { navController.navigate(Screen.ThemeConstructor.createRoute()) },
                onEditTheme = { themeId -> navController.navigate(Screen.ThemeConstructor.createRoute(themeId)) }
            )
        }

        composable(
            Screen.ThemeConstructor.route,
            arguments = listOf(navArgument("editId") { type = NavType.StringType; defaultValue = "" })
        ) { entry ->
            val editId = entry.arguments?.getString("editId")?.takeIf { it.isNotEmpty() }
            ThemeConstructorScreen(editThemeId = editId, onBackClick = { navController.popBackStack() })
        }

        composable(
            Screen.MenuItem.route,
            arguments = listOf(navArgument("restaurantId") { type = NavType.StringType }, navArgument("itemId") { type = NavType.StringType })
        ) { entry ->
            MenuItemDetailScreen(
                restaurantId = entry.arguments?.getString("restaurantId") ?: return@composable,
                itemId = entry.arguments?.getString("itemId") ?: return@composable,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { entry ->
            OrderDetailScreen(
                orderId = entry.arguments?.getString("orderId") ?: return@composable,
                onBackClick = { navController.popBackStack() },
                onCartClick = { navController.popBackStack(Screen.Home.route, false); navController.navigate(Screen.Cart.route) }
            )
        }
    }
}
