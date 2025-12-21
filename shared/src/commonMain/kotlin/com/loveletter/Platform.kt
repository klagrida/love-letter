package com.loveletter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
