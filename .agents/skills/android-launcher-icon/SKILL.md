---
name: android-launcher-icon
description: Generate and fix the adaptive Android launcher icon for this Sudoku app (Kotlin/Compose). Use when the app icon looks wrong on a device — e.g. a washed-out white tile on Samsung/One UI, the blue background missing, the grid too small/large, or when regenerating launcher + Play Store icon art across all densities.
---

# Android Launcher Icon

Generate and repair the adaptive launcher icon for this app. Covers the adaptive-icon
foreground-sizing gotcha that makes the icon render differently across launchers (the
exact bug that made it look like a plain white tile on Samsung One UI), plus the
end-to-end regenerate → preview → install → verify workflow.

## When to Use This Skill

- The app icon looks wrong on a real device (white/washed-out tile, missing blue
  background, grid too small or cropped) even though it looks fine in Android Studio.
- Redesigning or recoloring the launcher icon.
- Regenerating the icon set across all densities, plus the Play Store 512/1024 art.
- A reviewer/user says "the icon doesn't seem correct."

## The core gotcha: adaptive-icon foreground sizing

The launcher icon is an **adaptive icon** ([app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml](app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)):
a blue gradient **background** layer ([ic_launcher_background.xml](app/src/main/res/drawable/ic_launcher_background.xml))
+ a **foreground** PNG (the white grid card) + a **monochrome** layer (themed icons).

The 108dp canvas is NOT all visible. Launchers crop to a mask and **scale the foreground
up to fill it** — and they disagree on how much:

- **Pixel / AOSP:** little or no upscale (~1.0×).
- **Samsung One UI:** aggressive upscale (~1.4×) to fill its squircle.

So a foreground whose content fills too much of the 108dp canvas gets blown up on
Samsung until the white card covers nearly all the blue background → the icon reads as
a **plain white tile**. This looked fine in the Studio preview (Pixel-style render) but
broke on the physical Samsung phone.

**Rule of thumb:** keep the foreground's visible artwork to **~0.50 of the 108dp canvas**
(comfortably inside the safe zone). After any launcher's upscale a healthy blue frame
survives and it matches the self-contained legacy `ic_launcher.png`. The legacy/Play
square icons are self-contained (no launcher mask) so they use a larger **0.72** panel.

In [scripts/IconGen.java](scripts/IconGen.java) this is the one line that matters:

```java
float panel = withBg ? size*0.72f : size*0.50f;  // legacy=0.72, adaptive foreground=0.50
```

If the icon ever looks like a white tile again, that `0.50` is almost certainly the
knob — lower it for more blue frame, raise it for a bigger card.

## Workflow

All commands use Android Studio's bundled JBR — no system JDK needed:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
SKILL=.agents/skills/android-launcher-icon
```

### 1. Regenerate the full icon set

Writes every density's `ic_launcher_foreground.png`, the legacy
`ic_launcher.png` / `ic_launcher_round.png`, and the Play art (512 + 1024):

```bash
cd "$SKILL/scripts"
"$JAVA_HOME/bin/javac" IconGen.java
"$JAVA_HOME/bin/java" IconGen \
  ../../../../app/src/main/res \
  ../../../../store/assets
```

(Adjust the relative paths to the repo `app/src/main/res` and `store/assets`.)

### 2. Preview what a launcher will actually render

`IconPreview` composites background + foreground, masks to a squircle, and renders at
**1.0× (AOSP)** and **1.4× (Samsung)** so you can catch the white-tile bug before
installing. Always check the **1.4×** output — that's the worst case.

```bash
"$JAVA_HOME/bin/javac" IconPreview.java
"$JAVA_HOME/bin/java" IconPreview ../../../../app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png
# wrote /tmp/preview_x1.0.png and /tmp/preview_x1.4.png — open and inspect both
```

A correct icon shows the white grid card with a clear blue frame on **both** scales.

### 3. Build, install, verify on a real device

```bash
export PATH=$PATH:~/Library/Android/sdk/platform-tools
./gradlew :app:assembleDebug

# Pick the physical device explicitly if several are attached (adb devices).
# A signature mismatch (release vs debug) needs an uninstall first — this CLEARS
# local game data on that device, so confirm with the user before doing it.
adb -s <DEVICE_ID> uninstall com.tertiaryinfotech.sudokuapp   # only if signatures differ
adb -s <DEVICE_ID> install -r app/build/outputs/apk/debug/app-debug.apk

# Launchers cache icons. Check the app-drawer icon (true current art); the old
# home-screen shortcut may stay cached until reboot / re-add.
adb -s <DEVICE_ID> shell input keyevent KEYCODE_HOME
adb -s <DEVICE_ID> shell input swipe 360 1400 360 300 300   # open app drawer
adb -s <DEVICE_ID> exec-out screencap -p > /tmp/drawer.png
```

### 4. Ship to Play

```bash
./gradlew :app:bundleRelease   # signed AAB -> app/build/outputs/bundle/release/
```

- Bump `versionCode` in [app/build.gradle.kts](app/build.gradle.kts) first — Play rejects a
  duplicate `versionCode`.
- The Play **store listing** icon (512×512) is a separate upload in the Console; refresh
  it from [store/assets/play_store_512.png](store/assets/play_store_512.png).

## Icon design reference (current art)

Defined in [scripts/IconGen.java](scripts/IconGen.java):

- Background: linear gradient `#4C84F6` (top-left) → `#163682` (bottom-right) + a soft
  top-left radial sheen.
- Foreground: white rounded panel, drop shadow, 3×3 grid; centre cell ("7") filled with
  accent blue `#2F6BE0`, white numeral.
- Numerals: dark slate `#2A446E`, blue `#2F6BE0`, orange `#F5A623` (per-cell `GRID` map).
- Monochrome layer for Android 13+ themed icons is the hand-authored vector
  [ic_launcher_monochrome.xml](app/src/main/res/drawable/ic_launcher_monochrome.xml) — a
  single-color grid glyph on transparent (regenerate it by hand, not via IconGen).
