# Workspace Rules & Android CLI Cheatsheet for AttendX

## 1. Firebase & R8/ProGuard Guidelines

### A. Keep Rules for Firestore Data Models
- **Rule**: Every data class or DTO stored in or retrieved from Firestore using `toObject()` / `toObjects()` **MUST** have an explicit `-keep` entry in `app/proguard-rules.pro`.
- R8 obfuscation in release builds strips no-arg constructors required for reflection.
- Example:
  ```proguard
  -keep class in.jyotirmoy.attendx.peer.data.model.** { *; }
  -keep class in.jyotirmoy.attendx.timetable.data.model.community.** { *; }
  ```

### B. Safe Firestore Coroutine Flow Exception Handling
- **Rule**: Never call `close(error)` inside `callbackFlow` snapshot listeners.
- `PERMISSION_DENIED` or auth latency errors passed to `close(error)` crash the main thread. Always emit a safe fallback (e.g. `trySend(emptyList())`) or handle errors gracefully within the flow.

### C. Package Name & Firebase Authentication Alignment
- Maintain package name consistency (`in.jyotirmoy.attendx`).
- Ensure both **Release SHA-1** and **Debug SHA-1** (`E6:7B:05:FB:91:23:A3:B9:50:DB:47:6C:29:88:62:DC:58:76:8C:2D`) are added under the `in.jyotirmoy.attendx` app in Firebase Console and Google Cloud Console API key restrictions.

---

## 2. Commonly Used Commands Cheatsheet

### 🛠️ Build & Installation Commands
* **Install Debug Build on Connected Device/Emulator:**
  ```powershell
  .\gradlew installDebug
  ```
* **Assemble Production Release APKs:**
  ```powershell
  .\gradlew assembleRelease
  ```
* **Clean & Rebuild Project:**
  ```powershell
  .\gradlew clean assembleDebug
  ```

### 🔍 Debugging & Logcat
* **List Connected Devices / Emulators:**
  ```powershell
  adb devices
  ```
* **Filter Crash & Runtime Errors from Logcat:**
  ```powershell
  adb logcat -d 2>&1 | Select-String -Pattern "FATAL|AndroidRuntime|in.jyotirmoy|Caused by:" | Select-Object -Last 50
  ```
* **Filter Logcat on Specific Emulator:**
  ```powershell
  adb -s emulator-5554 logcat -d 2>&1 | Select-String -Pattern "FATAL|AndroidRuntime|in.jyotirmoy|Caused by:"
  ```
* **Clear Logcat Buffer:**
  ```powershell
  adb logcat -c
  ```

### 📱 Package Management
* **Uninstall Debug Package Variant:**
  ```powershell
  adb uninstall in.jyotirmoy.attendx.debug
  ```
* **Uninstall Main / Production Package:**
  ```powershell
  adb uninstall in.jyotirmoy.attendx
  ```

### 🔑 Release & Secrets Automation
* **Base64 Encode `google-services.json` for GitHub Secrets (PowerShell):**
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("app/google-services.json")) | clip
  ```
* **Git Commit, Tag & Push Release (Replace `vX.Y.Z` and message with actual version/summary):**
  ```powershell
  git add .
  git commit -m "feat: release summary"  # Example message
  git push
  git tag vX.Y.Z                        # Example tag (e.g. v2.5.1)
  git push origin vX.Y.Z
  ```
