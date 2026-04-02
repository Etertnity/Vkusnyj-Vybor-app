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
import com.vkusnyvybor.ui.screens.cart.CartScreen
import com.vkusnyvybor.ui.screens.home.HomeScreen
import com.vkusnyvybor.ui.screens.menuitem.MenuItemDetailScreen
import com.vkusnyvybor.ui.screens.order.OrderDetailScreen
import com.vkusnyvybor.ui.screens.profile.OrderHistoryScreen
import com.vkusnyvybor.ui.screens.profile.ProfileScreen
import com.vkusnyvybor.ui.screens.profile.ThemePickerScreen

@Composable
fun AppNavGraph(navController: NavHostController, ordersStore: OrdersStore) {
    val animationDuration = 300

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(tween(animationDuration)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(animationDuration)
            )
        },
        exitTransition = {
            fadeOut(tween(animationDuration)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(animationDuration)
            )
        },
        popEnterTransition = {
            fadeIn(tween(animationDuration)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(animationDuration)
            )
        },
        popExitTransition = {
            fadeOut(tween(animationDuration)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(animationDuration)
            )
        }
    ) {
        composable(
            Screen.Home.route,
            enterTransition = { fadeIn(tween(animationDuration)) },
            exitTransition = {
                fadeOut(tween(animationDuration)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(animationDuration)
                )
            }
        ) {
            HomeScreen(
                onItemClick = { restId, itemId -> navController.navigate(Screen.MenuItem.createRoute(restId, itemId)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onOrderClick = { orderId -> navController.navigate(Screen.OrderDetail.createRoute(orderId)) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onThemeClick = { navController.navigate(Screen.ThemePicker.route) },
                onOrdersClick = { navController.navigate(Screen.OrderHistory.route) }
            )
        }

        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                ordersStore = ordersStore,
                onBackClick = { navController.popBackStack() },
                onOrderClick = { orderId -> navController.navigate(Screen.OrderDetail.createRoute(orderId)) }
            )
        }

        composable(Screen.ThemePicker.route) {
            ThemePickerScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            Screen.MenuItem.route,
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
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
                onCartClick = { 
                    navController.popBackStack(Screen.Home.route, false)
                    navController.navigate(Screen.Cart.route) 
                }
            )
        }
    }
}
