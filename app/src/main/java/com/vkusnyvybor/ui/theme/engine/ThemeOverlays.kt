package com.vkusnyvybor.ui.theme.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════
//  Scanline Overlay — горизонтальные полоски поверх экрана
// ══════════════════════════════════════════════════════════════

@Composable
fun ScanlineOverlay(
    color: Color = LocalThemeDecorations.current.scanlineColor,
    lineSpacing: Float = 4f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val h = size.height
        val w = size.width
        var y = 0f
        while (y < h) {
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += lineSpacing
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Neon Divider — неоновая линия-разделитель
// ══════════════════════════════════════════════════════════════

@Composable
fun NeonDivider(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        val w = size.width
        val gradient = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, color, color, Color.Transparent),
            startX = 0f,
            endX = w
        )
        drawLine(
            brush = gradient,
            start = Offset(0f, size.height / 2),
            end = Offset(w, size.height / 2),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

// ══════════════════════════════════════════════════════════════
//  Grid Overlay — сетка для Cyberpunk
// ══════════════════════════════════════════════════════════════

@Composable
fun GridOverlay(
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
    cellSize: Float = 40f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Вертикальные линии
        var x = 0f
        while (x < w) {
            drawLine(color, Offset(x, 0f), Offset(x, h), strokeWidth = 0.5f)
            x += cellSize
        }
        // Горизонтальные линии
        var y = 0f
        while (y < h) {
            drawLine(color, Offset(0f, y), Offset(w, y), strokeWidth = 0.5f)
            y += cellSize
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Biohazard Stripe — полоски-предупреждение (Umbrella)
// ══════════════════════════════════════════════════════════════

@Composable
fun BiohazardStripe(
    color: Color = Color(0xFFCE1126),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(4.dp)) {
        val w = size.width
        val h = size.height
        val stripeWidth = 12f
        var x = -stripeWidth
        while (x < w + stripeWidth) {
            drawLine(
                color = color.copy(alpha = 0.6f),
                start = Offset(x, 0f),
                end = Offset(x + stripeWidth, h),
                strokeWidth = 3f
            )
            x += stripeWidth * 2
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Terminal Cursor — мигающий курсор (для Marathon header)
// ══════════════════════════════════════════════════════════════

@Composable
fun TerminalDots(
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(3.dp)) {
        val w = size.width
        val dotSize = 2f
        val spacing = 8f
        var x = 0f
        while (x < w) {
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = dotSize,
                center = Offset(x, size.height / 2)
            )
            x += spacing
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  Hexagonal Grid — гексагональный паттерн (футуристический)
// ══════════════════════════════════════════════════════════════

@Composable
fun HexGridOverlay(
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
    hexSize: Float = 30f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val hexHeight = hexSize * 2
        val hexWidth = hexSize * 1.732f // sqrt(3)

        var row = 0
        var y = 0f
        while (y < h + hexHeight) {
            var x = if (row % 2 == 0) 0f else hexWidth / 2
            while (x < w + hexWidth) {
                drawHexagon(x, y, hexSize, color)
                x += hexWidth
            }
            y += hexHeight * 0.75f
            row++
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHexagon(
    cx: Float, cy: Float, size: Float, color: Color
) {
    val path = Path()
    for (i in 0..5) {
        val angle = Math.toRadians((60.0 * i - 30.0)).toFloat()
        val x = cx + size * cos(angle)
        val y = cy + size * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Stroke(width = 0.5f))
}
