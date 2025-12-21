package com.loveletter.web

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.loveletter.ui.LoveLetterApp
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        // Wrap Compose UI app in a web-compatible way
        // Note: This is a placeholder - full Compose UI apps don't directly work in Compose for Web
        // You would need to either:
        // 1. Rewrite the UI using Compose for Web (org.jetbrains.compose.web.dom) APIs
        // 2. Or switch to WASM target instead of JS

        // For now, create a simple placeholder that compiles
        org.jetbrains.compose.web.dom.Div {
            org.jetbrains.compose.web.dom.H1 {
                org.jetbrains.compose.web.dom.Text("Love Letter")
            }
            org.jetbrains.compose.web.dom.P {
                org.jetbrains.compose.web.dom.Text("Web version coming soon. Please use the desktop or mobile app.")
            }
        }
    }
}
