# Smart Clipboard Manager

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
  - Multiline text
  - Possible code snippet
- Quick actions per type (open URL, call number, compose e-mail)
- Privacy-first behavior:
  - Sensitive content marking
  - OTP not persisted by default
  - Sensitive preview masking
  - Retention-based auto-cleanup
- UI screens:
  - Home
  - History
  - Detail
  - Settings
  - Help
  - Info
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

## Architecture

Version 1 uses a lean, package-based architecture inside a single `app` module:

- `domain/*`
- `data/*`
- `ui/*`
- `di/*`

## Build

### Requirements

- Android Studio (recent stable)
- JDK 17+
- Android SDK configured (`local.properties`)

### Commands

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Run on Device

1. Enable Developer Options + USB debugging.
2. Connect device via USB.
3. Install debug build:
   - `./gradlew installDebug`
4. Launch app from launcher.

## License

MIT License

(c) Günther Meusburger

## Credits

CLI development support tools:

- Codex
- Claude Code
- Gemini
