package `in`.jyotirmoy.attendx.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.compositionLocalOf
import `in`.jyotirmoy.attendx.core.domain.model.GithubReleaseType
import `in`.jyotirmoy.attendx.core.domain.model.SubjectCardStyle
import `in`.jyotirmoy.attendx.core.domain.provider.SeedColorProvider
import `in`.jyotirmoy.attendx.settings.domain.model.CustomFontFamily
import `in`.jyotirmoy.attendx.settings.domain.model.SettingsState

val LocalSettings = compositionLocalOf {
    SettingsState(
        isAutoUpdate = false,
        themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
        isHighContrastDarkMode = false,
        seedColor = SeedColorProvider.seed,
        isDynamicColor = true,
        isHapticEnabled = true,
        subjectCardCornerRadius = 8f,
        subjectCardStyle = SubjectCardStyle.CARD_STYLE_A,
        githubReleaseType = GithubReleaseType.STABLE,
        savedVersionCode = 0,
        showAttendanceStreaks = true,
        rememberCalendarMonthYear = false,
        startWeekOnMonday = true,
        enableDirectDownload = true,
        notificationPreference = true,
        notificationPermissionDialogShown = false,
        showGithubWarningDialog = true,
        fontFamily = CustomFontFamily.SYSTEM_FONT
    )
}