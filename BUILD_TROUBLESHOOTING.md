# Build Troubleshooting

## Error: Unresolved reference Column

`Column` is part of Jetpack Compose Foundation Layout. The source must contain either:

```kotlin
import androidx.compose.foundation.layout.Column
```

or the broader layout import used in this project:

```kotlin
import androidx.compose.foundation.layout.*
```

After changing imports:

1. File -> Sync Project with Gradle Files
2. Build -> Clean Project
3. Build -> Rebuild Project
4. Build -> Generate Signed Bundle / APK

If the error remains, delete the local `build/` folders and run Invalidate Caches / Restart.
