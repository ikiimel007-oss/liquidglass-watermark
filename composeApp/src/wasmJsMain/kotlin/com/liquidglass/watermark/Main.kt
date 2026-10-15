package com.liquidglass.watermark

import androidx.compose.ui.window.CanvasBasedWindow

fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget", title = "液态玻璃水印 · Liquid Glass Watermark") {
        App()
    }
}
