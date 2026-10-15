package com.liquidglass.watermark.watermark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.opacity
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import com.liquidglass.watermark.model.SurfaceBlend
import com.liquidglass.watermark.model.WatermarkConfig
import kotlin.random.Random

/** 提供 [BackdropHost] 记录的背景 layer backdrop。 */
val LocalBackdrop = compositionLocalOf<Backdrop?> { null }

/**
 * 把一个区域（通常是图片 / 场景）录制进 layer backdrop，供其上的玻璃控件采样。
 * 所有玻璃元素通过 `LocalBackdrop.current` 获取背景进行模糊 / 折射。
 */
@Composable
fun BackdropHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    androidx.compose.runtime.CompositionLocalProvider(LocalBackdrop provides backdrop) {
        Box(modifier.layerBackdrop(backdrop)) {
            content()
        }
    }
}

/**
 * 核心：液态玻璃水印 Modifier（Modifier 方式调用）。
 *
 * 它把当前组件绘制成一块“液态玻璃”表面——使用 [com.kyant.backdrop.drawBackdrop]，
 * 采样它身后的 [backdrop]（通常就是图片），并应用配置里的全部滤镜：
 * 模糊 [com.kyant.backdrop.effects.blur]、折射 [com.kyant.backdrop.effects.lens]、
 * 色温/透明度/鲜艳度，以及高光 [com.kyant.backdrop.highlight.Highlight]、外阴影
 * [com.kyant.backdrop.shadow.Shadow]、内阴影 [com.kyant.backdrop.shadow.InnerShadow]。
 */
@Stable
fun Modifier.liquidGlassWatermark(
    backdrop: Backdrop?,
    config: WatermarkConfig,
): Modifier {
    if (backdrop == null) return this

    val shape: () -> Shape = {
        if (config.capsuleCorners) {
            Capsule()
        } else {
            RoundedRectangle((config.watermarkHeightDp * config.cornerRadiusFraction).dp)
        }
    }

    return this.drawBackdrop(
        backdrop = backdrop,
        shape = shape,
        effects = {
            if (config.vibrancy) vibrancy()
            if (config.blurRadiusDp > 0f) {
                blur(config.blurRadiusDp.dp.toPx())
            }
            if (config.brightness != 0f || config.contrast != 1f || config.saturation != 1f) {
                colorControls(
                    brightness = config.brightness,
                    contrast = config.contrast,
                    saturation = config.saturation,
                )
            }
            if (config.glassOpacity < 1f) {
                opacity(config.glassOpacity)
            }
            if (config.refractionHeightFraction > 0f && config.refractionAmountFraction > 0f) {
                val minDimension = size.minDimension
                lens(
                    refractionHeight = config.refractionHeightFraction * minDimension,
                    refractionAmount = config.refractionAmountFraction * minDimension,
                    depthEffect = config.depthEffect,
                    chromaticAberration = config.chromaticAberration,
                )
            }
        },
        highlight = { config.resolveHighlight() },
        shadow = {
            if (!config.shadowEnabled) null
            else Shadow(
                radius = config.shadowRadiusDp.dp,
                offset = DpOffset(config.shadowOffsetXDp.dp, config.shadowOffsetYDp.dp),
                color = config.shadowColor.copy(alpha = config.shadowAlpha),
                alpha = 1f,
            )
        },
        innerShadow = {
            if (!config.innerShadowEnabled) null
            else InnerShadow(
                radius = config.innerShadowRadiusDp.dp,
                offset = DpOffset(
                    config.innerShadowOffsetXDp.dp,
                    config.innerShadowOffsetYDp.dp,
                ),
                color = config.innerShadowColor.copy(alpha = config.innerShadowAlpha),
                alpha = 1f,
            )
        },
        onDrawSurface = {
            drawSurfaceOverlays(config)
        },
    )
}

/** 便捷组件：液态玻璃水印条。内部使用 [Modifier.liquidGlassWatermark]，居中显示 [content]。 */
@Composable
fun LiquidGlassWatermarkBar(
    backdrop: Backdrop?,
    config: WatermarkConfig,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(modifier.liquidGlassWatermark(backdrop, config)) {
        Box(
            Modifier.matchParentSize(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/** 在玻璃表面之上叠加：着色层、顶部分隔高光、细粒度噪点。 */
private fun DrawScope.drawSurfaceOverlays(config: WatermarkConfig) {
    val surfaceColor = config.surfaceTintColor
    if (surfaceColor != null) {
        when (config.surfaceBlendMode) {
            SurfaceBlend.Hue -> {
                drawRect(surfaceColor, blendMode = BlendMode.Hue)
                drawRect(surfaceColor.copy(alpha = config.surfaceTintAlpha))
            }
            SurfaceBlend.Tint -> drawRect(surfaceColor.copy(alpha = config.surfaceTintAlpha))
            SurfaceBlend.None -> drawRect(surfaceColor.copy(alpha = config.surfaceTintAlpha))
        }
    }
    if (config.showDivider) {
        drawLine(
            color = Color.White.copy(alpha = 0.35f),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.5f,
        )
    }
    if (config.showGrain) {
        Grain.draw(this)
    }
}

/** 确定性细粒度噪点，模拟玻璃表面颗粒质感。 */
private object Grain {
    val points: List<Triple<Float, Float, Float>> by lazy {
        val r = Random(42)
        List(160) { Triple(r.nextFloat(), r.nextFloat(), 0.05f + r.nextFloat() * 0.10f) }
    }

    fun draw(scope: DrawScope) {
        val w = scope.size.width
        val h = scope.size.height
        for ((nx, ny, alpha) in points) {
            scope.drawRect(
                color = Color.White.copy(alpha = alpha * 0.5f),
                topLeft = Offset(nx * w, ny * h),
                size = androidx.compose.ui.geometry.Size(8f, 8f),
            )
        }
    }
}
