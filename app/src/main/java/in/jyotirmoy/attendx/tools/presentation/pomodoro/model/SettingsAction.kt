package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model

import androidx.compose.ui.graphics.Color

sealed interface SettingsAction {
    data class SaveAlarmEnabled(val enabled: Boolean) : SettingsAction
    data class SaveVibrateEnabled(val enabled: Boolean) : SettingsAction
    data class SaveBlackTheme(val enabled: Boolean) : SettingsAction
    data class SaveAodEnabled(val enabled: Boolean) : SettingsAction
    data class SaveDndEnabled(val enabled: Boolean) : SettingsAction
    data class SaveMediaVolumeForAlarm(val enabled: Boolean) : SettingsAction
    data class SaveSingleProgressBar(val enabled: Boolean) : SettingsAction
    data class SaveAutostartNextSession(val enabled: Boolean) : SettingsAction
    data class SaveSecureAod(val enabled: Boolean) : SettingsAction
    data class SaveAlarmSound(val uri: String?) : SettingsAction
    data class SaveTheme(val theme: String) : SettingsAction
    data class SaveColorScheme(val color: Color) : SettingsAction

    data class SaveFocusGoal(val goal: Long) : SettingsAction
    data class SaveFocusTime(val time: Long) : SettingsAction
    data class SaveShortBreakTime(val time: Long) : SettingsAction
    data class SaveLongBreakTime(val time: Long) : SettingsAction
    data class SaveSessionLength(val length: Int) : SettingsAction

    data class SaveVibrationOnDuration(val duration: Long) : SettingsAction
    data class SaveVibrationOffDuration(val duration: Long) : SettingsAction
    data class SaveVibrationAmplitude(val amplitude: Int) : SettingsAction

    data object AskEraseData : SettingsAction
    data object CancelEraseData : SettingsAction
    data object EraseData : SettingsAction
}
