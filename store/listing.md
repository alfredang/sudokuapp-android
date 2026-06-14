# Google Play — Store Listing & Submission Pack

App: **Sudoku** · Package: `com.tertiaryinfotech.sudokuapp` · Version 1.0 (versionCode 1)
Developer: Tertiary Infotech

---

## 1. Store listing text

**App name** (max 30)
```
Sudoku
```

**Short description** (max 80)
```
Classic Sudoku with 4 levels, smart hints, pencil notes & offline play. No ads.
```

**Full description** (max 4000)
```
Sudoku is a clean, native number puzzle built for focus and calm. Every puzzle is
generated right on your device with a guaranteed unique solution — so there's always
exactly one correct answer, and never a guess.

FOUR DIFFICULTY LEVELS
• Easy — a gentle warm-up
• Medium — a balanced challenge
• Hard — for seasoned solvers
• Expert — ruthless; good luck

BUILT FOR REAL SOLVING
• Pencil notes (candidates) in every cell
• Smart hints when you're stuck
• Conflict highlighting and same-number highlighting
• Auto-remove pencil marks as you place numbers
• Optional "3 strikes" mistake limit for an extra challenge
• Undo, erase, and a one-tap pause

TRACK YOUR PROGRESS
• Score every solve — faster, hint-free, mistake-free games score highest
• Best times per difficulty and a full game history
• Continue exactly where you left off

PRIVATE BY DESIGN
• 100% offline — no internet permission required
• No ads, no tracking, no accounts
• Your scores and history stay on your device and never leave it

Train your brain, one grid at a time.
```

**Category:** Games › Puzzle
**Tags:** sudoku, puzzle, number, brain, logic
**Contact email:** angch@tertiaryinfotech.com
**Website / Privacy Policy URL:** (required — see §5)

---

## 2. Graphic assets (in store/assets/)

| Asset | Spec | File |
|-------|------|------|
| App icon | 512×512 PNG, 32-bit | `play_store_512.png` |
| Feature graphic | 1024×500 PNG/JPG | `feature_graphic_1024x500.png` |
| Phone screenshots | 1080×2400 PNG (min 2, max 8) | `screenshots/01-home.png`, `02-game.png`, `03-agegate.png` |

Play requires a minimum of 2 phone screenshots — we provide 3.

---

## 3. Content rating (IARC questionnaire)

This app mirrors the iOS build, which ships an in-app 18+ gate. On Play the IARC
questionnaire drives the actual rating. Answer truthfully for a pure logic game:
- No violence, sexuality, profanity, drugs, gambling, or user-generated content.
- This will typically yield **Everyone / PEGI 3**.

Note: the iOS App Store rating was set to 18+ manually. On Play you cannot freely
override IARC's computed rating. If an 18+ presentation is desired, keep the in-app
age gate (already implemented) — but the Play content rating itself will reflect the
questionnaire answers. Recommend rating the game honestly (Everyone) and keeping the
age gate as an in-app courtesy.

---

## 4. Data safety form

- Does your app collect or share any user data? **No.**
- Is all user data encrypted in transit? N/A (no data leaves the device).
- Do you provide a way to request data deletion? N/A (data is local; Settings →
  Statistics → clear history removes it).

The app declares **no permissions** and has **no internet access**.

---

## 5. Required before you can submit (account-side, one-time)

1. **Privacy policy URL** — Play requires a public privacy-policy link even for
   no-data apps. A copy is drafted at `store/privacy-policy.md`; host it anywhere
   public (GitHub Pages, your site) and paste the URL in the listing.
2. **App content** declarations in Play Console: Privacy policy, Ads (None),
   Content rating (IARC), Target audience, Data safety, Government apps (No),
   Financial features (None).
3. **App access** — all functionality is available without login: choose
   "All functionality is available without special access".

---

## 6. Step-by-step: create & submit on Play Console

Prereq: a Google Play **Developer account** (one-time US$25, already paid per the
brief) at https://play.google.com/console.

1. **Create app**: Console → *Create app*. Name "Sudoku", language English (US),
   App, Free, accept declarations.
2. **Store listing** (Grow → Store presence → Main store listing): paste §1 text,
   upload §2 assets.
3. **App content** (Policy → App content): complete every item in §3–§5.
4. **Set up release signing**: Release → Setup → *App integrity* → let Google manage
   the app signing key (Play App Signing — recommended). Our `upload-keystore.jks`
   is the **upload key**; Google re-signs with the app key.
5. **Internal testing track first** (Release → Testing → Internal testing):
   *Create new release* → upload `app/build/outputs/bundle/release/app-release.aab`
   → add yourself as a tester → roll out. Install via the opt-in link to smoke-test.
6. **Production**: Release → Production → *Create new release* → upload the same AAB
   (or promote the internal release) → set release name "1.0 (1)" → add release
   notes → *Review release* → start rollout.
7. First production submissions go through Google review (hours–days for a new
   account). You'll get an email when it's live.

---

## 7. Optional: automate uploads via the Play Developer API

Initial app creation is manual (the API can't create a new listing). After the app
exists you can automate future AAB uploads:

1. Play Console → *Setup → API access* → link a Google Cloud project → create a
   **service account** → grant it "Release manager" on this app.
2. Download the service-account JSON key (store it outside the repo / in your secrets
   manager — never commit it).
3. Use `fastlane supply` or Google's `androidpublisher` API to push
   `app-release.aab` to a track. Example (fastlane):
   ```
   fastlane supply --aab app/build/outputs/bundle/release/app-release.aab \
     --track internal --json_key /path/to/service-account.json \
     --package_name com.tertiaryinfotech.sudokuapp
   ```

Tell me when the service-account JSON is in place and I'll wire up the upload command.
