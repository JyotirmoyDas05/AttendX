package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model

import androidx.compose.runtime.Immutable

@Immutable
data class TimerState(
    val timerMode: TimerMode = TimerMode.FOCUS,
    val timeStr: String = "25:00",
    val totalTime: Long = 25 * 60,
    val timerRunning: Boolean = false,
    val nextTimerMode: TimerMode = TimerMode.SHORT_BREAK,
    val nextTimeStr: String = "05:00",
    val showBrandTitle: Boolean = true,
    val currentFocusCount: Int = 1,
    val totalFocusCount: Int = 4,
    val alarmRinging: Boolean = false,
    val serviceRunning: Boolean = false,
    val infiniteFocus: Boolean = false
)

enum class TimerMode {
    FOCUS, SHORT_BREAK, LONG_BREAK, BRAND
}
