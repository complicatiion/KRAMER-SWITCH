## v1.0.5

- Switched all in-app UI labels and status messages to English
- Improved action button layout for better readability
- Increased clarity of action labels: Test Connection, Refresh Status, Power Off
- Improved dark theme text visibility inside settings fields and label fields
- Improved visibility of outlined settings buttons

# Changelog

## 1.0.4
- Fixed Compose type error: `GlassCard` now uses `ColumnScope` instead of invalid `Column` receiver type.
- Added explicit `ColumnScope` import for Android Studio/Kotlin compiler.
- Version bumped to 1.0.4.

# Changelog

## 1.0.0

- Initial Android Studio project
- Native Kotlin / Jetpack Compose app
- Kramer VS-81H Protocol 2000 switch control
- Settings dialog with IP, port, timeout, machine number and input labels
- Connection test, status refresh, OFF command
- App reset and cache clear actions
- Protocol unit tests
## 1.0.2
- Hotfix: `MainActivity.kt` uses `androidx.compose.foundation.layout.*` import to avoid unresolved Compose layout references such as `Column` during local Android Studio builds.
- VersionCode bumped to 2 and VersionName to 1.0.2.

## 1.0.3
- Added explicit Compose Column import in MainActivity.kt to avoid unresolved reference build errors when older project folders are reused.
- VersionCode increased to 3.
