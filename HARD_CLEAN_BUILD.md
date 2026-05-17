# Hard Clean Build

Do not copy this project over an older folder. Extract it into a completely new directory.

Recommended Windows steps:

```powershell
# Close Android Studio first
cd C:\Users\sk\Downloads
Rename-Item .\Karma-Switch .\Karma-Switch_old -ErrorAction SilentlyContinue
# Extract this ZIP fresh, then open the extracted KRAMER-SWITCH folder via Android Studio > File > Open
```

If you keep the same folder, remove build/caches first:

```powershell
Remove-Item -Recurse -Force .\.gradle, .\build, .\app\build, .\.idea -ErrorAction SilentlyContinue
Get-ChildItem -Recurse -Filter MainActivity.kt | Select-Object FullName
Select-String -Path .\app\src\main\java\de\sksdesign\kramerswitch\MainActivity.kt -Pattern "ColumnScope|GlassCard|foundation.layout.Column"
```

Expected source line:

```kotlin
private fun GlassCard(content: @Composable ColumnScope.() -> Unit)
```
