import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
}

compose.desktop {
    application {
        mainClass = "com.loveletter.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LoveLetter"
            packageVersion = "1.0.0"
            description = "Love Letter - A game of risk, deduction, and luck"
            copyright = "2024 Love Letter"
            vendor = "Love Letter Games"

            macOS {
                iconFile.set(project.file("icons/icon.icns"))
                bundleID = "com.loveletter.desktop"
            }

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "Love Letter"
                upgradeUuid = "f7b3c8d0-1234-5678-abcd-ef0123456789"
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}
