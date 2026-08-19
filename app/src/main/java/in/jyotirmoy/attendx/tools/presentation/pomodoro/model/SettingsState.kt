package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model

import android.provider.Settings
import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val theme: String = "auto",
    val colorScheme: String = "dynamic",
    val blackTheme: Boolean = false,
    val aodEnabled: Boolean = false,
    val alarmEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val dndEnabled: Boolean = false,
    val mediaVolumeForAlarm: Boolean = false,
    val singleProgressBar: Boolean = false,
    val autostartNextSession: Boolean = false,
    val secureAod: Boolean = true,
    val isShowingEraseDataDialog: Boolean = false,

    val vibrationOnDuration: Long = 1000L,
    val vibrationOffDuration: Long = 1000L,
    val vibrationAmplitude: Int = -1,

    val focusTime: Long = 25 * 60 * 1000L,
    val shortBreakTime: Long = 5 * 60 * 1000L,
    val longBreakTime: Long = 15 * 60 * 1000L,
    val focusGoal: Long = 0L,

    val sessionLength: Int = 4,

    val alarmSoundUri: String? = (Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_RINGTONE_URI)?.toString()
)
