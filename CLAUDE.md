# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

A native Android Sudoku game for phones, built with **Kotlin + Jetpack Compose** to
ship on the Google Play Store. It is a faithful port of the SwiftUI iOS app in
`../../iOS/sudokuapp`. Puzzles are generated on-device with a guaranteed unique
solution. Four difficulty levels, smart hints, pencil notes, scoring, and full game
history kept locally. Ships with an in-app 18+ gate (mirrors the iOS build).

## Build & run

No system JDK is required — use Android Studio's bundled JBR:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :app:assembleDebug      # debug APK  -> app/build/outputs/apk/debug/
./gradlew :app:assembleRelease    # signed release APK (needs keystore.properties)
./gradlew :app:bundleRelease      # signed release AAB for Play -> app/build/outputs/bundle/release/
```

Install & launch on a device/emulator:

```bash
export PATH=$PATH:~/Library/Android/sdk/platform-tools
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.tertiaryinfotech.sudokuapp/.MainActivity
adb exec-out screencap -p > /tmp/shot.png
```

**Tests:** there is no instrumentation/unit test target yet. The engine
(`engine/SudokuEngine.kt`) is pure and self-contained — the quickest sanity check is
to assert each difficulty yields `solutionCount(puzzle) == 1`. Add a `test/` source
set if coverage is needed.

## Toolchain

- AGP 8.7.3, Gradle 8.11.1 (wrapper committed), Kotlin 2.0.21 (built-in Compose compiler).
- `compileSdk 36` (the only platform installed locally), `targetSdk 35`, `minSdk 24`.
  `android.suppressUnsupportedCompileSdk=36` silences the AGP-vs-SDK advisory.
- Versions are centralised in `gradle/libs.versions.toml`.

## Architecture

Lightweight MVVM, mirroring the iOS app. A single `GameViewModel`
(`AndroidViewModel`, Compose snapshot state) is the source of truth; the root
composable switches on an `AppScreen` enum instead of a NavHost.

```
app/src/main/java/com/tertiaryinfotech/sudokuapp/
  MainActivity.kt                @main Activity; auto-pauses the timer in onStop()
  engine/SudokuEngine.kt         pure logic: generate / solve / uniqueness (MRV backtracking)
  model/Difficulty.kt            easy/medium/hard/expert -> clues, par time, base score, icon, tint
  model/GameSession.kt           completed-game record + ActiveGame (resume snapshot), @Serializable
  model/AppScreen.kt             navigation enum + GameAlert
  util/ScoreCalculator.kt        score = base + speed bonus − hint/mistake penalties
  data/ScoreStore.kt             SharedPreferences + kotlinx.serialization JSON: sessions + active game + stats
  viewmodel/GameViewModel.kt     board state, timer (coroutine), hints, undo, settings, 18+ gate
  ui/
    theme/Theme.kt               Material 3 color scheme; accent matches iOS AccentColor
    RootScreen, AgeGateScreen, HomeScreen, GameScreen, BoardView,
    CompletionScreen, StatsScreen, SettingsScreen
  res/                           adaptive launcher icon, themes, backup rules
```

## Key behaviours (identical to iOS)

- **Generation**: fill a random solved grid, then carve out cells while a 2-solution
  uniqueness check still returns exactly 1. Clue targets: Easy 45 · Medium 36 · Hard 30 · Expert 25.
- **Hints**: `useHint()` fills the selected (or first unsolved) cell with the solution
  value and increments the hint tally, which lowers the final score.
- **Persistence**: every move saves an `ActiveGame` so the player can quit and
  **Continue**; finished games append a `GameSession`. Nothing leaves the device.
- **18+ gate**: `AgeGateScreen` shows once on first launch; the choice is stored in
  SharedPreferences (`SudokuApp.ageConfirmed`).
- Storage keys and JSON shape are kept compatible with the iOS app's intent (same
  `SudokuApp.*` key names) for conceptual parity, though the two apps don't share data.

## Conventions

- Phone-only, portrait only (`android:screenOrientation="portrait"`).
- No third-party dependencies beyond AndroidX/Compose + kotlinx-serialization; no
  network; no runtime permissions.
- Do **not** mix this project with the iOS app — they are separate codebases that
  share only design and behaviour.

## Release & Play Store

- Signing: `keystore.properties` (git-ignored) points at `upload-keystore.jks` (the
  Play **upload key** — back it up; do not commit). `app/build.gradle.kts` reads it
  to sign release builds. Enroll in Play App Signing so Google manages the app key.
- Store listing copy, graphic assets, content-rating/data-safety answers, and a full
  Play Console checklist live in `store/` (`listing.md`, `privacy-policy.md`,
  `assets/`). Regenerate the icon/feature graphic with the Java tools used in
  `/tmp/IconGen.java` and `/tmp/FeatureGraphic.java`.
