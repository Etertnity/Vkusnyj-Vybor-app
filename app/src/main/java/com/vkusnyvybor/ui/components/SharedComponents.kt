package com.vkusnyvybor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.RestaurantColors
import com.vkusnyvybor.ui.theme.ShimmerBase
import com.vkusnyvybor.ui.theme.ShimmerHighlight

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: RoundedCornerShape = RoundedCornerShape(12.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateAnim - 200f, 0f), end = Offset(translateAnim, 0f)
    )
    Box(modifier = modifier.clip(shape).background(brush))
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    usePrimaryForButtons: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val buttonColor = if (usePrimaryForButtons) primaryColor else accentColor
    
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        if (quantity > 0) {
            FilledIconButton(
                onClick = onDecrease, modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = buttonColor.copy(alpha = 0.1f), 
                    contentColor = buttonColor
                )
            ) { Icon(Icons.Filled.Remove, null, Modifier.size(16.dp)) }
            Text(text = "$quantity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.widthIn(min = 24.dp))
        }
        
        FilledIconButton(
            onClick = onIncrease, modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = buttonColor, 
                contentColor = Color.White
            )
        ) { Icon(Icons.Filled.Add, null, Modifier.size(16.dp)) }
    }
}

@Composable
fun AnimatedFavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    var bouncing by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (bouncing) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { bouncing = false }, label = "bounce"
    )
    IconButton(onClick = { bouncing = true; onToggle() }, modifier = modifier.scale(bounceScale)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null, tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    item: MenuItem,
    restaurantColors: RestaurantColors? = null,
    quantity: Int = 0,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    useThemePrimary: Boolean = false
) {
    val accentColor = restaurantColors?.primary ?: MaterialTheme.colorScheme.primary
    
    Card(
        onClick = onClick, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(accentColor.copy(0.08f)), contentAlignment = Alignment.Center) {
                Text(text = getCategoryEmoji(item.category), style = MaterialTheme.typography.headlineMedium)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    AnimatedFavoriteButton(isFavorite = item.isFavorite, onToggle = onFavoriteToggle, modifier = Modifier.size(36.dp))
                }
                if (item.description.isNotEmpty()) Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${item.price}₽", style = MaterialTheme.typography.labelLarge, color = accentColor, fontWeight = FontWeight.Bold)
                    QuantitySelector(
                        quantity = quantity, 
                        onIncrease = onAddToCart, 
                        onDecrease = onRemoveFromCart, 
                        accentColor = accentColor,
                        usePrimaryForButtons = useThemePrimary
                    )
                }
            }
        }
    }
}

private fun getCategoryEmoji(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF54"
}
