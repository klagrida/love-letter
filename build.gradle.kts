plugins {
    kotlin("multiplatform") version "1.9.21" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("com.android.application") version "8.1.4" apply false
    id("com.android.library") version "8.1.4" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
}

allprojects {
    group = "com.loveletter"
    version = "1.0.0"
}
