package com.vkusnyvybor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vkusnyvybor.data.model.CartItemConfig
import com.vkusnyvybor.data.model.MenuItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductBottomSheet(
    item: MenuItem,
    onDismiss: () -> Unit,
    onAddToCart: (MenuItem, CartItemConfig) -> Unit
) {
    var selectedSizeId by remember { mutableStateOf(item.sizes.firstOrNull()?.id) }
    var selectedStates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var quantity by remember { mutableStateOf(1) }

    val basePrice = item.price
    val sizeAdd = item.sizes.find { it.id == selectedSizeId }?.priceAdd ?: 0
    val modifierAdd = item.modifiers
        .filter { !it.included && it.id in selectedStates }
        .sumOf { it.priceAdd }
        
    val totalPrice = (basePrice + sizeAdd + modifierAdd) * quantity

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    var modifiersSectionY by remember { mutableStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Box(modifier = Modifier.fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getEmojiForProduct(item.category), fontSize = 110.sp)
                    }

                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 36.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                if (item.weight.isNotEmpty()) {
                                    Text(
                                        item.weight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(modifiersSectionY.toInt())
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Outlined.Tune, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Настроить", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 28.sp
                        )

                        // ── Выбор размера ──
                        if (item.sizes.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Размер порции",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item.sizes.forEach { size ->
                                    val isSelected = selectedSizeId == size.id
                                    val containerColor by animateColorAsState(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                                        label = "size_color"
                                    )
                                    val borderColor by animateColorAsState(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        label = "size_border"
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                width = 2.dp,
                                                color = borderColor,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .clickable { selectedSizeId = size.id },
                                        color = containerColor,
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                size.name,
                                                style = MaterialTheme.typography.labelMedium, // Уменьшили шрифт
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1, // Чтобы не переносилось
                                                overflow = TextOverflow.Clip
                                            )
                                            if (size.priceAdd > 0) {
                                                Text(
                                                    "+${size.priceAdd}\u20BD",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── Состав ──
                        if (item.modifiers.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Column(
                                modifier = Modifier.onGloballyPositioned { 
                                    modifiersSectionY = it.positionInParent().y 
                                }
                            ) {
                                Text(
                                    "Состав",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    "Настройте ингредиенты под себя",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(Modifier.height(12.dp))

                                item.modifiers.forEach { mod ->
                                    val isActive = if (mod.included) mod.id !in selectedStates else mod.id in selectedStates
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedStates = if (mod.id in selectedStates) {
                                                    selectedStates - mod.id
                                                } else {
                                                    selectedStates + mod.id
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Checkbox(
                                                checked = isActive,
                                                onCheckedChange = null
                                            )
                                            Text(
                                                mod.name, 
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (!mod.included && mod.priceAdd > 0) {
                                            Text(
                                                "+${mod.priceAdd}\u20BD", 
                                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                    }
                }

                // ── Фиксированный низ ──
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp) // Уменьшили общий паддинг, чтобы кнопка была шире
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Селектор количества
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape)
                                .padding(2.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Remove, null)
                            }
                            
                            Text(
                                "$quantity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Add, null)
                            }
                        }

                        // Кнопка ДОБАВИТЬ
                        Button(
                            onClick = {
                                val removed = item.modifiers
                                    .filter { it.included && it.id in selectedStates }
                                    .map { it.id }.toSet()
                                
                                val added = item.modifiers
                                    .filter { !it.included && it.id in selectedStates }
                                    .map { it.id }.toSet()
                                
                                onAddToCart(
                                    item,
                                    CartItemConfig(
                                        selectedSizeId = selectedSizeId,
                                        removedModifiers = removed,
                                        addedModifiers = added
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp) // Минимальные отступы внутри
                        ) {
                            Text(
                                text = "Добавить за ${totalPrice}\u20BD",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), // Чуть меньше шрифт
                                maxLines = 1,
                                overflow = TextOverflow.Visible, // Отключаем обрезание
                                textAlign = TextAlign.Center,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getEmojiForProduct(category: String): String = when (category) {
    "Бургеры" -> "\uD83C\uDF54"
    "Гарниры" -> "\uD83C\uDF5F"
    "Снэки" -> "\uD83C\uDF57"
    "Напитки" -> "\uD83E\uDD64"
    "Десерты" -> "\uD83E\uDD67"
    "Роллы", "Твистеры" -> "\uD83C\uDF2F"
    "Курица", "Корзинки" -> "\uD83C\uDF57"
    else -> "\uD83C\uDF74"
}
