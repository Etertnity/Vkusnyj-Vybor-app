package com.vkusnyvybor.ui.screens.menuitem

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.Restaurant
import com.vkusnyvybor.data.repository.FavoritesStore
import com.vkusnyvybor.data.repository.MockRepository
import com.vkusnyvybor.ui.screens.cart.CartStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MenuItemDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MockRepository,
    val cartStore: CartStore,
    private val favoritesStore: FavoritesStore
) : ViewModel() {

    private val restaurantId: String = savedStateHandle["restaurantId"] ?: ""
    private val itemId: String = savedStateHandle["itemId"] ?: ""

    private val _menuItem = MutableStateFlow<MenuItem?>(null)
    val menuItem: StateFlow<MenuItem?> = _menuItem.asStateFlow()

    private val _restaurant = MutableStateFlow<Restaurant?>(null)
    val restaurant: StateFlow<Restaurant?> = _restaurant.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    init {
        val rest = repository.getRestaurantById(restaurantId)
        _restaurant.value = rest
        _menuItem.value = rest?.categories
            ?.flatMap { it.items }
            ?.find { it.id == itemId }
        
        _isFavorite.value = favoritesStore.isFavorite(itemId)
    }

    fun toggleFavorite() {
        _menuItem.value?.let { 
            favoritesStore.toggle(it)
            _isFavorite.value = favoritesStore.isFavorite(itemId)
        }
    }

    fun addToCart() {
        _menuItem.value?.let { cartStore.addItem(it) }
    }

    fun removeFromCart() {
        _menuItem.value?.let { cartStore.removeItem(it.id) }
    }

    fun getQuantity(): Int =
        _menuItem.value?.let { cartStore.getQuantity(it.id) } ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDetailScreen(
    restaurantId: String,
    itemId: String,
    onBackClick: () -> Unit,
    viewModel: MenuItemDetailViewModel = hiltViewModel()
) {
    val menuItem by viewModel.menuItem.collectAsStateWithLifecycle()
    val restaurant by viewModel.restaurant.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    if (menuItem == null || restaurant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val item = menuItem!!
    val rest = restaurant!!
    val colors = rest.colors
    val quantity = viewModel.getQuantity()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Основной скроллируемый контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero секция с градиентом от основного цвета ресторана
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.primary.copy(alpha = 0.25f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
                    label = "scale"
                )
                
                Text(
                    getDetailEmoji(item.category),
                    fontSize = 160.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${item.price}\u20BD", style = MaterialTheme.typography.headlineSmall, color = colors.primary, fontWeight = FontWeight.Bold)
                        if (item.oldPrice != null) {
                            Text("${item.oldPrice}\u20BD", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline, textDecoration = TextDecoration.LineThrough)
                        }
                    }
                }

                ListItem(
                    headlineContent = { Text(rest.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Рейтинг блюда: ${item.rating}") },
                    leadingContent = { 
                        Surface(Modifier.size(44.dp), shape = CircleShape, color = colors.primary.copy(0.1f)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Store, null, tint = colors.primary, modifier = Modifier.size(22.dp))
                            }
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, null, Modifier.size(20.dp), tint = Color(0xFFFFB300))
                            Text("${item.rating}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f), RoundedCornerShape(16.dp))
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Описание", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Пищевая ценность", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Белки", "Жиры", "Углеводы", "Ккал").forEach { label ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                            ) {
                                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("--", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(140.dp)) // Отступ для плавающей кнопки
            }
        }

        // Плавающие кнопки управления сверху (ФИКС: Используем WindowInsets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(0.8f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
            IconButton(
                onClick = { viewModel.toggleFavorite() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(0.8f), CircleShape)
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    null,
                    tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Плавающая кнопка корзины (ФИКС: Используем WindowInsets)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Button(
                onClick = { viewModel.addToCart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
            ) {
                Icon(Icons.Filled.ShoppingCart, null, Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                val priceText = if (quantity > 0) "${quantity} шт \u2022 ${item.price * quantity}\u20BD" else "В корзину \u2022 ${item.price}\u20BD"
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getDetailEmoji(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
