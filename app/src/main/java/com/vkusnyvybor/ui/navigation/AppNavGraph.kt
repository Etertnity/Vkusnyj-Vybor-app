package com.vkusnyvybor.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vkusnyvybor.ui.screens.cart.CartScreen
import com.vkusnyvybor.ui.screens.home.HomeScreen
import com.vkusnyvybor.ui.screens.menuitem.MenuItemDetailScreen
import com.vkusnyvybor.ui.screens.order.OrderDetailScreen
import com.vkusnyvybor.ui.screens.profile.ProfileScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            HomeScreen(
                onItemClick = { restId, itemId ->
                    navController.navigate(Screen.MenuItem.createRoute(restId, itemId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(route = Screen.Cart.route) {
            CartScreen(onBackClick = { navController.popBackStack() })
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onThemeClick = { /* TODO: навигация к экрану выбора темы */ }
            )
        }

        composable(
            route = Screen.MenuItem.route,
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            MenuItemDetailScreen(
                restaurantId = restaurantId,
                itemId = itemId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                onBackClick = { navController.popBackStack() },
                onCartClick = {
                    navController.popBackStack()
                    navController.navigate(Screen.Cart.route)
                }
            )
        }
    }
}
