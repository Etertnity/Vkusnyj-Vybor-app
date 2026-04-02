package com.vkusnyvybor.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vkusnyvybor.data.model.MenuCategory
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.data.repository.MockRepository
import com.vkusnyvybor.data.repository.OrdersStore
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedRestaurantIndex: Int = 0,
    val selectedRestaurant: Restaurant? = null,
    val menuCategories: List<MenuCategory> = emptyList(),
    val allMenuItems: List<MenuItem> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<MenuItem> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MockRepository,
    val cartStore: CartStore,
    val favoritesStore: FavoritesStore,
    private val ordersStore: OrdersStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        ordersStore.seedIfEmpty(repository.getRecentOrders())
        loadData()
        observeOrders()
    }

    private fun loadData() {
        val restaurants = repository.getRestaurants()
        val first = restaurants.firstOrNull()

        _uiState.value = HomeUiState(
            restaurants = restaurants,
            selectedRestaurantIndex = 0,
            selectedRestaurant = first,
            menuCategories = first?.categories ?: emptyList(),
            allMenuItems = repository.getAllMenuItems(),
            recentOrders = ordersStore.orders.value,
            isLoading = false
        )
    }

    private fun observeOrders() {
        viewModelScope.launch {
            ordersStore.orders.collect { orders ->
                _uiState.value = _uiState.value.copy(recentOrders = orders)
            }
        }
    }

    /**
     * Вызывается при свайпе карусели — подгружает меню выбранного ресторана.
     */
    fun onRestaurantChanged(index: Int) {
        val current = _uiState.value
        if (index == current.selectedRestaurantIndex) return
        val restaurant = current.restaurants.getOrNull(index) ?: return

        _uiState.value = current.copy(
            selectedRestaurantIndex = index,
            selectedRestaurant = restaurant,
            menuCategories = restaurant.categories
        )
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value
        if (query.isBlank()) {
            _uiState.value = current.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
            return
        }
        val q = query.lowercase()
        val results = current.allMenuItems.filter {
            it.name.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
        _uiState.value = current.copy(
            searchQuery = query,
            searchResults = results,
            isSearching = true
        )
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
    }

    fun addToCart(item: MenuItem) = cartStore.addItem(item)
    fun removeFromCart(itemId: String) = cartStore.removeItem(itemId)
    fun toggleFavorite(item: MenuItem) = favoritesStore.toggle(item)
}
