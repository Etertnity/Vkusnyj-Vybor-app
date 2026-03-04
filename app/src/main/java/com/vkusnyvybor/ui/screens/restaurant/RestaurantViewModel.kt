package com.vkusnyvybor.ui.screens.restaurant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.model.RestaurantTab
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.data.repository.MockRepository
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RestaurantUiState(
    val restaurant: Restaurant? = null,
    val selectedTab: RestaurantTab = RestaurantTab.MAIN_MENU,
    val isLoading: Boolean = true
)

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MockRepository,
    val cartStore: CartStore,
    val favoritesStore: FavoritesStore
) : ViewModel() {

    private val restaurantId: String = savedStateHandle["restaurantId"] ?: ""

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    val favoriteIds = favoritesStore.favoriteIds

    init {
        loadRestaurant()
    }

    private fun loadRestaurant() {
        val restaurant = repository.getRestaurantById(restaurantId)
        _uiState.value = RestaurantUiState(
            restaurant = restaurant,
            isLoading = false
        )
    }

    fun selectTab(tab: RestaurantTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleFavorite(item: MenuItem) {
        favoritesStore.toggle(item)
    }

    fun isFavorite(itemId: String): Boolean =
        favoritesStore.isFavorite(itemId)

    fun addToCart(item: MenuItem) {
        cartStore.addItem(item)
    }

    fun removeFromCart(itemId: String) {
        cartStore.removeItem(itemId)
    }

    fun getCartQuantity(itemId: String): Int =
        cartStore.getQuantity(itemId)
}
