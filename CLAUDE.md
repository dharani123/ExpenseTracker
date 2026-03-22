# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build                  # Full build
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on connected device/emulator
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests on device/emulator
./gradlew app:testDebugUnitTest --tests "com.example.expensetracker.ExampleUnitTest"  # Run a single test class
```

## Architecture

Single-module Android app using **Jetpack Compose** with **Material3**. Built with Gradle Kotlin DSL and a version catalog (`gradle/libs.versions.toml`).

- **Compile/Target SDK:** 36, **Min SDK:** 24, **JVM target:** Java 11
- **Compose BOM:** 2024.09.00
- **UI:** Declarative Compose with dynamic color support (Android 12+), dark/light theme
- **Package:** `com.example.expensetracker`
- **Entry point:** `MainActivity.kt` — sets up Compose content with edge-to-edge rendering
- **Theme:** `ui/theme/` — Color.kt, Theme.kt, Type.kt define the Material3 theme

Currently a starter template with no DI, database, navigation, or networking layers.
