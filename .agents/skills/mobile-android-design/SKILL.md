---
name: mobile-android-design
description: Master Material Design 3 and Jetpack Compose patterns for building native Android apps. Use when designing Android interfaces, implementing Compose UI, or following Google's Material Design guidelines.
---

# Android Mobile Design

Master Material Design 3 (Material You) and Jetpack Compose to build modern, adaptive Android applications that integrate seamlessly with the Android ecosystem.

## When to Use This Skill

- Designing Android app interfaces following Material Design 3
- Building Jetpack Compose UI and layouts
- Implementing Android navigation patterns (Navigation Compose)
- Creating adaptive layouts for phones, tablets, and foldables
- Using Material 3 theming with dynamic colors
- Building accessible Android interfaces
- Implementing Android-specific gestures and interactions
- Designing for different screen configurations

## Detailed section: Core Concepts

Originally a 9201-byte section in this SKILL.md. Moved to `references/details.md` to fit Codex's 8 KB skill body cap.

## Quick Start Component

```kotlin
@Composable
fun ItemListCard(
    item: Item,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onItemClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

## This Project: Sudoku (Android)

This skill is installed in the **Sudoku** app — a native, offline Sudoku game
(`com.tertiaryinfotech.sudokuapp`) and a faithful port of the SwiftUI iOS edition.
Apply the guidance below to *this* codebase; where the generic best practices below
conflict with these project decisions, **the project decisions win**.

**Architecture (lightweight MVVM):** a single `GameViewModel` (`AndroidViewModel`,
Compose snapshot state) is the source of truth. There is **no NavHost** — the root
composable `ui/RootScreen.kt` switches on an `AppScreen` enum. New screens are added
to that enum and the `when` block, not to a navigation graph. The seven screens live
in `ui/`: `AgeGateScreen`, `HomeScreen`, `GameScreen` + `BoardView`,
`CompletionScreen`, `StatsScreen`, `SettingsScreen`.

**Theming (`ui/theme/Theme.kt`):** wrap everything in `SudokuTheme { }` and read
colors via `MaterialTheme.colorScheme`. This app deliberately uses a **fixed
iOS-matched palette** (accent `#2973F5` light / `#619EFA` dark, iOS-style grouped
backgrounds `#F2F2F7` / `#000000`) — **do NOT introduce Material You dynamic color**;
visual parity with iOS is the goal. Dark mode follows the system. The status/nav bars
are tinted to match the background in `SudokuTheme`.

**Layout scope:** phone-only and **portrait-only**
(`android:screenOrientation="portrait"`). **Skip** `WindowSizeClass`, tablet, and
foldable adaptive layouts — they are out of scope here.

**Components:** the `ItemListCard` Quick Start below mirrors the real Home-screen
rows (Continue / Easy / Medium / Hard / Expert) — leading tinted icon box, title +
subtitle column, trailing chevron. Reuse that pattern for new list rows. The 9×9
grid is a custom-drawn `BoardView`, not a `LazyColumn`.

**Constraints:** no third-party dependencies beyond AndroidX/Compose +
kotlinx-serialization; no network; no runtime permissions. Persistence is
SharedPreferences + kotlinx.serialization JSON via `data/ScoreStore.kt`. Keep the
pure `engine/SudokuEngine.kt` free of Android/Compose imports.

## Best Practices

1. **Use Material Theme**: Access colors via `MaterialTheme.colorScheme` for automatic dark mode support
2. **Support Dynamic Color**: Enable dynamic color on Android 12+ for personalization
3. **Adaptive Layouts**: Use `WindowSizeClass` for responsive designs
4. **Content Descriptions**: Add `contentDescription` to all interactive elements
5. **Touch Targets**: Minimum 48dp touch targets for accessibility
6. **State Hoisting**: Hoist state to make components reusable and testable
7. **Remember Properly**: Use `remember` and `rememberSaveable` appropriately
8. **Preview Annotations**: Add `@Preview` with different configurations

## Common Issues

- **Recomposition Issues**: Avoid passing unstable lambdas; use `remember`
- **State Loss**: Use `rememberSaveable` for configuration changes
- **Performance**: Use `LazyColumn` instead of `Column` for long lists
- **Theme Leaks**: Ensure `MaterialTheme` wraps all composables
- **Navigation Crashes**: Handle back press and deep links properly
- **Memory Leaks**: Cancel coroutines in `DisposableEffect`
