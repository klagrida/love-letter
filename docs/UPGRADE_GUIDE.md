# Love Letter - Dependency Upgrade Guide

## Overview

This guide provides instructions for upgrading dependencies in the Love Letter Kotlin Multiplatform project. Follow this guide whenever you need to update Kotlin, Gradle, Compose, or other major dependencies.

---

## Current Version Information (as of Dec 2025)

### Build Tools
- **Gradle:** 8.5
- **Kotlin:** 1.9.21
- **Compose Multiplatform:** 1.5.11
- **Android Gradle Plugin:** 8.1.4

### Major Dependencies
- **Supabase SDK:** 2.0.4
- **Ktor:** 2.3.7
- **Kotlinx Coroutines:** 1.7.3
- **Kotlinx Serialization:** 1.6.2

---

## Before You Start

### Prerequisites Checklist

- [ ] All current changes are committed to git
- [ ] CI is passing (all tests green)
- [ ] You have 2-20 hours available (depending on upgrade scope)
- [ ] You've created a backup branch: `git checkout -b upgrade-backup`
- [ ] Team is notified of upcoming breaking changes

### Version Compatibility Matrix

Ensure these combinations are compatible before upgrading:

| Kotlin | Compose Multiplatform | AGP | Gradle |
|--------|----------------------|-----|--------|
| 2.3.0 | 1.9.3 | 8.13.2 | 9.2.1 |
| 2.0.x | 1.7.x | 8.7+ | 8.9+ |
| 1.9.x | 1.5.x | 8.1+ | 8.5+ |

**Ktor/Supabase Compatibility:**
- Supabase 3.x requires Ktor 3.x
- Supabase 2.x requires Ktor 2.x

---

## Upgrade Strategy

### Recommended Approach: Phased Upgrades

**DO NOT** upgrade everything at once. Use this 3-phase approach:

```
Phase 1: Foundation (Gradle, Kotlin, AGP)
    ↓ Test & Validate
Phase 2: Frameworks (Compose, Kotlinx, AndroidX)
    ↓ Test & Validate
Phase 3: Backend SDKs (Supabase, Ktor)
    ↓ Test & Validate
```

Commit after each phase succeeds.

---

## Phase 1: Foundation Updates

### Step 1: Update Gradle

**File:** `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-[VERSION]-bin.zip
```

**Example:**
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```

**Validate:**
```bash
./gradlew --version
```

### Step 2: Update Kotlin & AGP

**File:** `build.gradle.kts` (root)

```kotlin
plugins {
    kotlin("multiplatform") version "[KOTLIN_VERSION]" apply false
    kotlin("android") version "[KOTLIN_VERSION]" apply false
    kotlin("plugin.serialization") version "[KOTLIN_VERSION]" apply false
    id("com.android.application") version "[AGP_VERSION]" apply false
    id("com.android.library") version "[AGP_VERSION]" apply false
    id("org.jetbrains.compose") version "[COMPOSE_VERSION]" apply false
}
```

**Example for Kotlin 2.3.0:**
```kotlin
plugins {
    kotlin("multiplatform") version "2.3.0" apply false
    kotlin("android") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.compose") version "1.9.3" apply false
}
```

### Step 3: Update Gradle Properties

**File:** `gradle.properties`

Add/update these properties for Kotlin 2.x:

```properties
# K2 Compiler (required for Kotlin 2.x)
kotlin.experimental.tryK2=true

# Increase heap for larger builds
org.gradle.jvmargs=-Xmx6144M
kotlin.daemon.jvm.options=-Xmx4096M

# Gradle optimization
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
```

### Step 4: Validate Phase 1

```bash
# Clean and rebuild
./gradlew clean

# Build shared module only (fastest validation)
./gradlew :shared:build

# If successful, build all modules
./gradlew build
```

**If build fails:**
- Check Kotlin version compatibility with Compose
- Review error logs for deprecation warnings
- Consult [Kotlin migration guide](https://kotlinlang.org/docs/releases.html)

**Commit Phase 1:**
```bash
git add .
git commit -m "chore: upgrade foundation - Gradle [VERSION], Kotlin [VERSION], AGP [VERSION]"
```

---

## Phase 2: Framework Updates

### Step 1: Update Compose Compiler Extension

**File:** `androidApp/build.gradle.kts`

```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = "[VERSION]"
}
```

**Kotlin → Compose Compiler Compatibility:**
| Kotlin | Compose Compiler Extension |
|--------|---------------------------|
| 2.3.0 | 1.5.15 |
| 2.0.0 | 1.5.14 |
| 1.9.21 | 1.5.4 |

### Step 2: Update Kotlinx Libraries

**File:** `shared/build.gradle.kts`

In `commonMain` dependencies:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:[VERSION]")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:[VERSION]")
implementation("org.jetbrains.kotlinx:kotlinx-datetime:[VERSION]")
```

In `commonTest` dependencies:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:[VERSION]")
```

**Latest versions (Dec 2025):**
- Coroutines: 1.9.30
- Serialization: 1.8.0
- Datetime: 0.6.1

### Step 3: Update AndroidX Libraries

**File:** `androidApp/build.gradle.kts`

```kotlin
dependencies {
    implementation("androidx.activity:activity-compose:[VERSION]")
    implementation("androidx.core:core-ktx:[VERSION]")
    implementation(platform("androidx.compose:compose-bom:[VERSION]"))

    // Compose dependencies (versions managed by BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
}
```

**Latest versions (Dec 2025):**
- activity-compose: 1.10.0
- core-ktx: 1.15.0
- compose-bom: 2024.12.01

### Step 4: Validate Phase 2

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew :shared:test

# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Test all platforms
./gradlew :desktopApp:package
./gradlew :webApp:jsBrowserDevelopmentWebpack
```

**Check for:**
- Deprecated Compose API warnings
- Serialization format changes
- Coroutines behavior changes

**Commit Phase 2:**
```bash
git add .
git commit -m "chore: upgrade frameworks - Compose [VERSION], Kotlinx libs"
```

---

## Phase 3: Backend SDK Updates

⚠️ **WARNING:** This phase has breaking changes. Review code carefully.

### Step 1: Upgrade Ktor

**File:** `shared/build.gradle.kts`

Update all Ktor dependencies:

```kotlin
val ktorVersion = "[VERSION]" // e.g., "3.3.3"

// Common
implementation("io.ktor:ktor-client-core:$ktorVersion")
implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
implementation("io.ktor:ktor-client-websockets:$ktorVersion")
implementation("io.ktor:ktor-client-logging:$ktorVersion")

// Platform-specific (in respective source sets)
implementation("io.ktor:ktor-client-android:$ktorVersion") // androidMain
implementation("io.ktor:ktor-client-cio:$ktorVersion")     // jvmMain
implementation("io.ktor:ktor-client-js:$ktorVersion")      // jsMain
implementation("io.ktor:ktor-client-darwin:$ktorVersion")  // iosMain
```

**Breaking changes in Ktor 3.x:**
- Client configuration syntax changed
- Some plugin installation methods updated
- Package reorganization

**Migration guide:** https://ktor.io/docs/migrating-3.html

### Step 2: Upgrade Supabase SDK

⚠️ **MAJOR VERSION CHANGE:** 2.x → 3.x has breaking changes

**File:** `shared/build.gradle.kts`

**Before (2.x):**
```kotlin
implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.4")
implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.4")
implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.4")
implementation("io.github.jan-tennert.supabase:storage-kt:2.0.4")
```

**After (3.x):**
```kotlin
implementation(platform("io.github.jan-tennert.supabase:bom:[VERSION]"))
implementation("io.github.jan-tennert.supabase:auth-kt") // renamed!
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")
implementation("io.github.jan-tennert.supabase:storage-kt")
```

**Breaking changes:**
1. Module renamed: `gotrue-kt` → `auth-kt`
2. Requires Ktor 3.x
3. Minimum Android SDK increased: 24 → 26

**Code changes required:**

**File:** `shared/src/commonMain/kotlin/com/loveletter/network/AuthRepository.kt`

```kotlin
// Before
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

// After
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
```

Search and replace in all files:
- `io.github.jan.supabase.gotrue` → `io.github.jan.supabase.auth`

**Migration guide:** https://github.com/supabase-community/supabase-kt

### Step 3: Update Android minSdk (if upgrading to Supabase 3.x)

**Files:** `androidApp/build.gradle.kts` and `shared/build.gradle.kts`

```kotlin
android {
    defaultConfig {
        minSdk = 26  // Was 24, now required by Supabase 3.x
    }
}
```

**Impact:** Drops Android 7.0 support, still covers 99.5% of devices

### Step 4: Validate Phase 3

```bash
# Full clean build
./gradlew clean build

# Run all tests
./gradlew test

# Build all platforms
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:package
./gradlew :webApp:jsBrowserDevelopmentWebpack
```

**Manual testing required:**
- [ ] Test user authentication (signup/login)
- [ ] Test realtime features (game updates)
- [ ] Test database operations (CRUD)
- [ ] Verify no console errors

**Commit Phase 3:**
```bash
git add .
git commit -m "chore: upgrade backend SDKs - Supabase [VERSION], Ktor [VERSION]

BREAKING CHANGES:
- Supabase gotrue-kt renamed to auth-kt
- Updated import statements in AuthRepository
- Minimum Android SDK bumped to 26"
```

---

## Optional: Create Version Catalog

To simplify future upgrades, create a centralized version catalog.

**File:** `gradle/libs.versions.toml` (create new file)

```toml
[versions]
kotlin = "2.3.0"
compose = "1.9.3"
agp = "8.13.2"
gradle = "9.2.1"
ktor = "3.3.3"
supabase = "3.2.3"
coroutines = "1.9.30"
serialization = "1.8.0"
datetime = "0.6.1"

[libraries]
# Kotlinx
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-websockets = { module = "io.ktor:ktor-client-websockets", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# Supabase
supabase-bom = { module = "io.github.jan-tennert.supabase:bom", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt" }
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt" }
supabase-realtime = { module = "io.github.jan-tennert.supabase:realtime-kt" }
supabase-storage = { module = "io.github.jan-tennert.supabase:storage-kt" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
compose = { id = "org.jetbrains.compose", version.ref = "compose" }
```

**Update build.gradle.kts to use catalog:**

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
}

// shared/build.gradle.kts
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.client.core)
}
```

**Benefits:**
- Single source of truth for versions
- IDE autocomplete for dependencies
- Easier to update all related dependencies together

---

## Testing Strategy

### After Each Phase

Run these commands to validate:

```bash
# 1. Clean build
./gradlew clean build

# 2. Run tests
./gradlew test

# 3. Check for deprecation warnings
./gradlew build --warning-mode all

# 4. Platform-specific builds
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:package
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :webApp:jsBrowserDevelopmentWebpack
```

### Manual Testing Checklist

- [ ] **Authentication:** User signup, login, logout
- [ ] **Game Creation:** Create room, join room
- [ ] **Realtime Updates:** Live game state updates work
- [ ] **Database Operations:** CRUD operations function correctly
- [ ] **UI Rendering:** All screens render without crashes
- [ ] **Navigation:** Screen transitions work smoothly
- [ ] **Platform Compatibility:** Test on Android, Desktop, iOS simulator

---

## Common Issues & Solutions

### Issue: K2 Compiler Errors

**Symptom:** New compilation errors with Kotlin 2.x

**Solution:**
```properties
# In gradle.properties, temporarily disable K2
kotlin.experimental.tryK2=false
```

Then investigate specific errors. K2 is stricter, so code may need fixes.

### Issue: Gradle Build Cache Errors

**Symptom:** "Could not determine dependencies" or cache corruption

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean
./gradlew --stop
rm -rf ~/.gradle/caches/
./gradlew build
```

### Issue: Supabase Import Errors

**Symptom:** "Unresolved reference: gotrue"

**Solution:** Update imports:
```bash
# Find and replace across project
find shared/src -name "*.kt" -type f -exec sed -i 's/io.github.jan.supabase.gotrue/io.github.jan.supabase.auth/g' {} +
```

### Issue: Ktor Client Configuration Changed

**Symptom:** "Unresolved reference" in Ktor config

**Solution:** Review Ktor 3.x migration guide and update DSL:
https://ktor.io/docs/migrating-3.html

### Issue: Android Build Tools Missing

**Symptom:** "Failed to find target with hash string 'android-34'"

**Solution:**
```bash
# Update Android SDK through Android Studio
# Or via command line:
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

---

## Rollback Procedure

If upgrade fails and you need to rollback:

### Option 1: Git Rollback

```bash
# Rollback to before upgrade
git reset --hard upgrade-backup

# If you committed phases separately, rollback specific phase
git revert <commit-hash>
```

### Option 2: Manual Rollback

1. Restore `gradle/wrapper/gradle-wrapper.properties`
2. Restore version numbers in `build.gradle.kts`
3. Restore dependency versions in module build files
4. Run `./gradlew clean build`

---

## Version Discovery

### Finding Latest Versions

**Kotlin:**
- https://kotlinlang.org/docs/releases.html
- https://github.com/JetBrains/kotlin/releases

**Gradle:**
- https://gradle.org/releases/

**Compose Multiplatform:**
- https://github.com/JetBrains/compose-multiplatform/releases

**Supabase Kotlin:**
- https://github.com/supabase-community/supabase-kt/releases
- https://mvnrepository.com/artifact/io.github.jan-tennert.supabase

**Ktor:**
- https://ktor.io/docs/releases.html
- https://github.com/ktorio/ktor/releases

**AndroidX:**
- https://developer.android.com/jetpack/androidx/releases

### Version Compatibility Checker

Before upgrading, check compatibility:

```bash
# Check current versions
./gradlew -q dependencies --configuration runtimeClasspath
```

---

## CI/CD Considerations

### Update GitHub Actions

If using GitHub Actions, update workflow files:

**File:** `.github/workflows/ci.yml`

```yaml
- name: Set up JDK
  uses: actions/setup-java@v4
  with:
    java-version: '17'  # Update if changing Java version

- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v3
```

### Update Documentation

After successful upgrade:

1. Update `README.md` with new version requirements
2. Update this `UPGRADE_GUIDE.md` with current versions
3. Document any breaking changes in `CHANGELOG.md`

---

## Best Practices

### DO:
✅ Upgrade in phases, not all at once
✅ Commit after each successful phase
✅ Test thoroughly after each phase
✅ Review migration guides for major version bumps
✅ Check compatibility matrix before upgrading
✅ Update documentation after upgrade
✅ Notify team of breaking changes

### DON'T:
❌ Skip testing between phases
❌ Upgrade production dependencies on Friday afternoon
❌ Mix dependency upgrades with feature development
❌ Ignore deprecation warnings
❌ Upgrade without reading changelogs
❌ Forget to update CI/CD pipelines

---

## Quick Reference

### Current Project Structure

```
love-letter/
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties    # Gradle version
├── build.gradle.kts                     # Root plugins
├── gradle.properties                    # Kotlin compiler flags
├── androidApp/build.gradle.kts          # Android config
├── shared/build.gradle.kts              # Main dependencies
├── desktopApp/build.gradle.kts          # Desktop config
└── webApp/build.gradle.kts              # Web config
```

### Key Files to Check

| File | What to Update |
|------|----------------|
| `gradle-wrapper.properties` | Gradle version |
| `gradle.properties` | Kotlin compiler flags, JVM args |
| Root `build.gradle.kts` | Plugin versions (Kotlin, AGP, Compose) |
| `shared/build.gradle.kts` | All dependencies (Ktor, Supabase, Kotlinx) |
| `androidApp/build.gradle.kts` | Compose compiler, AndroidX, minSdk |

---

## Appendix: Full Upgrade Checklist

Copy this checklist for your next upgrade:

**Pre-Upgrade:**
- [ ] Create backup branch
- [ ] Ensure CI is green
- [ ] Review compatibility matrix
- [ ] Read changelogs for all upgrades
- [ ] Notify team
- [ ] Allocate sufficient time

**Phase 1 - Foundation:**
- [ ] Update `gradle-wrapper.properties`
- [ ] Update root `build.gradle.kts` plugins
- [ ] Update `gradle.properties` if needed
- [ ] Validate: `./gradlew clean build`
- [ ] Commit Phase 1

**Phase 2 - Frameworks:**
- [ ] Update Compose compiler version
- [ ] Update Kotlinx libraries
- [ ] Update AndroidX libraries
- [ ] Validate: `./gradlew test`
- [ ] Build all platforms
- [ ] Commit Phase 2

**Phase 3 - Backend:**
- [ ] Update Ktor dependencies
- [ ] Update Supabase dependencies
- [ ] Update imports (gotrue → auth)
- [ ] Update minSdk if required
- [ ] Validate: Full test suite
- [ ] Manual testing
- [ ] Commit Phase 3

**Post-Upgrade:**
- [ ] Update README
- [ ] Update this guide with new versions
- [ ] Update CHANGELOG
- [ ] Verify CI still passes
- [ ] Notify team of completion
- [ ] Monitor for issues

---

**Last Updated:** December 2025
**Maintained By:** Development Team
**Questions?** Check project README or create an issue
