# MedSymptomTracker

A Kotlin Android application for tracking medical symptoms, medications, vital signs, and reminders. Built for an education platform using modern Android architecture components.

## Architecture Overview

The project follows a clean architecture pattern with three main layers:

- **Domain layer** (`domain/model/`): Pure Kotlin data classes and enums representing core business models — `Symptom`, `Medication`, `VitalSign`, `Reminder`, and associated enums (`Severity`, `VitalType`, `FrequencyUnit`, `ReminderType`).
- **Data layer** (`data/`): Room entities with bidirectional mapping to/from domain models, DAOs, and repository implementations backed by an encrypted SQLCipher database.
- **DI layer** (`di/`): Hilt modules wiring the database, DAOs, and repositories for injection throughout the app.
- **Worker layer** (`worker/`): `SyncWorker` (a `CoroutineWorker`) handles periodic background sync; `BootReceiver` re-enqueues the worker after device reboot.

## How to Build

```bash
./gradlew assembleDebug --no-daemon
```

The debug APK is produced at: `app/build/outputs/apk/debug/app-debug.apk`

## How to Run Tests

```bash
./gradlew test --no-daemon
```

Unit tests cover entity round-trip mapping, repository behaviour, domain model invariants, and WorkManager constraint configuration.

## Encryption Strategy

All data at rest is encrypted using **SQLCipher** (`net.zetetic:android-database-sqlcipher:4.5.4`). On first launch, a 32-byte random passphrase is generated via `SecureRandom` and stored in **EncryptedSharedPreferences** (AES-256-GCM). The passphrase is retrieved on subsequent launches and passed to Room via `SupportFactory`. This ensures the database file is unreadable without the key, and the key itself is protected by the Android Keystore through `MasterKey`.

## WorkManager Sync Strategy

`SyncWorker` runs as a **periodic worker** (every 6 hours) and requires an active network connection (`NetworkType.CONNECTED`). It queries unsynced records from the local database and marks them synced after processing. The worker is:

- Enqueued on app start via `MedTrackerApp.onCreate()` using `ExistingPeriodicWorkPolicy.KEEP` (so re-launches do not create duplicate workers).
- Re-enqueued after device reboot via `BootReceiver` listening for `ACTION_BOOT_COMPLETED`.
- Built with Hilt injection support via `@HiltWorker` and a custom `HiltWorkerFactory` registered through `Configuration.Provider`.

## Tech Stack

- Kotlin 1.9.22, AGP 8.2.2
- Jetpack Compose (BOM 2024.02.00) + Material 3
- Room 2.6.1 + KSP
- Hilt 2.50 + Hilt-Work 1.2.0
- WorkManager 2.9.0
- SQLCipher 4.5.4 + EncryptedSharedPreferences
- kotlinx-datetime 0.5.0
- ktlint 12.1.0
