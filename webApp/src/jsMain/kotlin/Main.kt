package com.loveletter.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.loveletter.ui.LoveLetterApp
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow(
            title = "Love Letter",
            canvasElementId = "ComposeTarget"
        ) {
            LoveLetterApp()
        }
    }
}
