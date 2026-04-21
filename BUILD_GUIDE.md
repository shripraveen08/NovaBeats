# NovaBeats — Complete Build Guide
# 100% Free Music APK | No Subscription | No Copyright Issues

## ─── WHAT YOU NEED (all free) ────────────────────────────────────────────────

1. Android Studio Hedgehog (2023.1.1) or newer
   Download: https://developer.android.com/studio

2. Java JDK 17 (bundled with Android Studio, nothing extra needed)

3. A free Jamendo Developer account for the API key
   Register: https://devportal.jamendo.com  (free, instant)

4. Internet Archive — NO key needed, completely open


## ─── STEP 1: Open Project ─────────────────────────────────────────────────────

1. Unzip the NovaBeats source folder
2. Open Android Studio
3. Click "Open" → select the NovaBeats folder
4. Wait for Gradle sync (first time takes 3–5 minutes, downloads dependencies)


## ─── STEP 2: Add Your Jamendo API Key ────────────────────────────────────────

Open this file:
  app/src/main/java/com/novabeats/data/remote/jamendo/JamendoApiService.kt

Find line:
  const val JAMENDO_CLIENT_ID = "YOUR_JAMENDO_CLIENT_ID"

Replace with your actual client ID from https://devportal.jamendo.com
Example:
  const val JAMENDO_CLIENT_ID = "a1b2c3d4"

That's the only configuration you need. Internet Archive needs no key.


## ─── STEP 3: Add Launcher Icons ──────────────────────────────────────────────

1. In Android Studio: Right-click res/ → New → Image Asset
2. Set icon type to "Launcher Icons (Adaptive and Legacy)"
3. Use any image you like (or the green play button we included)
4. Android Studio will generate all sizes automatically


## ─── STEP 4: Build Debug APK (for testing) ───────────────────────────────────

Option A — Android Studio UI:
  Build menu → Build Bundle(s) / APK(s) → Build APK(s)
  APK saved to: app/build/outputs/apk/debug/app-debug.apk

Option B — Terminal:
  cd NovaBeats
  ./gradlew assembleDebug
  APK: app/build/outputs/apk/debug/app-debug.apk


## ─── STEP 5: Build Release APK (for sharing/installing) ──────────────────────

1. Create a signing keystore (one time only):
   Build menu → Generate Signed Bundle / APK → APK → Create new keystore
   Fill in details, save the .jks file safely

2. Build signed release:
   ./gradlew assembleRelease
   APK: app/build/outputs/apk/release/app-release.apk

3. Install on phone:
   adb install app/build/outputs/apk/release/app-release.apk
   OR just copy the APK to your phone and tap to install
   (enable "Install from unknown sources" in phone settings)


## ─── PROJECT STRUCTURE ────────────────────────────────────────────────────────

NovaBeats/
├── app/
│   ├── build.gradle.kts          ← all dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/novabeats/
│       │   ├── NovaBeatApp.kt           ← Application class (Hilt)
│       │   ├── MainActivity.kt          ← Entry point + navigation
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── NovaBeatDatabase.kt   ← Room DB
│       │   │   │   ├── dao/Daos.kt           ← All DAO interfaces
│       │   │   │   └── entities/Entities.kt  ← DB tables
│       │   │   └── remote/
│       │   │       ├── jamendo/              ← CC music (free & legal)
│       │   │       └── archive/              ← Public domain music
│       │   ├── di/AppModule.kt          ← Hilt dependency injection
│       │   ├── player/
│       │   │   ├── NovaBeatPlayerService.kt  ← Media3 foreground service
│       │   │   ├── PlayerViewModel.kt        ← Playback state & controls
│       │   │   └── DownloadWorker.kt         ← Offline download
│       │   └── ui/
│       │       ├── theme/                    ← Material 3 + Dynamic Color
│       │       ├── home/                     ← Home + mood playlists
│       │       ├── explore/                  ← Search across all sources
│       │       ├── library/                  ← Local & downloaded tracks
│       │       ├── playlists/                ← Playlist management
│       │       └── player/                   ← Mini + full player UI
│       └── res/
│           ├── drawable/ic_splash.xml
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties


## ─── MUSIC SOURCES & COPYRIGHT ────────────────────────────────────────────────

SOURCE          | LICENSE          | NOTES
----------------|------------------|--------------------------------------
Jamendo         | Creative Commons | 600,000+ tracks, fully legal
Internet Archive| Public Domain    | Millions of recordings, fully legal
Local Files     | User's own files | No issues
YouTube Music   | NOT included     | We intentionally excluded YTM to
                |                  | avoid copyright / ToS issues


## ─── FEATURES ────────────────────────────────────────────────────────────────

✅ Background playback (even with screen off)
✅ Notification controls (play/pause/next on lock screen)
✅ Offline downloads (save songs for no-internet playback)
✅ Mood-based playlists (Focus, Sleep, Workout, Chill, Party, Sad)
✅ Sleep timer with auto-pause
✅ Like / favourite songs
✅ Recently played history
✅ Create and manage playlists
✅ Search across Jamendo + Internet Archive
✅ Album art display
✅ Material 3 design with dynamic color (Android 12+)
✅ Works on Android 6.0 and above
✅ 100% ad-free
✅ 100% subscription-free
✅ Open source (GPL-3.0)


## ─── TROUBLESHOOTING ──────────────────────────────────────────────────────────

Problem: Gradle sync fails
Fix: File → Invalidate Caches → Restart

Problem: "SDK not found"
Fix: Android Studio → SDK Manager → install Android 14 SDK (API 34)

Problem: No music loads
Fix: Make sure you added your Jamendo client ID correctly

Problem: App crashes on launch
Fix: Check Logcat in Android Studio for the error message


## ─── CONTACT & LICENSE ────────────────────────────────────────────────────────

NovaBeats is open source under GPL-3.0
Based on OuterTune / InnerTune (also GPL-3.0)
All music content sourced from Jamendo (CC) and Internet Archive (Public Domain)
No affiliation with YouTube, Google, or any commercial music service
