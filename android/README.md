# 🧁 BakeCost Pro — Android APK Build Guide

This folder contains the Android project for BakeCost Pro.  
GitHub Actions automatically builds a `.apk` file every time you push to `main`.

---

## 📱 How to Get the APK

### Method 1 — Download from GitHub Actions (easiest)

1. Go to your repository on GitHub
2. Click the **Actions** tab at the top
3. Click the latest **"🧁 Build BakeCost Pro APK"** workflow run
4. Scroll down to **Artifacts**
5. Download **BakeCost-Pro-Debug-APK**
6. Unzip the downloaded file → you get `BakeCost_Pro_v1.0_YYYYMMDD_debug.apk`
7. Transfer to your Android phone and install

> **First time installing?** On your Android phone go to:  
> Settings → Security (or Apps) → **Install unknown apps** → Allow for your file manager

---

## 🔐 How to Build a Signed Release APK (Optional)

A **debug APK** works perfectly for personal use. For distributing on the Play Store  
or sharing widely, you need a **signed release APK**.

### Step 1 — Generate a Keystore (do this once)

On your computer with Java installed, run:

```bash
keytool -genkey -v \
  -keystore bakecost_release.keystore \
  -alias bakecost \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

When prompted, enter passwords and your details. **Save the keystore file and passwords safely — you cannot recover them.**

### Step 2 — Add Secrets to GitHub

1. Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret** and add these 4 secrets:

| Secret Name | Value |
|---|---|
| `KEYSTORE_BASE64` | Base64 of your keystore: `base64 -i bakecost_release.keystore` |
| `KEY_ALIAS` | `bakecost` (or whatever alias you used) |
| `KEY_PASSWORD` | The key password you set |
| `STORE_PASSWORD` | The keystore password you set |

### Step 3 — Push and download Release APK

Once secrets are set, push any change to `main`. The workflow will automatically  
build both debug and release APKs. Download the **BakeCost-Pro-Release-APK** artifact.

---

## 🛠️ Build Locally (Advanced)

Requirements: Android Studio / JDK 17 / Android SDK

```bash
# From repo root
cp index.html android/app/src/main/assets/index.html

cd android
chmod +x gradlew

# Debug build
./gradlew assembleDebug

# APK location
# android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Project Structure

```
.github/workflows/build.yml     ← GitHub Actions APK builder
android/
├── app/
│   ├── src/main/
│   │   ├── assets/             ← index.html is copied here by CI
│   │   ├── java/com/bakecostpro/app/
│   │   │   └── MainActivity.kt ← WebView wrapper
│   │   ├── res/
│   │   │   ├── mipmap-*/       ← App icons (5 densities)
│   │   │   ├── values/         ← strings, colors, themes
│   │   │   └── xml/            ← network config, file provider
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradle/wrapper/
    └── gradle-wrapper.properties
```

---

## ⚙️ App Technical Details

| Property | Value |
|---|---|
| Min Android Version | 5.0 (API 21) |
| Target Android Version | 14 (API 34) |
| Internet permission | ✅ (for Google Sheets sync + AI comparison) |
| Local storage | ✅ (DOM storage enabled in WebView) |
| Back button | ✅ (navigates back in WebView history) |
| File import (CSV) | ✅ (file chooser for ingredient DB import) |
| File export (CSV) | ✅ (download manager) |
| JS `confirm()` dialogs | ✅ (native Android alert dialogs) |
