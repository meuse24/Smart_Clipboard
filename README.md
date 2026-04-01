# M24 Smart Clipboard

SmartClipboardManager is a local-first Android clipboard assistant built with Kotlin and Jetpack Compose.

## Features

- Active clipboard import in foreground (no accessibility hacks)
- Local clipboard history with search
- Content classification:
  - URL
  - Phone number
  - E-mail
  - OTP
  - JSON
  - IBAN
  - Color (Hex / RGB / HSL)
  - Geo coordinates (decimal degrees)
  - Image (copied from any app)
  - File
  - Multiline text
  - Code snippet
- Quick actions per type: open URL, call number, compose e-mail, open Maps, open file, image / color preview
- Privacy-first behavior:
  - Sensitive content marking
  - OTP not persisted by default
  - Sensitive preview masking
  - Retention-based auto-cleanup (including media files)
- Modern UI with bottom navigation (Home · History · Settings)
- UI screens: Home, History, Detail, Settings, Help, Info
- Quick Settings Tile entry point
- Share / Process Text integration

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Hilt (DI)
- Room (local database)
- DataStore Preferences (settings)
- Coroutines + Flow
- Coil (image loading)

## Architecture

Package-based clean architecture inside a single `app` module:

- `domain/*` — models, use cases, repository interfaces
- `data/*` — Room DAO/entities, repository implementations, media helpers
- `ui/*` — Compose screens, ViewModels, navigation
- `di/*` — Hilt modules

## Build

### Requirements

- Android Studio (recent stable)
- JDK 17+
- Android SDK configured (`local.properties`)

### Commands

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew installDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat installDebug
```

## Run on Device

1. Enable Developer Options + USB debugging.
2. Connect device via USB.
3. Install debug build: `./gradlew installDebug`
4. Launch app from launcher.

## License

MIT License

(c) Günther Meusburger

## Credits

CLI development support: Codex, Claude Code, Gemini
