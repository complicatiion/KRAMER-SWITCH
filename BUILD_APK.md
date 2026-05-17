# Build APK

## Android Studio

1. Open the project folder `KRAMER-SWITCH` in Android Studio.
2. Wait for **Gradle Sync** to finish.
3. Select the `app` configuration.
4. Use **Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK will be generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Requirements

- Android Studio version compatible with Android Gradle Plugin 8.13.x
- Gradle 8.13 or newer
- JDK 17
- Android SDK Platform 35 and Build Tools 35.x
- Internet access for first Gradle dependency sync

## Notes

- The app requires the Android `INTERNET` permission for TCP socket control.
- The original SVG logo is stored under `app/src/main/assets/` so Android resource compilation only processes the valid Vector Drawable XML under `res/drawable/`.
