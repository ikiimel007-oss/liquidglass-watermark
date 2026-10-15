package com.liquidglass.watermark.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

/** 有 backdrop 时使用玻璃 drawBackdrop，无 backdrop 时退化为纯色表面（便于复用 / 预览）。 */
private fun Modifier.glassOrPlain(
    backdrop: Backdrop?,
    shape: Shape,
    plainColor: Color = Color(0xCCFFFFFF),
    effects: BackdropEffectScope.() -> Unit,
): Modifier {
    return if (backdrop == null) {
        this.background(plainColor, shape)
    } else {
        this.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = effects,
            highlight = { Highlight.Ambient },
            shadow = { Shadow(radius = 8f.dp, color = Color.Black.copy(alpha = 0.06f)) },
            innerShadow = { InnerShadow(radius = 6f.dp, alpha = 0.4f) },
        )
    }
}

@Composable
private fun glassAccentColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF0091FF) else Color(0xFF0088FF)

// ────────────────────────────────────────────────────────────
// LiquidSlider
// ────────────────────────────────────────────────────────────
@Composable
fun LiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier.height(24f.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth
        val progress =
            ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start))
                .coerceIn(0f, 1f)
        val knobSize = 18f.dp

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 轨道
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6f.dp)
                    .clip(Capsule())
                    .background(
                        if (isSystemInDarkTheme()) Color(0xFF787880).copy(0.36f)
                        else Color(0xFF787878).copy(0.2f)
                    ),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0.001f, 1f))
                        .height(6f.dp)
                        .clip(Capsule())
                        .background(glassAccentColor()),
                )
            }
        }

        // 玻璃旋钮
        Box(
            Modifier
                .offset { IntOffset(((trackWidth - 18f) * progress).roundToInt(), 0) }
                .size(knobSize)
                .glassOrPlain(
                    backdrop = backdrop,
                    shape = CircleShape,
                    effects = {
                        blur(8f.dp.toPx())
                        lens(8f.dp.toPx(), 10f.dp.toPx(), chromaticAberration = true)
                        vibrancy()
                    },
                )
                .pointerInput(valueRange, trackWidth) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val delta =
                            (valueRange.endInclusive - valueRange.start) * (dragAmount / trackWidth)
                        onValueChange((value + delta).coerceIn(valueRange))
                    }
                }
                .then(
                    if (backdrop == null) {
                        Modifier.background(Color.White, CircleShape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 内部高光点
            Box(
                Modifier
                    .size(4f.dp)
                    .offset(x = -2.dp, y = -1.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// LiquidButton
// ────────────────────────────────────────────────────────────
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier
            .height(48f.dp)
            .glassOrPlain(
                backdrop = backdrop,
                shape = Capsule(),
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(14f.dp.toPx(), 24f.dp.toPx())
                },
            )
            .clickableGlass(onClick)
            .padding(horizontal = 20f.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

// ────────────────────────────────────────────────────────────
// LiquidToggle
// ────────────────────────────────────────────────────────────
@Composable
fun LiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
) {
    val knobProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(200),
        label = "toggle",
    )
    val width = 52f.dp
    val knobSize = 22f.dp

    Box(
        modifier
            .width(width)
            .height(30f.dp)
            .glassOrPlain(
                backdrop = backdrop,
                shape = Capsule(),
                effects = {
                    blur(6f.dp.toPx())
                    lens(10f.dp.toPx(), 14f.dp.toPx())
                },
            )
            .clickableGlass { onCheckedChange(!checked) },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .padding(4f.dp)
                .offset { IntOffset(((width.value - knobSize.value) * knobProgress).roundToInt(), 0) }
                .size(knobSize)
                .glassOrPlain(
                    backdrop = backdrop,
                    shape = CircleShape,
                    effects = {
                        blur(4f.dp.toPx())
                        vibrancy()
                    },
                )
                .then(
                    if (backdrop == null) Modifier.background(Color.White, CircleShape)
                    else Modifier
                )
                .background(
                    if (checked) glassAccentColor().copy(alpha = 0.6f) else Color.Transparent,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(4f.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f)),
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// LiquidTabs
// ────────────────────────────────────────────────────────────
@Composable
fun LiquidTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
) {
    val itemWidth = 96f.dp
    val indicatorWidth = itemWidth - 12f.dp

    Row(
        modifier
            .glassOrPlain(
                backdrop = backdrop,
                shape = RoundedRectangle(24f.dp),
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
            )
            .padding(6f.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .width(itemWidth)
                    .height(40f.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        Modifier
                            .width(indicatorWidth)
                            .height(32f.dp)
                            .glassOrPlain(
                                backdrop = backdrop,
                                shape = RoundedRectangle(16f.dp),
                                effects = {
                                    blur(4f.dp.toPx())
                                    vibrancy()
                                },
                            ),
                    )
                }
                Text(
                    title,
                    color = if (selected) Color.White else Color(0xCCFFFFFF),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableGlass { onSelected(index) },
                )
            }
        }
    }
}

private fun Modifier.clickableGlass(onClick: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        detectTapGestures(onTap = { onClick() })
    }
