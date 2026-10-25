# Building AR SAVE from Source

This document details the build prerequisites, dependency chain, and compilation instructions for **AR SAVE**.

---

## 🛠️ Prerequisites

- **Operating System**: Windows, macOS, or Linux
- **JDK**: Java Development Kit 17 or 21 (e.g. OpenJDK 21 or Android Studio JetBrains Runtime)
- **Android SDK**:
  - `compileSdk`: 36 (Android 16)
  - `minSdk`: 24 (Android 7.0 Nougat)
  - `targetSdk`: 36
  - Android SDK Platform-Tools 36+
  - Android SDK Build-Tools 35.0.0+ / 36.0.0+

---

## 🏗️ Build Commands

### 1. Build Standalone Debug APK

```bash
# Windows
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
gradlew.bat assembleDebug

# Linux / macOS
export JAVA_HOME=/path/to/jdk-21
./gradlew assembleDebug
```

Output:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Signed Release APK

```bash
# Windows
gradlew.bat assembleRelease

# Linux / macOS
./gradlew assembleRelease
```

Output:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔍 Native Bundled Libraries Verification

To inspect the native shared libraries bundled inside the APK:

```bash
python -c "import zipfile; z = zipfile.ZipFile('app/build/outputs/apk/release/app-release.apk'); print('\n'.join([f for f in z.namelist() if 'lib/' in f]))"
```

The APK contains:
- `lib/arm64-v8a/libpython.bin.so`
- `lib/arm64-v8a/libpython.zip.so`
- `lib/arm64-v8a/libffmpeg.bin.so`
- `lib/arm64-v8a/libffprobe.bin.so`
- `lib/x86_64/libpython.bin.so`
- `lib/x86_64/libffmpeg.bin.so`
- `lib/armeabi-v7a/libpython.bin.so`
- `lib/armeabi-v7a/libffmpeg.bin.so`
