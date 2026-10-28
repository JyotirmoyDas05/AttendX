<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="120" height="120" alt="AttendX Logo" />
</p>

<h1 align="center">AttendX</h1>

<p align="center">
  <img src="./right%20now/img%201.jpg" width="31%" style="border-radius:26px; margin: 2px;"/>
  <img src="./right%20now/img%202.jpg" width="31%" style="border-radius:26px; margin: 2px;"/>
  <img src="./right%20now/img%203.jpg" width="31%" style="border-radius:26px; margin: 2px;"/>
</p>

<p align="center">
  <img src="./right%20now/img%204.jpg" width="31%" style="border-radius:26px; margin: 2px;"/>
  <img src="./right%20now/img%205.jpg" width="31%" style="border-radius:26px; margin: 2px;"/>
</p>

<p align="center">
    <a href="https://github.com/JyotirmoyDas05/AttendX/releases/latest">
        <img src="https://img.shields.io/github/v/release/JyotirmoyDas05/AttendX?include_prereleases&logo=github&style=for-the-badge&label=Latest%20Release" alt="Latest Release">
    </a>
    <a href="https://github.com/JyotirmoyDas05/AttendX/releases">
        <img src="https://img.shields.io/github/downloads/JyotirmoyDas05/AttendX/total?logo=github&style=for-the-badge" alt="Total Downloads">
    </a>
    <img src="https://img.shields.io/badge/Android-10%2B-green?style=for-the-badge&logo=android" alt="Android 10+">
    <img src="https://img.shields.io/badge/Kotlin-100%25-purple?style=for-the-badge&logo=kotlin" alt="Kotlin">
</p>

<!-- DeepWiki Badge -->
<p align="center">
  <a href="https://deepwiki.com/JyotirmoyDas05/AttendX">
    <img alt="Ask DeepWiki" src="https://deepwiki.com/badge.svg" height="40" />
  </a>
</p>

---

**AttendX** is a comprehensive, privacy-focused academic tracking tool designed for effortless attendance management. Built with modern Android technologies, it revolutionizes how students manage their class attendance by moving beyond simple counters to a **calendar-first approach**.

Unlike traditional apps that only show percentages, AttendX provides an intuitive visual calendar interface where you can mark attendance for specific dates, view patterns at a glance, and get smart **AI-powered insights** about your bunk allowance.

---

## 🚀 Major Update: Home Screen Widgets

- **Four Expressive Widgets**: Overall attendance, Subject attendance, Next class and Schedule — built with Glance, Google Sans Flex figures and wavy progress bars.
- **Layouts That Fill Their Space**: Every widget resizes both ways and reveals more content as it grows, instead of padding out empty space.
- **Always Current**: Widgets roll over the moment a class ends, refresh at midnight, and recover after a reboot or a time-zone change.
- **One-Tap Deep Links**: Tapping a class opens it on the Timetable tab; "Add class" opens the sheet with the floating nav bar still in reach.

---

## Why AttendX Stands Out

Most attendance trackers are just simple counters with no context. AttendX solves this:

| Traditional Apps ❌                                     | AttendX ✅                                                                            |
| :------------------------------------------------------ | :------------------------------------------------------------------------------------ |
| **No Date Association**: Can't see _when_ you attended. | **Date-Specific Records**: Every attendance is tied to a calendar date.               |
| **Just Numbers**: No visual history.                    | **Visual Calendar**: See streaks, gaps, and monthly patterns instantly.               |
| **Manual Math**: You figure out bunking logic.          | **AI Insights**: Smart calculations tell you _exactly_ how many classes you can bunk. |
| **Limited History**: Can't view past months.            | **Monthly Navigation**: Browse complete attendance history.                           |

---

## Key Features

### 📅 Calendar-Based Attendance

- **Visual Overview**: See your entire month's attendance at a glance.
- **Color-Coded**: Dates are marked Green (Present) or Red (Absent) for instant feedback.
- **Streak Tracking**: Monitor your longest present or absent streaks.

### 🎯 Smart Attendance Insights

The app doesn't just show a percentage. It tells you what to do:

- _"You can bunk **3** more classes and still stay above 75%."_
- _"You are safe at 75%. Don't miss the next class!"_
- _"Attend the next **2** classes to reach your target."_

### 🔔 Intelligent Timetable Notifications

- **Exact Alarms**: Alerts fire precisely at class time using `AlarmManager`.
- **Interactive**: Mark status directly from the notification.
- **Resilient**: Alarms auto-restore after device reboots.

### 🧩 Home Screen Widgets (New!)

- **Overall Attendance**: Your percentage, the present/absent split, and how many classes you can still skip.
- **Subject Attendance**: Pick a subject when you place it; tracks that subject against its own target.
- **Next Class**: What's on now, what's next and when it ends, recalculated the minute a class rolls over.
- **Schedule**: Your week day by day — a compact list on small sizes, a full board at 3×3 and above.
- **Material 3 Expressive**: Google Sans Flex figures, wavy progress, concentric corners, dynamic color and dark theme.

### 📱 Liquid Glass Navigation

- **AGSL Shaders**: Real-time glassmorphism with depth effects and chromatic dispersion.
- **G2 Squircles**: Fluid gooey tab indicator animations.
- **Customization Studio**: Adjust glass opacity, tint, blur, and refraction.

### 📊 Expressive Pomodoro Focus Suite

- **Material 3 Expressive Timer**: Dynamic session transitions with custom themes.
- **Rich Analytics**: Interactive charts for focus vs break ratios and weekly/monthly trends.
- **Topic Tags**: Organize study sessions with custom labels and color tags.
- **Foreground Service**: Reliable background timer with live notification controls.

### 👥 QR-Based Peer Attendance Comparison

- **Ephemeral QR Sharing**: Compare attendance stats instantly with classmates via encrypted QR codes.
- **Realtime Session Sync**: Live Firestore synchronization with auto-expiring sessions.
- **Smart Subject Matching**: Fuzzy match engine for subject alignment.

### 📚 Subject Study Resources

- **Tasks & Deadlines**: Track assignments with due dates and completion checklists.
- **Course Materials**: Attach PDFs, lecture notes, and reference links.
- **Media Viewers**: Built-in audio player and image preview.

### 🎨 Modern Material You Design

- **Dynamic Theming**: Adapts to your device wallpaper.
- **Expressive UI**: Fluid animations and clean typography.

### 💾 Backup & Restore

- **Full Data Export**: Save your database and settings locally.
- **Easy Restore**: Seamlessly migrate data to a new device.

---

## 🛠️ Usage

### Quick Start

1.  **Add a Subject**: Tap the "+" button and enter subject details.
2.  **Set Target**: Choose your desired attendance percentage (default: 75%).
3.  **Mark Attendance**: Click any date on the calendar to toggle Present/Absent/Holiday.
4.  **View Insights**: Read the smart messages cards to know your bunk status.

### Pro Tips

- **Sidebar**: Use the sidebar (rotating gear icon) for a quick summary of all subjects.
- **Notifications**: Enable "Timetable Notifications" in settings to get class alerts.
- **Backup**: Regular backups ensure you never lose your academic history.

---

## 🔧 Technical Specifications

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture + Dependency Injection (Hilt)
- **Database**: Room Database (Offline-first)
- **Background Tasks**: WorkManager & AlarmManager

---

## Installation

You can download the latest APK from the [Releases](https://github.com/JyotirmoyDas05/AttendX/releases) section.

### Choose the Right APK for Your Device

| Architecture | APK | Size | Recommended For |
| :----------- | :-- | :--- | :-------------- |
| **arm64-v8a** | `AttendX-vX.X.X-arm64-v8a.apk` | ~18 MB | Most modern Android devices (2017+) — **Recommended** |
| **armeabi-v7a** | `AttendX-vX.X.X-armeabi-v7a.apk` | ~16 MB | Older 32-bit ARM devices (2015-2017) |
| **x86_64** | `AttendX-vX.X.X-x86_64.apk` | ~19 MB | Android emulators, Chromebooks, Intel-based tablets |
| **universal** | `AttendX-vX.X.X-universal.apk` | ~34 MB | Any Android device (all architectures combined) |

### How to Find Your Architecture

1.  **Settings → About Phone → Architecture** (varies by device)
2.  Or install the APK and let it fail — by failing you learn ( JK ;} by if it fails you will know you haven't installed the correct arch and try the other one, or if unsure just Install the **UNIVERSAL** package for No install issues | although it would take significant app size due to all architecture combined for most compatibility )

### Installation Steps

1.  Download the appropriate APK for your device.
2.  Install on your Android device.
3.  Grant Notification & Alarm permissions when prompted.

---

## Contributing & Support

If you find this app useful, please:

- ⭐ **Star this repository** on GitHub.
- 🐛 **Report bugs** in the Issues section.
- 💡 **Suggest features** you'd like to see.

**Built with ❤️ by Jyotirmoy Das**
