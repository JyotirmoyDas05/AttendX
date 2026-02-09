# AttendX

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

---

**AttendX** is a comprehensive, privacy-focused academic tracking tool designed for effortless attendance management. Built with modern Android technologies, it revolutionizes how students manage their class attendance by moving beyond simple counters to a **calendar-first approach**.

Unlike traditional apps that only show percentages, AttendX provides an intuitive visual calendar interface where you can mark attendance for specific dates, view patterns at a glance, and get smart **AI-powered insights** about your bunk allowance.

---

## 🚀 New in v2.3.1: Multi-Architecture APK Support

Android 15+ requires 64-bit apps. This update ensures compatibility across all devices.

- **Multi-Architecture Builds**: APKs for arm64-v8a, armeabi-v7a, x86_64, and universal.
- **Smart OTA Updates**: Auto-detects your device architecture and downloads the best APK.
- **Android 15+ Ready**: 64-bit devices automatically get the arm64-v8a build.
- **GitHub Releases**: All architecture variants included in each release.

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

### 🔔 Intelligent Timetable Notifications (New!)

- **Exact Alarms**: Alerts fire precisely at class time using `AlarmManager`.
- **Interactive**: Mark status directly from the notification.
- **Resilient**: Alarms auto-restore after device reboots.

### 🎨 Modern Material You Design

- **Dynamic Theming**: Adapts to your device wallpaper.
- **Expressive UI**: Fluid animations and clean typography (One UI Sans).

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

You can download the latest APK from the [Releases](https://github.com/xtractiion/self.attendance/releases) section.

1.  Download `app-arm64-v8a-release.apk` (Recommended).
2.  Install on your Android device.
3.  Grant Notification & Alarm permissions when prompted.

---

## Contributing & Support

If you find this app useful, please:

- ⭐ **Star this repository** on GitHub.
- 🐛 **Report bugs** in the Issues section.
- 💡 **Suggest features** you'd like to see.

**Built with ❤️ by Jyotirmoy Das**
