# AttendX Privacy Policy

**Last updated: 4 September 2026**

AttendX is built so that your attendance record is yours. There is no sign-up, no
password, and no user profile. This document explains exactly what stays on your
phone, what can leave it, and what triggers that.

---

## The short version

- Your attendance, subjects, timetable, tasks, notes and study materials **never leave your device** unless you personally tap a button that shares something.
- There are exactly **three** things that send data off your device, and each is an explicit, opt-in action: sharing a timetable, comparing attendance with someone via QR, and linking a Google account.
- We never ask for your name, email, phone number or location. Nothing is sold or used for advertising.

---

## What stays on your device

All of the following is stored only in the app's local database and settings, and
is included in a backup file only when you export one yourself:

- Attendance records, per-subject percentages, streaks and targets
- Subjects, subject codes and colours
- Your timetable, class times, rooms and class types
- Tasks, notes and course materials
- Pomodoro / focus session history
- Every app setting and preference

If you uninstall AttendX without exporting a backup, this data is gone. We cannot
recover it, because we never had it.

---

## What can leave your device

### 1. An anonymous ID

The first time you use any online feature, the app creates an **anonymous account**
with Firebase Authentication. This is a random identifier. It is not linked to your
name, email, phone, Google account or device identity, and it is not used for
tracking or advertising. It exists so that the server can tell "the person who
shared this timetable" from everyone else.

It is stored on your device. Clearing app data or uninstalling loses it, and with
it the ability to edit anything you previously shared — unless you have linked a
Google account (see below).

### 2. Timetables you choose to share

Only when you use **Share Timetable**. What is uploaded:

- College, department, semester, section and academic year, as you typed them
- The subject names, subject codes and histogram labels you ticked
- The class days, times, rooms and class types you ticked
- Your anonymous ID, and your display name if you set one

**Your attendance data is never part of a shared timetable.** No percentages, no
records of which classes you attended, no targets or streaks.

Shared timetables are **public to anyone using the app**. Do not type anything into
the college, department or section fields that you would not want other students
to read.

### 3. Attendance comparison sessions

Only when you start or join a **peer comparison** by QR code. This is the most
sensitive thing the app transmits, so it is worth being precise:

- What is uploaded: your subject names and **your attendance percentage for each subject**, plus the anonymous IDs of both participants.
- Who can read it: only the two people in the session. Once a session is paired, nobody else can read it, even if they somehow obtain the session's ID.
- How pairing works: the session ID is shown as a QR code, and is a long random value. Anyone who scans it before the session is joined can join it — so only show the code to the person you mean to compare with.
- The session is **deleted from the server** as soon as either person leaves it, or after 15 minutes, whichever comes first. Nothing is kept afterwards.
- One gap worth naming: if the app is force-quit rather than closed normally, that deletion may not run and the record can linger. Contact us and we will remove it.

### 4. A Google account, only if you ask for one

By default AttendX has/needs no accounts to function for almost all the Features. On the **Share Timetable** screen you can
optionally link a Google account. This exists for exactly one reason: so you can
still update a timetable you shared after reinstalling the app or moving to a new
phone.

If you link one:

- Firebase Authentication stores your **email address and Google account ID**, attached to your existing anonymous ID.
- Your email is **never written into a shared timetable** and is never shown to other users.
- Other students still see "Anonymous Student", or the display name you chose.
- Nothing else in AttendX asks you to sign in. Every other feature works exactly as before.

If you never tap Link, no email address is ever collected.

### 5. A display name, only if you ask for one

After linking, you may choose a display name that replaces "Anonymous Student" on
timetables you share. It is stored with your account and in a public list of taken
names, so that two people cannot claim the same one. Anything you type here is
public — do not use your full legal name unless you intend to.

### 6. Update checks

The app periodically asks GitHub for the latest release. GitHub receives your IP
address as part of that ordinary web request. No app data is sent. You can turn
this off in Settings.

### 7. Firebase Analytics

The app bundles Google's Firebase Analytics SDK. AttendX logs no custom events,
but the SDK automatically collects basic usage data such as an app instance ID,
app version, device model, operating system version, coarse country, session
counts and screen views. This is aggregate usage information; it contains none of
your attendance, subject or timetable data.

---

## What we never collect

- Your name, email (unless you link an account), phone number or address
- Location or GPS data
- Contacts, photos, files or other apps' data
- Advertising identifiers
- The camera is used **only** to scan a comparison QR code, in that moment. No image is stored or transmitted.

---

## Retention and deletion

| Data | How long |
|---|---|
| Everything on your device | Until you delete it or uninstall |
| A shared timetable | Until it is deleted. Contact us to remove one. |
| Comparison sessions | Deleted when either person leaves, or after 15 minutes |
| Anonymous account | Until you clear app data or uninstall |
| Linked email, if any | Until the account is deleted. Contact us to delete it. |
| Display name reservation | Until you change or release it |

To have a shared timetable, a comparison session or a linked account removed, open an issue at
[github.com/JyotirmoyDas05/AttendX/issues](https://github.com/JyotirmoyDas05/AttendX/issues)
or email the address in the app's About screen.

---

## Who else is involved

- **Google Firebase** (Authentication, Cloud Firestore, Analytics) hosts the shared timetables, comparison sessions and anonymous accounts. Google's privacy policy: [policies.google.com/privacy](https://policies.google.com/privacy)
- **GitHub** serves update checks and releases.

No other third party receives any data. Nothing is sold, rented or used for
advertising or profiling.

---

## Children

AttendX is intended for students in higher secondary and higher education. It is
not directed at children under 13, and we do not knowingly collect data from them.

---

## Your choices

- Use the app entirely offline. Every core feature — attendance, timetable, tasks, focus timer — works with no network at all.
- Never tap Share Timetable, and nothing you own is ever uploaded.
- Never start a comparison, and no attendance percentage ever leaves your phone.
- Never link an account, and no email is ever collected.
- Export a local backup at any time from Settings.

---

## Changes

Material changes to this policy will be noted in the app's changelog and in the
commit history of this file. The date at the top always reflects the current
version.

## Contact

Open an issue at
[github.com/JyotirmoyDas05/AttendX/issues](https://github.com/JyotirmoyDas05/AttendX/issues).
