package com.vkusnyvybor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vkusnyvybor.data.model.MenuItem
import com.vkusnyvybor.ui.theme.*

// ── Shimmer эффект (Marathon — неоновый) ──────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
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

// ── Кибер-карточка с неоновой рамкой ─────────────────────────

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.6f),
                        glowColor.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

// ── Бейдж с ценой (киберпанк) ────────────────────────────────

@Composable
fun PriceBadge(
    price: Int,
    oldPrice: Int? = null,
    accentColor: Color = NeonCyan,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Surface(
            color = accentColor.copy(alpha = 0.15f),
            shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
        ) {
            Text(
                text = "${price}\u20BD",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (oldPrice != null) {
            Text(
                text = "${oldPrice}\u20BD",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

// ── Кнопка количества (+/-) — киберпанк ──────────────────────

@Composable
fun QuantitySelector(
    quantity: Int,
    accentColor: Color = NeonCyan,
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
                shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                )
            ) {
                Icon(Icons.Filled.Remove, "Убрать", Modifier.size(16.dp))
            }

            Text(
                text = "$quantity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.widthIn(min = 24.dp),
            )
        }

        FilledIconButton(
            onClick = onIncrease,
            modifier = Modifier.size(32.dp),
            shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor,
                contentColor = DarkBg
            )
        ) {
            Icon(Icons.Filled.Add, "Добавить", Modifier.size(16.dp))
        }
    }
}

// ── Карточка товара (Marathon-стиль) ──────────────────────────

@Composable
fun MenuItemCard(
    item: MenuItem,
    accentColor: Color = NeonCyan,
    quantity: Int = 0,
    onAddToCart: () -> Unit,
    onRemoveFromCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        accentColor.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Изображение с неоновой рамкой
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.08f),
                                NeonMagenta.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        accentColor.copy(alpha = 0.2f),
                        CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getEmojiForCategory(item.category),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.weight.isNotEmpty()) {
                    Text(
                        text = item.weight,
                        style = MaterialTheme.typography.labelSmall,
                        color = Outline
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
                        accentColor = accentColor
                    )
                    QuantitySelector(
                        quantity = quantity,
                        accentColor = accentColor,
                        onIncrease = onAddToCart,
                        onDecrease = onRemoveFromCart
                    )
                }
            }
        }
    }
}

// ── Декоративная линия-сканер ─────────────────────────────────

@Composable
fun ScanLineEffect(
    modifier: Modifier = Modifier,
    color: Color = NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                val lineWidth = size.width * 0.4f
                val x = (size.width + lineWidth) * offset - lineWidth
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            color.copy(alpha = 0.6f),
                            color,
                            color.copy(alpha = 0.6f),
                            Color.Transparent
                        ),
                        startX = x,
                        endX = x + lineWidth
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
    )
}

// ── Утилита ───────────────────────────────────────────────────

fun getEmojiForCategory(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
