# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform + Compose Multiplatform app targeting Android and iOS. Application id / namespace: `com.disheveled.dailyquotes`. Currently a fresh KMP wizard scaffold (Greeting/Platform demo) — no domain code yet.

Toolchain (from `gradle/libs.versions.toml`): Kotlin 2.3.20, Compose Multiplatform 1.10.3, Material3 1.10.0-alpha05, AGP 8.13.2, JVM target 11. Android `minSdk` 29, `compileSdk` / `targetSdk` 36.

## Commands

All Gradle commands run from the repo root via the wrapper.

```bash
./gradlew :composeApp:assembleDebug          # Android debug APK
./gradlew :composeApp:installDebug           # install on connected Android device/emulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # build iOS sim framework

./gradlew :composeApp:testDebugUnitTest      # Android JVM unit tests (commonTest + androidUnitTest)
./gradlew :composeApp:iosSimulatorArm64Test  # iOS sim tests (commonTest + iosTest)
./gradlew :composeApp:allTests               # all KMP test targets

# single test
./gradlew :composeApp:testDebugUnitTest --tests "com.disheveled.dailyquotes.ComposeAppCommonTest.example"
```

iOS app build/run: open `iosApp/iosApp.xcodeproj` in Xcode and use the iosApp scheme. Xcode invokes Gradle to produce the `ComposeApp` framework as a build phase; do not commit the generated `.framework` in `composeApp/build/`. Only `iosArm64` and `iosSimulatorArm64` targets are configured — there is no `iosX64` (Intel sim) target.

## Architecture

Single Gradle module `:composeApp` (see `settings.gradle.kts`) with KMP source sets:

- `commonMain` — shared Compose UI and logic. Entry point is `@Composable fun App()` in `App.kt`. Compose resources live under `commonMain/composeResources/` and are accessed via the generated `dailyquotes.composeapp.generated.resources.Res` object.
- `androidMain` — `MainActivity` calls `setContent { App() }`. Android-only deps (`activity-compose`, `ui-tooling-preview`) declared here.
- `iosMain` — `MainViewController()` wraps `App()` in a `ComposeUIViewController`. Builds a static framework named `ComposeApp` consumed by the Swift app.
- `commonTest` — uses `kotlin.test`.

Platform abstraction uses Kotlin `expect`/`actual`:

- `Platform.kt` (commonMain) declares `interface Platform` and `expect fun getPlatform()`.
- `Platform.android.kt` and `Platform.ios.kt` provide `actual` implementations. Add new platform-specific APIs by following this pattern — declare `expect` in commonMain, then implement in both `androidMain` and `iosMain`.

iOS host app (`iosApp/`) is a thin SwiftUI wrapper: `iOSApp.swift` → `ContentView` → `ComposeView: UIViewControllerRepresentable` calls `MainViewControllerKt.MainViewController()` from the generated `ComposeApp` framework. Add Kotlin-side iOS entry points by exposing top-level functions in `iosMain` — they're reachable from Swift as `<FileName>Kt.<func>()`.

Dependencies are managed via the `gradle/libs.versions.toml` version catalog and referenced as `libs.*` in `composeApp/build.gradle.kts`. Add new libraries to the catalog rather than hardcoding coordinates. `settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS`.
