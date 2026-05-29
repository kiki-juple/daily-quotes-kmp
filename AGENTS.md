# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project

Kotlin Multiplatform + Compose Multiplatform app ("Renung") targeting Android and iOS. Application id / namespace: `com.disheveled.dailyquotes`. Backed by the [FavQs API](https://favqs.com/api) for daily quotes and user authentication.

Toolchain (`gradle/libs.versions.toml`): Kotlin 2.3.21, Compose Multiplatform 1.11.0, Material3 1.11.0-alpha07, AGP 8.13.2, JVM target 11. Android `minSdk` 29, `compileSdk` / `targetSdk` 37.

## Commands

All Gradle commands run from the repo root via the wrapper.

```bash
./gradlew :composeApp:assembleDebug          # Android debug APK
./gradlew :composeApp:installDebug           # install on connected Android device/emulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64   # build iOS sim framework

# Tests live in :shared:data:commonTest (unit tests for repositories)
./gradlew :shared:data:testDebugUnitTest     # run data-layer tests on JVM
./gradlew :composeApp:testDebugUnitTest      # run composeApp tests on JVM

# single test
./gradlew :shared:data:testDebugUnitTest --tests "com.disheveled.dailyquotes.data.repository.AuthRepositoryTest.loginSuccessPopulatesUserAndPersistsSession"
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode and use the iosApp scheme. Only `iosArm64` and `iosSimulatorArm64` targets are configured — there is no `iosX64` (Intel sim) target.

## Module Structure

Four Gradle modules (see `settings.gradle.kts`):

| Module            | Role                                                                           |
|-------------------|--------------------------------------------------------------------------------|
| `:composeApp`     | Compose UI, ViewModels, navigation, DI root (`initKoin`)                       |
| `:shared:network` | Ktor HTTP client, `FavQsApi`, DTOs, `SessionStore` (multiplatform-settings)    |
| `:shared:local`   | SQLDelight database (`DailyQuotesDatabase`), `SqlDriverProvider` expect/actual |
| `:shared:data`    | Domain models (`Quote`, `User`), repositories, Koin `dataModule`               |

Dependency graph: `:composeApp` → `:shared:data` → `:shared:network` + `:shared:local`.

Add new libraries to `gradle/libs.versions.toml` and reference them as `libs.*` in build files. `settings.gradle.kts` enables `TYPESAFE_PROJECT_ACCESSORS` so modules reference each other as `projects.shared.network`.

## Architecture

### Layered within `:composeApp`
- `App.kt` — root `@Composable`, wraps everything in `DailyQuotesTheme` and delegates to `AppNavHost`.
- `ui/nav/` — navigation with Navigation 3 (`NavBackStack`, `NavKey`, `entryProvider`). `AppNavHost` reads `AuthRepository.currentUser` flow to choose between `AuthNavHost` (Login → Register) and `MainNavHost` (Home / Favorites with bottom `NavBar`). Destinations are `sealed interface` `NavKey` subtypes in `Screen.kt`.
- `ui/<feature>/` — each screen has a paired `ViewModel` that consumes repositories and exposes `StateFlow`.
- `ui/components/` — shared Renung design-system components.
- `di/AppModule.kt` — calls `initKoin { }`, registers ViewModels as `viewModelOf`, and pulls in `sharedDataModules()` from `:shared:data`.

### Android entry point
`DailyQuotesApp : Application` calls `initAndroidPlatform(this)` then `initKoin { androidContext(...) }`. `MainActivity` calls `setContent { App() }`.

### iOS entry point
`MainViewController.kt` (iosMain) exposes `MainViewController()` — called from Swift as `MainViewControllerKt.MainViewController()`.

### Data layer (`:shared:data`)
- `AuthRepository` — manages login/register via `FavQsApi`, persists the session token + login in `SessionStore`, and exposes `currentUser: StateFlow<User?>` used by navigation to gate the auth wall.
- `QuoteRepository` — fetches quote-of-the-day from the API, caches it in SQLDelight (`QuoteOfTheDay` table keyed by date), and evicts stale entries.
- `FavoritesRepository` — CRUD for locally-stored favorite quotes (`FavoriteQuote` table).
- `resultOf { }` helper wraps suspend calls in `Result<T>`, rethrowing `CancellationException`.

### Network (`:shared:network`)
- `FavQsApi` — typed Ktor client wrapper for `GET /qotd`, `POST /users`, `POST /session`, `GET /users/{login}`.
- `SessionStore` — reads/writes user token and login from `multiplatform-settings` (`MapSettings` in tests, platform-native in production).
- Platform-specific HTTP engines: OkHttp (Android) / Darwin (iOS) via `HttpClientEngineProvider` expect/actual.
- API key injected at build time into `GeneratedApiKey.kt` via a Gradle task in `:shared:network`.

### Local storage (`:shared:local`)
- SQLDelight schema in `shared/local/src/commonMain/sqldelight/`. Database name: `DailyQuotesDatabase`.
- `SqlDriverProvider` expect/actual: `AndroidSqliteDriver` on Android, `NativeSqliteDriver` on iOS.

## Design System (Renung)

- `RenungColors` — semantic palette: Paper/Ink/Clay/Sage/Mist tokens (no raw `Color` values in UI code).
- `ui/theme/` — `DailyQuotesTheme`, `RenungColors`, `RenungSpacing`, `RenungShapes`, `RenungTypography`.
- Compose resources (icons, strings) live under `commonMain/composeResources/` and are accessed via the generated `Res` object.

## Testing

Tests for repositories live in `:shared:data:commonTest`. They use `ktor-client-mock` (`MockEngine`) and `multiplatform-settings-test` (`MapSettings`) — no real database or network. No SQLDelight in the test source set; repositories under test receive a real `FavQsApi` wired to a `MockEngine`.