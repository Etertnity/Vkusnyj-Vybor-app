package com.vkusnyvybor.data.repository

import com.vkusnyvybor.data.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesStore @Inject constructor() {
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _favoriteItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val favoriteItems: StateFlow<List<MenuItem>> = _favoriteItems.asStateFlow()

    fun toggle(item: MenuItem) {
        val id = item.id
        _favoriteIds.update { current ->
            if (id in current) current - id else current + id
        }
        _favoriteItems.update { current ->
            if (current.any { it.id == id }) {
                current.filter { it.id != id }
            } else {
                current + item
            }
        }
    }

    fun isFavorite(id: String): Boolean = id in _favoriteIds.value
}
