package com.loveletter

import kotlinx.browser.window

class JsPlatform : Platform {
    override val name: String = "Web ${window.navigator.userAgent}"
}

actual fun getPlatform(): Platform = JsPlatform()
