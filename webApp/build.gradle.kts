plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                outputFileName = "loveletter.js"
            }
            binaries.executable()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.html.core)
                implementation(compose.runtime)
            }
        }
    }
}
