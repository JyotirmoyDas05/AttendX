package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model

sealed interface TimerAction {
    data class SkipTimer(val fromButton: Boolean) : TimerAction
    data class SetInfiniteFocus(val value: Boolean) : TimerAction
    data object ResetTimer : TimerAction
    data object UndoReset : TimerAction
    data object StopAlarm : TimerAction
    data object ToggleTimer : TimerAction
}
