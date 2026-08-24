package `in`.jyotirmoy.attendx.core.common

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.jyotirmoy.attendx.core.presentation.provider.AppSeedColors
import `in`.jyotirmoy.attendx.core.presentation.provider.SeedColor
import `in`.jyotirmoy.attendx.core.utils.HapticUtils.strongHaptic
import `in`.jyotirmoy.attendx.core.utils.HapticUtils.weakHaptic
import `in`.jyotirmoy.attendx.notification.isNotificationPermissionGranted
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.model.SettingsState
import `in`.jyotirmoy.attendx.settings.presentation.viewmodel.SettingsViewModel

val LocalWeakHaptic = staticCompositionLocalOf { {} }
val LocalStrongHaptic = staticCompositionLocalOf { {} }
val LocalDarkMode = staticCompositionLocalOf<Boolean> {
    error("No dark mode provided")
}
val LocalSeedColor = staticCompositionLocalOf<SeedColor> {
    error("No seed color provided")
}
val LocalTonalPalette = staticCompositionLocalOf<List<AppSeedColors>> {
    error("No tonal palette provided")
}

val LocalNotificationPermission = staticCompositionLocalOf<Boolean> {
    error("No notification preference provided")
}

val LocalFloatingNavBarPadding = compositionLocalOf { 0.dp }

/**
 * A no-op horizontal drag consumer. ModalNavigationDrawer wraps ALL content in a
 * full-screen `anchoredDraggable` (any unconsumed horizontal drag anywhere opens
 * it — there is no edge check), and a state-flag flip in `gesturesEnabled` loses
 * the race because the drawer starts dragging in the same pointer dispatch.
 * Consuming the drag here wins deterministically instead: descendants run first
 * in the pointer main pass, so real handlers keep priority — tab clicks, the
 * nav bar's puck drag and the histogram's scrolling all still work, and the
 * drawer only ever sees an already-consumed drag, which cancels its detection.
 *
 * Applied to the floating nav bar (dragging from the Home tab rightwards used
 * to pull the drawer open) and the attendance histogram.
 */
@Composable
fun Modifier.blocksDrawerGesture(): Modifier {
    val noOpState = rememberDraggableState { }
    return draggable(
        state = noOpState,
        orientation = Orientation.Horizontal,
        onDragStarted = { _ -> },
        onDragStopped = { },
    )
}

@Composable
fun CompositionLocals(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val context = LocalContext.current

    val autoUpdate by settingsViewModel.booleanState(SettingsKeys.AUTO_UPDATE)

    val themeMode by settingsViewModel.intState(SettingsKeys.THEME_MODE)

    val primarySeed by settingsViewModel.intState(SettingsKeys.PRIMARY_SEED)

    val secondarySeed by settingsViewModel.intState(SettingsKeys.SECONDARY_SEED)

    val tertiarySeed by settingsViewModel.intState(SettingsKeys.TERTIARY_SEED)

    val seedColor = SeedColor(primarySeed, secondarySeed, tertiarySeed)

    val isDynamicColor by settingsViewModel.booleanState(SettingsKeys.DYNAMIC_COLORS)

    val isHighContrastDarkMode by settingsViewModel.booleanState(SettingsKeys.HIGH_CONTRAST_DARK_MODE)

    val isHapticEnabled by settingsViewModel.booleanState(SettingsKeys.HAPTICS_AND_VIBRATION)

    val subjectCardCornerRadius by settingsViewModel.floatState(SettingsKeys.SUBJECT_CARD_CORNER_RADIUS)

    val subjectCardStyle by settingsViewModel.intState(SettingsKeys.SUBJECT_CARD_STYLE)

    val githubReleaseType by settingsViewModel.intState(SettingsKeys.GITHUB_RELEASE_TYPE)

    val savedVersionCode by settingsViewModel.intState(SettingsKeys.SAVED_VERSION_CODE)

    val showAttendanceStreaks by settingsViewModel.booleanState(SettingsKeys.STREAK_MODIFIER)

    val rememberCalendarMonthYear by settingsViewModel.booleanState(SettingsKeys.REMEMBER_CALENDAR_MONTH_YEAR)

    val startWeekOnMonday by settingsViewModel.booleanState(SettingsKeys.START_WEEK_ON_MONDAY)

    val enableDirectDownload by settingsViewModel.booleanState(SettingsKeys.ENABLE_DIRECT_DOWNLOAD)

    val notificationPreference by settingsViewModel.booleanState(SettingsKeys.ENABLE_NOTIFICATIONS)

    val notificationPermissionDialogShown by settingsViewModel.booleanState(SettingsKeys.NOTIFICATION_PERMISSION_DIALOG_SHOWN)

    val showGithubWarningDialog by settingsViewModel.booleanState(SettingsKeys.SHOW_GITHUB_WARNING_DIALOG)

    val fontStyle by settingsViewModel.intState(SettingsKeys.FONT_FAMILY)

    val state =
        remember(
            autoUpdate,
            themeMode,
            seedColor,
            isDynamicColor,
            isHighContrastDarkMode,
            isHapticEnabled,
            subjectCardCornerRadius,
            subjectCardStyle,
            githubReleaseType,
            savedVersionCode,
            showAttendanceStreaks,
            rememberCalendarMonthYear,
            startWeekOnMonday,
            enableDirectDownload,
            notificationPreference,
            notificationPermissionDialogShown,
            showGithubWarningDialog,
            fontStyle
        ) {
            SettingsState(
                isAutoUpdate = autoUpdate,
                themeMode = themeMode,
                isHighContrastDarkMode = isHighContrastDarkMode,
                seedColor = seedColor,
                isDynamicColor = isDynamicColor,
                isHapticEnabled = isHapticEnabled,
                subjectCardCornerRadius = subjectCardCornerRadius,
                subjectCardStyle = subjectCardStyle,
                githubReleaseType = githubReleaseType,
                savedVersionCode = savedVersionCode,
                showAttendanceStreaks = showAttendanceStreaks,
                rememberCalendarMonthYear = rememberCalendarMonthYear,
                startWeekOnMonday = startWeekOnMonday,
                enableDirectDownload = enableDirectDownload,
                notificationPreference = notificationPreference,
                notificationPermissionDialogShown = notificationPermissionDialogShown,
                showGithubWarningDialog = showGithubWarningDialog,
                fontFamily = fontStyle
            )
        }

    val isDarkTheme = when (themeMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }

    val isNotificationEnabledAndPermitted =
        notificationPreference && isNotificationPermissionGranted(context)

    val tonalPalette = listOf(
        // Green Shades
        AppSeedColors.GreenLight,
        AppSeedColors.GreenMedium,
        AppSeedColors.GreenDark,
        // Sky Blue Shades
        AppSeedColors.SkyBlueLight,
        AppSeedColors.SkyBlueMedium,
        // Blue Shades
        AppSeedColors.BlueLight,
        AppSeedColors.BlueMedium,
        // Navy Blue Shades
        AppSeedColors.NavyBlueLight,
        AppSeedColors.NavyBlueMedium,
        AppSeedColors.NavyBlueDark
    )

    val weakHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.weakHaptic()
            }
        }
    }

    val strongHaptic = remember(isHapticEnabled, view) {
        {
            if (isHapticEnabled) {
                view.strongHaptic()
            }
        }
    }

    CompositionLocalProvider(
        LocalSettings provides state,
        LocalWeakHaptic provides weakHaptic,
        LocalStrongHaptic provides strongHaptic,
        LocalSeedColor provides seedColor,
        LocalDarkMode provides isDarkTheme,
        LocalTonalPalette provides tonalPalette,
        LocalNotificationPermission provides isNotificationEnabledAndPermitted
    ) {
        content()
    }
}

@Composable
private fun SettingsViewModel.booleanState(key: SettingsKeys): State<Boolean> {
    return getBoolean(key).collectAsState(initial = key.default as Boolean)
}

@Composable
private fun SettingsViewModel.intState(key: SettingsKeys): State<Int> {
    return getInt(key).collectAsState(initial = key.default as Int)
}

@Composable
private fun SettingsViewModel.floatState(key: SettingsKeys): State<Float> {
    return getFloat(key).collectAsState(initial = key.default as Float)
}