package com.team12kotlin.juggle.ui.tools.wheel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.team12kotlin.juggle.ui.theme.JuggleTheme
import kotlin.math.cos
import kotlin.math.sin

private const val LABEL_RADIUS = 0.62f

/** The wheel itself; [rotation] is in radians and the pointer stays fixed at the top. */
@Composable
fun DecisionWheel(
    options: List<String>,
    rotation: Float,
    modifier: Modifier = Modifier,
    size: Dp = 300.dp
) {
    val colors = MaterialTheme.colorScheme
    val palette = listOf(
        colors.primary,
        colors.primaryContainer,
        colors.secondaryContainer,
        colors.tertiary,
        colors.surfaceContainerHighest,
        colors.inversePrimary
    )
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    val labelWidth = with(LocalDensity.current) { ((size - 28.dp) / 2 * LABEL_RADIUS).roundToPx() }
    val labels = remember(options, labelStyle, labelWidth) {
        options.map {
            measurer.measure(
                text = AnnotatedString(it),
                style = labelStyle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                constraints = Constraints(maxWidth = labelWidth)
            )
        }
    }
    val separator = colors.surface
    val hub = colors.surface
    val ring = colors.primary
    val pointer = colors.primary
    val onSurface = colors.onSurface

    Canvas(modifier = modifier.size(size)) {
        if (options.isEmpty()) return@Canvas

        val radius = this.size.minDimension / 2 - 14.dp.toPx()
        val center = this.center
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)
        val sweep = 360f / options.size
        val rotationDegrees = Math.toDegrees(rotation.toDouble()).toFloat()

        options.indices.forEach { i ->
            val start = rotationDegrees - 90f + i * sweep
            // With 7, 13... options the last segment would repeat the first color next to it.
            val color = if (i == options.lastIndex && options.size > 1 && i % palette.size == 0) {
                palette[2]
            } else {
                palette[i % palette.size]
            }
            drawArc(color, start, sweep, true, topLeft, arcSize)
            drawArc(separator, start, sweep, true, topLeft, arcSize, style = Stroke(2.dp.toPx()))

            val middle = Math.toRadians((start + sweep / 2).toDouble())
            val label = labels[i]
            val position = Offset(
                center.x + (radius * LABEL_RADIUS * cos(middle)).toFloat(),
                center.y + (radius * LABEL_RADIUS * sin(middle)).toFloat()
            )
            // Labels on the left half would read upside down.
            val textAngle = start + sweep / 2 + if (cos(middle) < 0) 180f else 0f
            rotate(degrees = textAngle, pivot = position) {
                drawText(
                    textLayoutResult = label,
                    color = if (color.luminance() > 0.5f) onSurface else Color.White,
                    topLeft = Offset(
                        position.x - label.size.width / 2f,
                        position.y - label.size.height / 2f
                    )
                )
            }
        }

        drawCircle(ring, radius, center, style = Stroke(3.dp.toPx()))
        drawCircle(hub, radius * 0.13f, center)
        drawCircle(ring, radius * 0.13f, center, style = Stroke(3.dp.toPx()))

        val tip = center.y - radius + 14.dp.toPx()
        val pointerPath = Path().apply {
            moveTo(center.x - 12.dp.toPx(), center.y - radius - 8.dp.toPx())
            lineTo(center.x + 12.dp.toPx(), center.y - radius - 8.dp.toPx())
            lineTo(center.x, tip)
            close()
        }
        drawPath(pointerPath, pointer)
    }
}

@Preview(showBackground = true)
@Composable
private fun DecisionWheelPreview() {
    JuggleTheme {
        DecisionWheel(options = listOf("Pizza", "Sushi", "Tacos", "Burgers", "Salad", "Pasta"), rotation = 0f)
    }
}
