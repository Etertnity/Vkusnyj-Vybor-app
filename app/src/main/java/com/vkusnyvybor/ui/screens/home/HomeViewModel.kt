package com.vkusnyvybor.ui.screens.home

import androidx.lifecycle.ViewModel
import com.vkusnyvybor.data.model.Order
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.repository.MockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val recentOrders: List<Order> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = HomeUiState(
            restaurants = repository.getRestaurants(),
            recentOrders = repository.getRecentOrders(),
            isLoading = false
        )
    }
}
