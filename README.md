# 🧁 BakeCost Pro — Bakery Pricing Calculator

A complete bakery cost and pricing calculator that works as:
- 🌐 **Web app** via GitHub Pages (open in any browser)
- 📱 **iPhone PWA** — Add to Home Screen from Safari
- 🤖 **Android APK** — built automatically by GitHub Actions

---

## 🚀 Quick Start

### Web / iPhone
Your GitHub Pages URL: `https://YOUR-USERNAME.github.io/bakecost-pro/`  
Open in Safari on iPhone → Share → **Add to Home Screen**

### Android APK
1. Go to **Actions** tab → latest run → **Artifacts** → download `BakeCost-Pro-Debug-APK`
2. Transfer the `.apk` to your Android phone and install  
   *(Settings → Security → Install unknown apps → Allow)*

---

## 📁 Repository Structure

```
├── index.html              ← Web app (also copied into Android assets by CI)
├── manifest.json           ← PWA manifest for iPhone
├── sw.js                   ← Service worker (offline support)
├── icons/                  ← PWA icons (iOS)
├── .nojekyll               ← GitHub Pages config
├── .github/
│   └── workflows/
│       └── build.yml       ← Builds Android APK automatically on every push
└── android/                ← Android project
    ├── app/src/main/
    │   ├── assets/         ← index.html copied here by CI
    │   ├── java/…/         ← MainActivity.kt (WebView wrapper)
    │   └── res/            ← Icons, themes, config
    ├── build.gradle
    ├── settings.gradle
    └── gradlew
```

---

## ⚙️ GitHub Pages Setup

1. Repository → **Settings** → **Pages**
2. Source: `main` branch → `/ (root)` → **Save**

---

## 🔐 Signed Release APK (Optional)

See `android/README.md` for full instructions on generating a keystore  
and adding GitHub Secrets for a signed release build.

---

## 🌐 Features

- 🎂 12+ preset varieties + add custom ones
- 🧂 35+ pre-loaded ingredient prices (Indian market rates)
- ⚖️ Theoretical vs actual cake weight & yield tracking
- ⚙️ Auto-suggested overheads (electricity, labour, packaging)
- 🧮 Selling price calculator with custom profit margin
- 🌐 AI market price comparison (Anthropic API)
- ☁️ Google Sheets live sync (hardwired, auto every 1 min)
- 📋 Full history with ingredient & cost breakdown popup
- 🧂 Ingredient Price Database — add/edit/delete/import/export CSV
