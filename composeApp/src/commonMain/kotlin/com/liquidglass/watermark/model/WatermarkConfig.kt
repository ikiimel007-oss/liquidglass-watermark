package com.liquidglass.watermark.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle

/** 液态玻璃高光的三种样式（对应库里的 HighlightStyle）。 */
enum class HighlightStyleId { Default, Ambient, Plain }

/** 水印条相对图片的放置方式。 */
enum class WatermarkPlacement { OverlayBottom, BelowImage }

/**
 * 液态玻璃水印的全部可调参数。
 *
 * 这些字段完整映射了 AndroidLiquidGlass / Backdrop 库中
 * [com.kyant.backdrop.drawBackdrop] 的 effects（blur / lens / colorControls / opacity /
 * vibrancy）、highlight（高光样式 / 宽度 / 透明度 / 角度 / 衰减）、shadow（外阴影）以及
 * innerShadow（内阴影）等所有可调项。
 */
@Stable
@Immutable
data class WatermarkConfig(
    // ── 布局与文字 ─────────────────────────────────────────
    val watermarkText: String = "Liquid Glass",
    val watermarkHeightDp: Float = 48f,
    val cornerRadiusFraction: Float = 0.25f,   // 圆角 = barHeight * fraction，0..0.5
    val capsuleCorners: Boolean = false,       // 是否用全胶囊圆角
    val placement: WatermarkPlacement = WatermarkPlacement.OverlayBottom,
    val bottomPaddingDp: Float = 12f,
    val textSizeSp: Float = 16f,
    val textColor: Color = Color.White,
    val textOpacity: Float = 1f,
    val textLetterSpacingSp: Float = 1.5f,

    // ── 背景滤镜 effects ─────────────────────────────────
    val blurRadiusDp: Float = 8f,
    val refractionHeightFraction: Float = 0.12f,   // 折射高度 = barHeight * fraction
    val refractionAmountFraction: Float = 0.18f,   // 折射强度 = barHeight * fraction * 2
    val depthEffect: Boolean = false,
    val chromaticAberration: Boolean = false,
    val vibrancy: Boolean = true,                  // 鲜艳度（提高饱和度）
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val glassOpacity: Float = 1f,                  // 玻璃不透明度

    // ── 高光 highlight ──────────────────────────────────
    val highlightEnabled: Boolean = true,
    val highlightStyle: HighlightStyleId = HighlightStyleId.Default,
    val highlightWidthDp: Float = Highlight().width.value,
    val highlightAlpha: Float = 1f,
    val highlightAngleDeg: Float = 45f,
    val highlightFalloff: Float = 1f,
    val highlightIntensity: Float = 0.38f,         // 仅 Ambient 样式使用

    // ── 外阴影 shadow ───────────────────────────────────
    val shadowEnabled: Boolean = true,
    val shadowRadiusDp: Float = 24f,
    val shadowOffsetXDp: Float = 0f,
    val shadowOffsetYDp: Float = 4f,
    val shadowAlpha: Float = 0.10f,
    val shadowColor: Color = Color.Black,

    // ── 内阴影 innerShadow ──────────────────────────────
    val innerShadowEnabled: Boolean = true,
    val innerShadowRadiusDp: Float = 24f,
    val innerShadowOffsetXDp: Float = 0f,
    val innerShadowOffsetYDp: Float = 24f,
    val innerShadowAlpha: Float = 0.15f,
    val innerShadowColor: Color = Color.Black,

    // ── 表面着色层（在玻璃上面叠加一层颜色/纹理）────────────
    val surfaceTintColor: Color? = null,           // 非空时在玻璃上叠加一层该颜色
    val surfaceTintAlpha: Float = 0.5f,
    val surfaceBlendMode: SurfaceBlend = SurfaceBlend.None,

    // ── 表面附加绘制 ────────────────────────────────────
    val showDivider: Boolean = true,               // 在玻璃上画一条顶部分隔高光
    val showGrain: Boolean = true,                 // 细粒度噪点（增加玻璃质感）
) {
    /** 解析当前配置出的 [Highlight]，供 drawBackdrop 的 highlight 参数使用。 */
    fun resolveHighlight(): Highlight? {
        if (!highlightEnabled) return null
        val style: HighlightStyle = when (highlightStyle) {
            HighlightStyleId.Default -> HighlightStyle.Default(
                color = Color.White,
                angle = highlightAngleDeg,
                falloff = highlightFalloff,
            )
            HighlightStyleId.Ambient -> HighlightStyle.Ambient(
                intensity = highlightIntensity,
            )
            HighlightStyleId.Plain -> HighlightStyle.Plain(color = Color.White)
        }
        return Highlight(
            width = highlightWidthDp.dp,
            blurRadius = (highlightWidthDp / 2f).dp,
            alpha = highlightAlpha,
            style = style,
        )
    }
}

enum class SurfaceBlend { None, Hue, Tint }
