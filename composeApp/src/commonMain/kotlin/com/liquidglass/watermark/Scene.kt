package com.liquidglass.watermark

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * 示例“图片”——一张用 Compose Canvas 绘制的风景图（渐变天空 + 太阳 + 山峦 + 水面倒影）。
 * 细节足够丰富，能让液态玻璃的模糊与折射效果一目了然。
 * 也可在 [backgroundImage] 非空时直接绘制用户提供的图片。
 */
@Composable
fun ImageScene(
    modifier: Modifier = Modifier,
    backgroundImage: androidx.compose.ui.graphics.ImageBitmap? = null,
) {
    Canvas(modifier) {
        if (backgroundImage != null) {
            drawImage(backgroundImage)
        } else {
            drawScene()
        }
    }
}

private fun DrawScope.drawScene() {
    val w = size.width
    val h = size.height
    val horizon = h * 0.58f

    // 天空渐变
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFF1B1035),
            0.55f to Color(0xFF5B2A86),
            horizon / h to Color(0xFFB34B6E),
            1f to Color(0xFFF2A65A),
        ),
    )

    // 太阳光晕
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF3B0), Color(0xFFF2A65A).copy(alpha = 0.0f)),
            center = Offset(w * 0.7f, horizon - h * 0.18f),
            radius = h * 0.24f,
        ),
        radius = h * 0.24f,
        center = Offset(w * 0.7f, horizon - h * 0.18f),
    )
    drawCircle(
        color = Color(0xFFFFE08A),
        radius = h * 0.055f,
        center = Offset(w * 0.7f, horizon - h * 0.18f),
    )

    // 远山
    drawMountain(
        base = horizon,
        peaks = listOf(
            Triple(0f, h * 0.30f, w * 0.35f),
            Triple(w * 0.32f, h * 0.26f, w * 0.5f),
        ),
        color = Color(0xFF3A1F63),
    )
    drawMountain(
        base = horizon,
        peaks = listOf(
            Triple(w * 0.55f, h * 0.22f, w * 0.4f),
            Triple(w * 0.8f, h * 0.30f, w * 0.4f),
        ),
        color = Color(0xFF6B2A5B),
    )

    // 水面
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFF7A2A66),
            1f to Color(0xFF241348),
        ),
        topLeft = Offset(0f, horizon),
        size = androidx.compose.ui.geometry.Size(w, h - horizon),
    )

    // 水波 / 倒影
    val waveColor = Color.White.copy(alpha = 0.08f)
    for (i in 0 until 14) {
        val y = horizon + (h - horizon) * (0.15f + 0.06f * i)
        drawLine(
            color = waveColor,
            start = Offset(w * 0.05f, y),
            end = Offset(w * 0.95f, y),
            strokeWidth = 1.5f,
        )
    }
    drawCircle(
        color = Color(0xFFFFC25E).copy(alpha = 0.35f),
        radius = h * 0.03f,
        center = Offset(w * 0.7f, horizon + h * 0.10f),
    )
}

private fun DrawScope.drawMountain(
    base: Float,
    peaks: List<Triple<Float, Float, Float>>,
    color: Color,
) {
    val path = Path()
    val first = peaks.first()
    val leftX = first.first - first.third
    val rightX = peaks.last().first + peaks.last().third
    path.moveTo(leftX, base)
    for ((cx, peakH, halfW) in peaks) {
        path.lineTo(cx - halfW, base)
        path.lineTo(cx, base - peakH)
        path.lineTo(cx + halfW, base)
    }
    path.lineTo(rightX, base)
    path.close()
    drawPath(path, color)
}
