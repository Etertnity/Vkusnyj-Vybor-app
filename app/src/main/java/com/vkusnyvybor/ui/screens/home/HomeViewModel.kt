package com.vkusnyvybor.ui.screens.home

import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.repository.MockRepository
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val allMenuItems: List<MenuItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val searchResults: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MockRepository,
    val cartStore: CartStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val allItems = repository.getAllMenuItems()
        val categories = allItems.map { it.category }.distinct().sorted()

        _uiState.value = HomeUiState(
            restaurants = repository.getRestaurants(),
            recentOrders = repository.getRecentOrders(),
            allMenuItems = allItems,
            categories = categories,
            isLoading = false
        )
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value
        val isSearching = query.isNotBlank() || current.selectedCategory != null

        val results = filterItems(
            items = current.allMenuItems,
            query = query,
            category = current.selectedCategory
        )

        _uiState.value = current.copy(
            searchQuery = query,
            searchResults = results,
            isSearching = isSearching
        )
    }

    fun onCategorySelected(category: String?) {
        val current = _uiState.value
        val newCategory = if (current.selectedCategory == category) null else category
        val isSearching = current.searchQuery.isNotBlank() || newCategory != null

        val results = filterItems(
            items = current.allMenuItems,
            query = current.searchQuery,
            category = newCategory
        )

        _uiState.value = current.copy(
            selectedCategory = newCategory,
            searchResults = results,
            isSearching = isSearching
        )
    }

    fun clearSearch() {
        val current = _uiState.value
        _uiState.value = current.copy(
            searchQuery = "",
            selectedCategory = null,
            searchResults = emptyList(),
            isSearching = false
        )
    }

    fun addToCart(item: MenuItem) {
        cartStore.addItem(item)
    }

    private fun filterItems(
        items: List<MenuItem>,
        query: String,
        category: String?
    ): List<MenuItem> {
        var filtered = items

        if (query.isNotBlank()) {
            val q = query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.lowercase().contains(q)
            }
        }

        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }

        return filtered
    }
}
