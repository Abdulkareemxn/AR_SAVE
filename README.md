# AR SAVE ⚡

> **A modern, locally processed native Android video & audio downloader powered by yt-dlp, QuickJS, and FFmpeg.**
> 
> - **Lead Developer / Project by:** A. Kareem ([@akareemxn](https://instagram.com/akareemxn))
> - **Organization:** AR INSTITUTE OF COMPUTER AND TECHNOLOGY ([@ar_institute_](https://instagram.com/ar_institute_))
> - **App Name:** AR SAVE
> - **Version:** 1.0.0

---

## 🌟 Overview

**AR SAVE** is an open-source, standalone Android application designed with Jetpack Compose (Material 3) and an obsidian-navy glassmorphism aesthetic. It bundles a complete **CPython 3.12** runtime, **QuickJS** JavaScript engine (`libqjs.so`), the latest **yt-dlp core**, and native **FFmpeg** binaries (`libffmpeg.so`, `libffprobe.so`) directly into the APK.

### Why AR SAVE?
- ⚡ **100% On-Device Execution**: Zero backend server, zero proxy, zero cloud processing required.
- 🔒 **Zero Telemetry & Private**: No analytics, tracking, or telemetry SDKs.
- 🎬 **Up to 4K Ultra HD & 60fps**: Full resolution ladder (2160p, 1440p, 1080p, 720p, 480p) + 320 kbps Studio Audio (MP3 / M4A).
- 📱 **Native Material 3 Interface**: 10-screen custom design featuring Splash Screen, Home Screen, Video Details, Live Downloading Monitor, Downloads History, Side Navigation Drawer, Library Manager, Settings, Theme Selector, and Credential/About screen.

---

## 🚀 Key Features

1. **Standalone On-Device Architecture**:
   - Bundled **Python 3.12** runtime (`libpython.so`) and **QuickJS** (`libqjs.so`).
   - Native **FFmpeg & FFprobe** for on-device video stream muxing and audio transcoding.
   - Built-in automatic self-healing updater for the yt-dlp extraction core.

2. **Obsidian & Electric Blue UI (Jetpack Compose)**:
   - Deep obsidian background (`#0B0F17`) with cyan/electric blue accents (`#2563EB`, `#00C6FF`).
   - One-tap clipboard paste button and quick social platform launchers (YouTube, Instagram, Facebook, TikTok).
   - 2x2 Feature Cards (*Fast Downloads*, *All Major Sites*, *Safe & Private*, *High Quality*).

3. **Rich Media Details & Format Picker**:
   - 16:9 thumbnail preview with duration badge.
   - Channel avatar, name, and subscriber count.
   - View count, upload date, and duration metadata.
   - Filter pills (`[Video]`, `[Audio]`, `[Subtitles]`) with radio selections and tags (`[HDR]`, `[60fps]`).

4. **Live Downloading Monitor**:
   - Real-time quality, file size, download speed meter, elapsed time, and remaining time (ETA).
   - Live progress indicator with pause, resume, and cancel capabilities.

5. **Downloads & Library Management**:
   - Active downloads tracker with badge count.
   - Completed media list stored in `Downloads/AR_SAVE`.
   - In-app search, one-tap system media playback, social sharing, and deletion.

6. **System Share Intent**:
   - Open YouTube, Instagram, Facebook, or TikTok, tap **Share** ➔ **AR SAVE** to directly inspect and download media.

---

## 📦 Ready-to-Install APKs

Pre-built installable APKs are provided in [`publish_apk/app/`](file:///c:/Users/Khushbu%20AR/Desktop/yt-dlp-master/yt-dlp-master/publish_apk/app):

| Package | Type | Size | Description |
|---|---|---|---|
| [`AR_SAVE.apk`](publish_apk/app/AR_SAVE.apk) | Standalone Debug APK | ~171.9 MB | Pre-signed standalone APK ready for immediate installation on Android 7.0+ (API 24 to 36). |
| [`AR_SAVE_release.apk`](publish_apk/app/AR_SAVE_release.apk) | Optimized Release APK | ~164.4 MB | Production release build with bundled CPython 3.12, QuickJS, FFmpeg, and release assets. |

---

## 🏛️ Project Structure

```
publish/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/arsave/app/
│   │   │   ├── ArSaveApp.kt            # App initialization & CPython purge
│   │   │   ├── MainActivity.kt         # Navigation, drawer & scaffold
│   │   │   ├── data/
│   │   │   │   └── DownloadEngine.kt   # yt-dlp Kotlin engine & state flows
│   │   │   └── ui/
│   │   │       ├── about/              # About & verified credits screen
│   │   │       ├── components/         # Brand logo, top bar & side drawer
│   │   │       ├── downloads/          # Active & completed download views
│   │   │       ├── library/            # Media library & search view
│   │   │       ├── main/               # Home, details & downloading views
│   │   │       ├── settings/           # Preferences & theme selector
│   │   │       ├── splash/             # Neon splash screen
│   │   │       └── theme/              # Obsidian & electric blue palette
│   │   └── res/                        # Vector icons, launcher icons & raw assets
│   ├── build.gradle.kts                # App-level dependencies & NDK packaging
│   └── proguard-rules.pro              # Proguard configuration
├── gradle/
│   ├── libs.versions.toml              # Version catalog
│   └── wrapper/                        # Gradle wrapper jar & properties
├── publish_apk/
│   └── app/
│       ├── AR_SAVE.apk                 # Debug APK
│       └── AR_SAVE_release.apk         # Release APK
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Project settings & repositories
├── gradle.properties                   # JVM & memory configuration
├── gradlew & gradlew.bat               # Gradle wrapper executable
└── README.md                           # Documentation
```

---

## 🛠️ Building from Source

### Prerequisites
- **JDK 17 or 21**
- **Android Studio Ladybug / Meerkat** or newer (with Android SDK 36)
- **NDK** installed (targets `arm64-v8a`, `x86_64`, `armeabi-v7a`)

### Build Commands
```bash
# Clone the repository
git clone https://github.com/your-username/ar-save.git
cd ar-save

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease
```
The output APKs will be located in `app/build/outputs/apk/debug/` and `app/build/outputs/apk/release/`.

---

## 👨‍💻 Credits & Author

- **Project by:** A. Kareem  
  - Instagram: [@akareemxn](https://instagram.com/akareemxn)
- **Organization:** AR INSTITUTE OF COMPUTER AND TECHNOLOGY  
  - Instagram: [@ar_institute_](https://instagram.com/ar_institute_)
- **Core Technology:** Powered by [yt-dlp](https://github.com/yt-dlp/yt-dlp) & [FFmpeg](https://ffmpeg.org).

---

## 📜 License

This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
