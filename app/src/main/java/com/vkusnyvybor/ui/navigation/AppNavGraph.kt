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
import com.vkusnyvybor.ui.screens.favorites.FavoritesScreen
import com.vkusnyvybor.ui.screens.home.HomeScreen
import com.vkusnyvybor.ui.screens.map.MapScreen
import com.vkusnyvybor.ui.screens.profile.ProfileScreen
import com.vkusnyvybor.ui.screens.restaurant.RestaurantScreen

import com.vkusnyvybor.ui.screens.menuitem.MenuItemDetailScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        // ── Bottom nav screens (crossfade) ──

        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            HomeScreen(
                onRestaurantClick = { restaurantId ->
                    navController.navigate(Screen.Restaurant.createRoute(restaurantId))
                }
            )
        }

        composable(
            route = Screen.Map.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            MapScreen()
        }

        composable(
            route = Screen.Cart.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            CartScreen()
        }

        composable(
            route = Screen.Favorites.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            FavoritesScreen(
                onRestaurantClick = { restaurantId ->
                    navController.navigate(Screen.Restaurant.createRoute(restaurantId))
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
        ) {
            ProfileScreen()
        }

        // ── Detail screens (slide) ──

        composable(
            route = Screen.Restaurant.route,
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(tween(300))
            },
            exitTransition = { fadeOut(tween(200)) },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(350)
                ) + fadeOut(tween(250))
            }
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
            RestaurantScreen(
                restaurantId = restaurantId,
                onBackClick = { navController.popBackStack() },
                onItemClick = { restId, itemId ->
                    navController.navigate(Screen.MenuItem.createRoute(restId, itemId))
                }
            )
        }

        // ── Menu Item Detail ──

        composable(
            route = Screen.MenuItem.route,
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(350)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(350)
                ) + fadeOut(tween(250))
            }
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: return@composable
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            MenuItemDetailScreen(
                restaurantId = restaurantId,
                itemId = itemId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
