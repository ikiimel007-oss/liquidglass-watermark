package com.liquidglass.watermark

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "液态玻璃水印 · Liquid Glass Watermark",
        state = rememberWindowState(width = 1150.dp, height = 800.dp),
    ) {
        App()
    }
}
