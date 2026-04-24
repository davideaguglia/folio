<table align="center" width="100%"><tr><td align="center" bgcolor="#1a3a2a" style="padding:32px">
  <img src="app/src/main/res/drawable/logo.png" width="100" alt="Folio logo">
</td></tr></table>

<h1 align="center">Folio</h1>

<p align="center">
  <em>A clean, offline-first personal finance app for Android.</em><br>
  Track your spending, manage investments, and understand your financial health — all stored privately on your device.<br>
  No accounts, no cloud, no tracking.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/min_SDK-26_(Android_8.0)-356b44" alt="Min SDK 26">
  <img src="https://img.shields.io/badge/offline--first-100%25_private-245e3e" alt="Offline-first">
  <img src="https://img.shields.io/badge/license-open_source-1a3a2a" alt="License">
</p>

---

## Screenshots

| Home | Assets |
|:---:|:---:|
| ![Home](screenshots/Home.png) | ![Assets](screenshots/Assets.png) |

| Budget | History | Reports |
|:---:|:---:|:---:|
| ![Budget](screenshots/Budget.png) | ![History](screenshots/History.png) | ![Reports](screenshots/Reports.png) |

---

## ✦ Features

| Feature | Description |
|---|---|
| **Home** | Monthly net flow, income/expense summary, spending donut chart, recent transactions |
| **History** | Log income & expenses with category, account, date and notes; search, filter, swipe to delete, long-press multi-select |
| **Recurring** | Daily / weekly / monthly / yearly entries generated automatically at launch |
| **Assets** | Track stocks, ETFs, crypto and more; search by ticker symbol or ISIN for live price lookup; portfolio breakdown chart |
| **Budget** | Monthly spending limits per category with colour-coded progress bars |
| **Reports** | 12-month income/expense bar chart, category breakdown, net worth history |
| **Dark mode** | Toggle dark/light theme, persisted across sessions |
| **100% offline** | All data lives in a local SQLite database on your device |

---

## ✦ Download

Go to [**Releases**](../../releases) and grab the latest **`folio.apk`**.

| Step | |
|:---:|---|
| **1** | Download `folio.apk` on your Android device |
| **2** | Open the file — allow installation from unknown sources if prompted |
| **3** | Tap **Install** |

> Requires **Android 8.0** (API 26) or higher.

---

## ✦ Build from source

```bash
git clone https://github.com/davideaguglia/Finance_App.git
cd Finance_App

# Build a debug APK
./gradlew assembleDebug

# Install on a connected device
adb install app/build/outputs/apk/debug/app-debug.apk
```

> **Requirements:** Android Studio Hedgehog or newer · JDK 17 · Android device / emulator API 26+

---

## ✦ Tech

<p align="center">
  <code>Kotlin</code> · <code>Jetpack Compose</code> · <code>Material Design 3</code> · <code>Room</code> · <code>Hilt</code> · <code>Coroutines</code> · <code>WorkManager</code> · <code>Retrofit</code>
</p>
