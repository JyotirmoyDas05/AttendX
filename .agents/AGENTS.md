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

### D. Git Commit & Release Rule
- **Rule**: **NEVER** automatically commit, tag, or push release changes without explicit user approval after testing.
- Pushing tags (`v*`) triggers the GitHub Actions Release build pipeline. Regular `git push` to `main` does NOT trigger release builds.

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

### 🔥 Firebase / Firestore Commands

**Always invoke the Firebase CLI through `npx firebase-tools`.** Never run a bare
`firebase` command and never install the CLI globally — `npx` fetches it on demand,
so there is nothing to keep updated and no global version to drift.

* **Deploy Firestore security rules** (from repo root, reads `firebase.json` → `firestore.rules`):
  ```powershell
  npx firebase-tools deploy --only firestore:rules --project attendx-community
  ```
  The explicit `--project` makes this a one-shot command that works on any machine
  and in any checkout, without a prior `firebase use`. Drop the flag only if
  `.firebaserc` is present and you know it points at the right project.
* **Run the security rules test suite** (starts the emulator, runs it, shuts it down):
  ```powershell
  cd firestore-tests; npm test
  ```
* **Run the Firestore emulator alone** (for manual poking):
  ```powershell
  npx firebase-tools emulators:start --only firestore
  ```
* **Log in / check the active account:**
  ```powershell
  npx firebase-tools login
  npx firebase-tools projects:list
  ```

> Rules changes must be accompanied by a test in `firestore-tests/rules.test.js`, and
> the suite must pass before deploying. A rules bug is silent — it either locks out
> real users or leaves data open, and neither shows up in a Gradle build.

### 🧪 Running the Test Suites

Three suites, three different reasons to reach for them.

* **JVM unit tests** — pure logic (time ranges, paging arithmetic, template merging,
  username filtering). Fast, no device needed. Run these on every change:
  ```powershell
  .\gradlew testDebugUnitTest
  ```
  A single class:
  ```powershell
  .\gradlew testDebugUnitTest --tests "*CalendarPagingTest*"
  ```
  Results, including the failure messages, land in
  `app/build/reports/tests/testDebugUnitTest/index.html`.

* **Instrumented UI tests** — layout and gesture behaviour that cannot be measured
  off-device: pinch-to-zoom (`adb shell input` cannot produce two fingers) and the
  status-band layout rules under long strings and large font scales. Needs a running
  emulator or attached device:
  ```powershell
  .\gradlew connectedDebugAndroidTest
  ```
  A single class (note: `--tests` is not supported for this task):
  ```powershell
  .\gradlew connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=in.jyotirmoy.attendx.TimetableZoomTest"
  ```
  > ⚠️ **This uninstalls the app from the target device when it finishes, which
  > erases its app data** — subjects, attendance, timetable and the anonymous
  > Firebase UID. Never run it against a device holding real data. Losing the
  > anonymous UID also orphans any community template uploaded from that device,
  > because ownership is that UID (see §1C). Export a backup from Settings first,
  > or use a throwaway emulator.

* **Firestore rules tests** — see the Firebase section above. Required before any
  rules deploy.

* **Everything before a release:**
  ```powershell
  .\gradlew testDebugUnitTest assembleDebug assembleRelease
  ```
  `assembleRelease` matters on its own: it is the only build that runs R8, and
  shrinking has broken resources here before (see §1A).

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
* **Cut a release with its notes written up front (preferred).**
  The GitHub release body *is* the in-app changelog — `ChangelogScreen` and the
  first-run `ChangelogBottomSheet` both read it from the GitHub Releases API, not
  from `strings.xml`. Publishing the notes with the tag means users never see a
  placeholder while you edit afterwards.

  Write the notes to a scratch file first, matching the house style used from
  v2.6.0 onwards:

  ```markdown
  # What's New in AttendX vX.Y.Z

  ## Headline Feature
  - **Bolded lead-in**: what changed and why it matters.

  ## Fixes & Polish
  - **Bolded lead-in**: what was broken, now not.

  ---
  **Full Changelog**: https://github.com/JyotirmoyDas05/AttendX/compare/vPREV...vX.Y.Z
  ```

  Then commit, and let one `gh` command create both the tag and the release:

  ```powershell
  git add .
  git commit -m "feat(release): vX.Y.Z - short summary"
  git push
  gh release create vX.Y.Z --target main --title "AttendX vX.Y.Z" --notes-file notes.md
  ```

  Creating the release also creates and pushes the tag, which triggers the build.
  The workflow detects that the release already exists and only uploads the APKs to
  it, leaving the notes alone. Do not attach APKs yourself — CI builds the signed
  ones.

* **Tag-only fallback** (CI writes the notes from `strings.xml`, or a bare
  `Release vX.Y.Z` if there is no `changelogs_vX_Y_Z` entry — there have been none
  since v2.5.1, so expect the placeholder and edit the release afterwards):
  ```powershell
  git tag vX.Y.Z
  git push origin vX.Y.Z
  ```

---

## 3. Material 3 Expressive Components & Animation Guidelines

### A. Exclusively Use Material 3 Expressive UI Offerings
- AttendX is a **Material 3 Expressive** design system. Always prefer and use Material 3 Expressive components (`@OptIn(ExperimentalMaterial3ExpressiveApi::class)` from `androidx.compose.material3`) rather than legacy M2 or basic M3 components.  
- **Examples**:
  - **Loading & Progress Indicators**:
    - Use `LoadingIndicator` / `ContainedLoadingIndicator` for indeterminate spinners and loading states (morphing polygon shapes).
    - Use `CircularWavyProgressIndicator` / `LinearWavyProgressIndicator` for determinate and indeterminate progress bars.
    - Avoid legacy `CircularProgressIndicator` / `LinearProgressIndicator` unless explicitly requested.

### B. AnimatedContent State Keying
- When using `AnimatedContent` with sealed UI states containing high-frequency changing values (such as countdown timers, `secondsRemaining`, or live progress updates), **ALWAYS** specify `contentKey = { it::class }`:
  ```kotlin
  AnimatedContent(
      targetState = state,
      contentKey = { it::class },
      transitionSpec = { fadeIn() togetherWith fadeOut() }
  ) { targetState ->
      // Composable content
  }
  ```
  This ensures smooth in-place recomposition without flashing the screen or resetting child animations on every second.

