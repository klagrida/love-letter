package com.loveletter.desktop

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.loveletter.ui.LoveLetterApp

fun main() = application {
    val state = rememberWindowState(
        size = DpSize(1024.dp, 768.dp),
        position = WindowPosition(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "Love Letter",
        resizable = true
    ) {
        LoveLetterApp()
    }
}
