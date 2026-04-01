# CLAUDE.md — M24 Smart Clipboard

## Project Overview

Android clipboard assistant. Local-first, no accessibility service required. User actively imports clipboard content; the app classifies, stores and surfaces it.

## Build Commands

```bash
# Windows (use .bat wrapper)
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat installDebug
```

## Architecture

Clean architecture, single `app` module, package-based layout.

```
app/src/main/java/com/smartclipboardmanager/
├── data/
│   ├── local/          # Room DB (AppDatabase v3), DAO, entities
│   ├── mapper/         # Entity ↔ Domain mappers
│   ├── media/          # MediaStoreHelper (copy to internal storage, implements MediaDeleter)
│   └── repository/     # ClipboardRepositoryImpl, SettingsRepositoryImpl
├── di/                 # Hilt modules (DatabaseModule, RepositoryModule, UseCaseModule)
├── domain/
│   ├── classification/ # ClipboardClassifier + rules per ClipContentType
│   ├── media/          # MediaDeleter interface (testability without Context)
│   ├── model/          # ClipboardEntry, ClipContentType, AppSettings, ClipboardImportInput
│   ├── privacy/        # SensitiveContentPolicy, SensitiveContentRedactor
│   ├── repository/     # ClipboardRepository, SettingsRepository (interfaces)
│   └── usecase/        # ImportClipboardContentUseCase, ImportMediaContentUseCase,
│                       # CleanupOldEntriesUseCase
├── service/            # ClipboardImportTileService (Quick Settings tile)
├── ui/
│   ├── components/     # MediaPreview (image/color), QuickActionsRow
│   ├── navigation/     # AppNavHost (bottom nav: Home · History · Settings)
│   ├── screen/         # home/, history/, detail/, settings/, help/, info/
│   ├── theme/          # Material3 theme
│   └── viewmodel/      # HomeViewModel, HistoryViewModel, DetailViewModel
└── MainActivity.kt
```

## Key Conventions

- **Room DB version:** currently 3; bump version + add migration (or use `fallbackToDestructiveMigration`) for schema changes.
- **MediaDeleter interface:** `CleanupOldEntriesUseCase` depends on this interface (not `MediaStoreHelper`) for testability. Hilt binds `MediaStoreHelper → MediaDeleter` in `RepositoryModule`.
- **Media import:** `ImportMediaContentUseCase` copies clipboard URIs to `filesDir/clipboard_media/` immediately (URIs become invalid after clipboard clears). `ClipboardEntryEntity.mediaUri` stores the local path.
- **Atomic DAO operations:** Use `ClipboardDao.getMediaUrisAndDeleteOlderThan()` (`@Transaction` wrapper) — never call `getMediaUrisOlderThan` + `deleteOlderThan` separately.
- **Toast via SharedFlow:** `HomeViewModel.mediaImportResult: SharedFlow<Boolean>` — `MainActivity` observes it and shows the toast. Never call `Toast` from the ViewModel directly.
- **Color parsing:** `AndroidColor.parseColor` only handles hex. `MediaPreview.parseColor()` additionally handles `rgb()`, `rgba()`, `hsl()`, `hsla()`.
- **Bottom navigation:** `AppNavHost` wraps a `Scaffold` with a `NavigationBar`. Screens must not handle their own back navigation with the top-level nav bar entries.

## Supported Content Types (`ClipContentType`)

`OTP`, `IBAN`, `EMAIL`, `URL`, `JSON`, `COLOR`, `PHONE_NUMBER`, `GEO_LOCATION`, `CODE_SNIPPET`, `IMAGE`, `FILE`, `MULTI_LINE_TEXT`, `PLAIN_TEXT`

## Testing

Unit tests in `app/src/test/`. No Android framework dependency in unit tests.
- `ImportClipboardContentUseCaseTest` uses `InMemoryClipboardRepository` and `InMemorySettingsRepository`.
- `CleanupOldEntriesUseCase` tests pass `object : MediaDeleter { override fun deleteFile(path: String) {} }` as the no-op deleter.
- Privacy tests in `domain/privacy/`.

## Dependencies (notable)

| Library | Purpose |
|---|---|
| Room 2.6.x | Local clipboard database |
| DataStore Preferences | App settings |
| Hilt 2.50 | Dependency injection |
| Coil Compose 2.7.0 | Loading images from local file paths |
| Turbine | Flow testing |
| MockK | Unit test mocking |
