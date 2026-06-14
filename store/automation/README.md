# Play Store upload automation

Automated AAB uploads to Google Play via the Play Developer API. **The app must
already exist in Play Console** — the API cannot create a new listing, accept
agreements, or fill the content-rating / data-safety forms. Do those once in the UI
(see `../listing.md`), then use this for every release.

## One-time setup

1. **Create the app in Play Console** and complete the "App content" declarations
   (`../listing.md` §3–§6). Upload the first AAB manually or with the script below.
2. **Link a Google Cloud project & service account**
   - Play Console → *Setup → API access* → link/create a Google Cloud project.
   - Create a **service account** (Cloud Console → IAM → Service Accounts).
   - Back in Play Console → *API access* → grant that service account access, with
     the **Release manager** role for this app (Admin also works).
3. **Download the service-account JSON key** (Cloud Console → the service account →
   Keys → Add key → JSON). Save it somewhere private, e.g.
   `~/.secrets/play-sudoku.json`. **Never commit it** (already covered by .gitignore
   patterns for `*.json` keys — keep it outside the repo to be safe).

Dependencies are already present on this machine (`google-auth`,
`google-api-python-client`). If on a fresh machine:
```bash
pip3 install google-auth google-api-python-client
```

## Upload a build

From `store/automation/`:
```bash
# Build the AAB first (from the project root):
#   JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
#   ./gradlew :app:bundleRelease

python3 upload_to_play.py \
  --json-key ~/.secrets/play-sudoku.json \
  --aab ../../app/build/outputs/bundle/release/app-release.aab \
  --package com.tertiaryinfotech.sudokuapp \
  --track internal \
  --release-name "1.0 (1)" \
  --notes "First release — native Android Sudoku."
```

Start with `--track internal` to smoke-test via the internal opt-in link, then
re-run with `--track production` (or promote in the UI) when ready.

## Bumping versions for future releases

Each upload needs a unique, higher `versionCode`. Edit `app/build.gradle.kts`:
```
versionCode = 2
versionName = "1.1"
```
rebuild the AAB, and re-run the script.

## Alternative: fastlane (also installed here)

`Fastfile` and `Appfile` in this folder provide a `fastlane release` lane that does
the same thing. Set the JSON key path in `Appfile` or via
`SUPPLY_JSON_KEY=~/.secrets/play-sudoku.json`.
