package com.vkusnyvybor.ui.theme.engine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun UmbrellaLogo(modifier: Modifier = Modifier.size(100.dp)) {
    val red = Color(0xFFCA0000)
    val white = Color.White
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        for (i in 0 until 8) {
            val startAngle = i * 45f
            val path = Path().apply {
                moveTo(center.x, center.y)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = 45f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(path, if (i % 2 == 0) red else white)
        }
        drawCircle(white, radius * 0.15f, center)
    }
}

@Composable
fun CyberpunkLogo(modifier: Modifier = Modifier.size(100.dp)) {
    val cyan = Color(0xFF00FBFF)
    val magenta = Color(0xFFFF00FF)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Треугольник/молния
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.2f)
            lineTo(w * 0.5f, h * 0.5f)
            lineTo(w * 0.7f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.9f)
            lineTo(w * 0.4f, h * 0.6f)
            lineTo(w * 0.2f, h * 0.6f)
            close()
        }
        drawPath(path, cyan, style = Stroke(width = 4f))
        drawPath(path, cyan.copy(alpha = 0.3f), style = Fill)
        // Глитч полоска
        drawRect(magenta, Offset(w * 0.1f, h * 0.4f), Size(w * 0.3f, 4f))
    }
}

@Composable
fun MarathonLogo(modifier: Modifier = Modifier.size(100.dp)) {
    val green = Color(0xFF00FF00)
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 0.5f
        // Стилизованный круг с прицелом
        drawCircle(green, size.minDimension * 0.4f, style = Stroke(width = 2f))
        drawRect(green, Offset(size.width * 0.1f, center.y), Size(size.width * 0.8f, 1f))
        drawRect(green, Offset(center.x, size.height * 0.1f), Size(1f, size.height * 0.8f))
    }
}
