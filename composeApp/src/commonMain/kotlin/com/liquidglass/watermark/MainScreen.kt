package com.liquidglass.watermark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liquidglass.watermark.components.LiquidButton
import com.liquidglass.watermark.components.LiquidSlider
import com.liquidglass.watermark.components.LiquidTabs
import com.liquidglass.watermark.components.LiquidToggle
import com.liquidglass.watermark.model.HighlightStyleId
import com.liquidglass.watermark.model.SurfaceBlend
import com.liquidglass.watermark.model.WatermarkConfig
import com.liquidglass.watermark.model.WatermarkPlacement
import com.liquidglass.watermark.watermark.BackdropHost
import com.liquidglass.watermark.watermark.LiquidGlassWatermarkBar
import com.liquidglass.watermark.watermark.LocalBackdrop
import com.kyant.shapes.RoundedRectangle

@Composable
fun App() {
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF57B6FF))) {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    var config by remember { mutableStateOf(WatermarkConfig()) }
    var tab by remember { mutableIntStateOf(0) }
    var demoToggle by remember { mutableStateOf(true) }
    val tabs = listOf("滤镜", "高光", "阴影", "布局文字")

    // 全窗口壁纸 + 全屏 backdrop（供控制面板等玻璃控件采样）
    BackdropHost(Modifier.fillMaxSize()) {
        val appBackdrop = LocalBackdrop.current

        Column(Modifier.fillMaxSize()) {
            // ── 预览区 ───────────────────────────────────────
            Box(
                Modifier
                    .weight(1.35f)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedRectangle(28f.dp))
                ) {
                    // 图片自身是一个 backdrop，水印条采样它 → 模糊/折射图片
                    BackdropHost(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        val imageBackdrop = LocalBackdrop.current
                        ImageScene(Modifier.fillMaxSize())

                        if (config.placement == WatermarkPlacement.OverlayBottom) {
                            LiquidGlassWatermarkBar(
                                backdrop = imageBackdrop,
                                config = config,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = config.bottomPaddingDp.dp)
                                    .fillMaxWidth(0.92f)
                                    .height(config.watermarkHeightDp.dp),
                            ) { WatermarkContent(config) }
                        }
                    }

                    // 放置在图片下方的模式
                    if (config.placement == WatermarkPlacement.BelowImage) {
                        LiquidGlassWatermarkBar(
                            backdrop = appBackdrop,
                            config = config,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .height(config.watermarkHeightDp.dp),
                        ) { WatermarkContent(config) }
                    }
                }
            }

            // ── 控制面板 ─────────────────────────────────────
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedRectangle(28f.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x22000000), Color(0x00000000)),
                            ),
                        )
                        .padding(16.dp),
                ) {
                    // 顶栏：标题 + 演示 Tabs
                    Text(
                        "液态玻璃水印 · Liquid Glass Watermark",
                        color = Color.White,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    )
                    Row(
                        Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiquidTabs(
                            titles = tabs,
                            selectedIndex = tab,
                            onSelected = { tab = it },
                            backdrop = appBackdrop,
                        )
                        Box(Modifier.width(12.dp))
                        LiquidToggle(
                            checked = demoToggle,
                            onCheckedChange = { demoToggle = it },
                            backdrop = appBackdrop,
                        )
                    }

                    Column(
                        Modifier
                            .padding(top = 14.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        when (tab) {
                            0 -> FilterPanel(config, appBackdrop) { config = it }
                            1 -> HighlightPanel(config, appBackdrop) { config = it }
                            2 -> ShadowPanel(config, appBackdrop) { config = it }
                            else -> LayoutTextPanel(config, appBackdrop) { config = it }
                        }

                        // 底部演示按钮
                        Row(
                            Modifier
                                .padding(top = 6.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LiquidButton(
                                onClick = { config = WatermarkConfig() },
                                backdrop = appBackdrop,
                                modifier = Modifier.weight(1f),
                            ) { Text("恢复默认", color = Color.White, fontSize = 14.sp) }
                            LiquidButton(
                                onClick = { demoToggle = !demoToggle },
                                backdrop = appBackdrop,
                                modifier = Modifier.weight(1f),
                                tint = Color(0xFFFF8D28),
                            ) { Text("切换开关", color = Color.White, fontSize = 14.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatermarkContent(config: WatermarkConfig) {
    Icon(
        Icons.Filled.WaterDrop,
        contentDescription = null,
        tint = config.textColor.copy(alpha = config.textOpacity),
        modifier = Modifier.width(18.dp),
    )
    Text(
        config.watermarkText,
        color = config.textColor.copy(alpha = config.textOpacity),
        fontSize = config.textSizeSp.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = config.textLetterSpacingSp.sp,
    )
}

// ────────────────────────────────────────────────────────────
// 控制面板工具
// ────────────────────────────────────────────────────────────
@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    backdrop: com.kyant.backdrop.Backdrop?,
    format: (Float) -> String = { twoDecimals(it) },
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = Color(0xFFE6E6E6), fontSize = 13.sp)
            Text(
                format(value),
                color = Color(0xFF57B6FF),
                fontSize = 12.sp,
                style = TextStyle(fontWeight = FontWeight.Medium),
            )
        }
        LiquidSlider(value, onValueChange, valueRange, backdrop)
    }
}

@Composable
private fun LabeledToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: com.kyant.backdrop.Backdrop?,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color(0xFFE6E6E6), fontSize = 13.sp)
        LiquidToggle(checked, onCheckedChange, backdrop)
    }
}

// ── 滤镜 ────────────────────────────────────────────────────
@Composable
private fun FilterPanel(
    config: WatermarkConfig,
    backdrop: com.kyant.backdrop.Backdrop?,
    onChange: (WatermarkConfig) -> Unit,
) {
    LabeledSlider("模糊半径", config.blurRadiusDp, 0f..40f, { onChange(config.copy(blurRadiusDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("折射高度", config.refractionHeightFraction, 0f..0.5f, { onChange(config.copy(refractionHeightFraction = it)) }, backdrop)
    LabeledSlider("折射强度", config.refractionAmountFraction, 0f..0.6f, { onChange(config.copy(refractionAmountFraction = it)) }, backdrop)
    LabeledSlider("玻璃不透明度", config.glassOpacity, 0.05f..1f, { onChange(config.copy(glassOpacity = it)) }, backdrop)
    LabeledSlider("亮度", config.brightness, -1f..1f, { onChange(config.copy(brightness = it)) }, backdrop)
    LabeledSlider("对比度", config.contrast, 0.5f..1.5f, { onChange(config.copy(contrast = it)) }, backdrop)
    LabeledSlider("饱和度", config.saturation, 0f..2f, { onChange(config.copy(saturation = it)) }, backdrop)
    LabeledToggle("鲜艳度 (vibrancy)", config.vibrancy, { onChange(config.copy(vibrancy = it)) }, backdrop)
    LabeledToggle("折射深度 (depthEffect)", config.depthEffect, { onChange(config.copy(depthEffect = it)) }, backdrop)
    LabeledToggle("色差 (chromaticAberration)", config.chromaticAberration, { onChange(config.copy(chromaticAberration = it)) }, backdrop)
    LabeledToggle("表面噪点", config.showGrain, { onChange(config.copy(showGrain = it)) }, backdrop)
}

// ── 高光 ────────────────────────────────────────────────────
@Composable
private fun HighlightPanel(
    config: WatermarkConfig,
    backdrop: com.kyant.backdrop.Backdrop?,
    onChange: (WatermarkConfig) -> Unit,
) {
    LabeledToggle("启用高光", config.highlightEnabled, { onChange(config.copy(highlightEnabled = it)) }, backdrop)
    LabeledSlider("高光宽度", config.highlightWidthDp, 0f..8f, { onChange(config.copy(highlightWidthDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("高光透明度", config.highlightAlpha, 0f..1f, { onChange(config.copy(highlightAlpha = it)) }, backdrop)
    if (config.highlightStyle == HighlightStyleId.Default) {
        LabeledSlider("高光角度", config.highlightAngleDeg, 0f..180f, { onChange(config.copy(highlightAngleDeg = it)) }, backdrop, { "${it.round()}°" })
        LabeledSlider("高光衰减", config.highlightFalloff, 0.2f..3f, { onChange(config.copy(highlightFalloff = it)) }, backdrop)
    }
    if (config.highlightStyle == HighlightStyleId.Ambient) {
        LabeledSlider("环境光强度", config.highlightIntensity, 0f..1f, { onChange(config.copy(highlightIntensity = it)) }, backdrop)
    }
    Text("高光样式", color = Color(0xFFE6E6E6), fontSize = 13.sp)
    LiquidTabs(
        titles = listOf("Default", "Ambient", "Plain"),
        selectedIndex = config.highlightStyle.ordinal,
        onSelected = { onChange(config.copy(highlightStyle = HighlightStyleId.entries[it])) },
        backdrop = backdrop,
    )
}

// ── 阴影 ────────────────────────────────────────────────────
@Composable
private fun ShadowPanel(
    config: WatermarkConfig,
    backdrop: com.kyant.backdrop.Backdrop?,
    onChange: (WatermarkConfig) -> Unit,
) {
    LabeledToggle("启用外阴影", config.shadowEnabled, { onChange(config.copy(shadowEnabled = it)) }, backdrop)
    LabeledSlider("外阴影半径", config.shadowRadiusDp, 0f..60f, { onChange(config.copy(shadowRadiusDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("外阴影偏移 X", config.shadowOffsetXDp, -30f..30f, { onChange(config.copy(shadowOffsetXDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("外阴影偏移 Y", config.shadowOffsetYDp, -30f..30f, { onChange(config.copy(shadowOffsetYDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("外阴影不透明度", config.shadowAlpha, 0f..1f, { onChange(config.copy(shadowAlpha = it)) }, backdrop)
    LabeledToggle("启用内阴影", config.innerShadowEnabled, { onChange(config.copy(innerShadowEnabled = it)) }, backdrop)
    LabeledSlider("内阴影半径", config.innerShadowRadiusDp, 0f..60f, { onChange(config.copy(innerShadowRadiusDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("内阴影偏移 Y", config.innerShadowOffsetYDp, -60f..60f, { onChange(config.copy(innerShadowOffsetYDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("内阴影不透明度", config.innerShadowAlpha, 0f..1f, { onChange(config.copy(innerShadowAlpha = it)) }, backdrop)
}

// ── 布局文字 ────────────────────────────────────────────────
@Composable
private fun LayoutTextPanel(
    config: WatermarkConfig,
    backdrop: com.kyant.backdrop.Backdrop?,
    onChange: (WatermarkConfig) -> Unit,
) {
    OutlinedTextField(
        value = config.watermarkText,
        onValueChange = { onChange(config.copy(watermarkText = it)) },
        label = { Text("水印文字", color = Color(0xFFAAAAAA)) },
        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    LabeledSlider("文字大小", config.textSizeSp, 8f..40f, { onChange(config.copy(textSizeSp = it)) }, backdrop, { "${it.round()}sp" })
    LabeledSlider("文字不透明度", config.textOpacity, 0f..1f, { onChange(config.copy(textOpacity = it)) }, backdrop)
    LabeledSlider("文字字距", config.textLetterSpacingSp, 0f..8f, { onChange(config.copy(textLetterSpacingSp = it)) }, backdrop, { "${it.round()}sp" })
    LabeledSlider("水印条高度", config.watermarkHeightDp, 20f..120f, { onChange(config.copy(watermarkHeightDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledSlider("圆角比例", config.cornerRadiusFraction, 0f..0.5f, { onChange(config.copy(cornerRadiusFraction = it)) }, backdrop)
    LabeledSlider("距底边距", config.bottomPaddingDp, 0f..40f, { onChange(config.copy(bottomPaddingDp = it)) }, backdrop, { "${it.round()}dp" })
    LabeledToggle("全胶囊圆角", config.capsuleCorners, { onChange(config.copy(capsuleCorners = it)) }, backdrop)
    LabeledToggle("置于图片下方", config.placement == WatermarkPlacement.BelowImage, {
        onChange(config.copy(placement = if (it) WatermarkPlacement.BelowImage else WatermarkPlacement.OverlayBottom))
    }, backdrop)
    LabeledToggle("顶部高光分隔线", config.showDivider, { onChange(config.copy(showDivider = it)) }, backdrop)
}

private fun Float.round(): Int = (this + 0.5f).toInt()

/** 跨平台两位小数格式化（避免 JS 上没有的 String.format）。 */
private fun twoDecimals(value: Float): String {
    val rounded = kotlin.math.round(value * 100).toInt()
    val sign = if (rounded < 0) "-" else ""
    val a = kotlin.math.abs(rounded)
    val whole = a / 100
    val frac = a % 100
    return "$sign$whole.${if (frac < 10) "0" else ""}$frac"
}
