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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.data.model.RestaurantColors
import com.vkusnyvybor.ui.theme.ShimmerBase
import com.vkusnyvybor.ui.theme.ShimmerHighlight

// ── Shimmer эффект ────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

// ── Градиентный хедер ресторана ───────────────────────────────

@Composable
fun GradientHeader(
    colors: RestaurantColors,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_shift")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.gradientStart,
                        colors.gradientEnd,
                        colors.primary
                    ),
                    start = Offset(offset, 0f),
                    end = Offset(offset + 600f, 800f)
                )
            )
    ) {
        content()
    }
}

// ── Бейдж с ценой ─────────────────────────────────────────────

@Composable
fun PriceBadge(
    price: Int,
    oldPrice: Int? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Surface(
            color = accentColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "${price}₽",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (oldPrice != null) {
            Text(
                text = "${oldPrice}₽",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

// ── Кнопка количества (+/-) ───────────────────────────────────

@Composable
fun QuantitySelector(
    quantity: Int,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (quantity > 0) {
            FilledIconButton(
                onClick = onDecrease,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = accentColor
                )
            ) {
                Icon(Icons.Filled.Remove, "Убрать", Modifier.size(16.dp), tint = Color.White)
            }

            Text(
                text = "$quantity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 24.dp),
            )
        }

        FilledIconButton(
            onClick = onIncrease,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor
            )
        ) {
            Icon(Icons.Filled.Add, "Добавить", Modifier.size(16.dp), tint = Color.White)
        }
    }
}

// ── Кнопка Избранное с анимацией ──────────────────────────────

@Composable
fun AnimatedFavoriteButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.0f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fav_scale"
    )

    // Bounce при переключении
    var bouncing by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (bouncing) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { bouncing = false },
        label = "bounce"
    )

    IconButton(
        onClick = {
            bouncing = true
            onToggle()
        },
        modifier = modifier.scale(bounceScale)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = "Избранное",
            tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Карточка товара ───────────────────────────────────────────

@Composable
fun MenuItemCard(
    item: MenuItem,
    restaurantColors: RestaurantColors,
    quantity: Int = 0,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Заглушка для изображения товара
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                restaurantColors.primary.copy(alpha = 0.1f),
                                restaurantColors.secondary.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Здесь будет Coil AsyncImage когда подключим реальные картинки
                Text(
                    "🍔",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    AnimatedFavoriteButton(
                        isFavorite = item.isFavorite,
                        onToggle = onFavoriteToggle,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.weight.isNotEmpty()) {
                    Text(
                        text = item.weight,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceBadge(
                        price = item.price,
                        oldPrice = item.oldPrice,
                        accentColor = restaurantColors.primary
                    )
                    QuantitySelector(
                        quantity = quantity,
                        accentColor = restaurantColors.primary,
                        onIncrease = onAddToCart,
                        onDecrease = onRemoveFromCart
                    )
                }
            }
        }
    }
}
