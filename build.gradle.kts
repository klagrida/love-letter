plugins {
    kotlin("multiplatform") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    kotlin("plugin.compose") version "2.2.21" apply false
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.compose") version "1.9.3" apply false
}

allprojects {
    group = "com.loveletter"
    version = "1.0.0"
}
