package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model

data class TimerStateSnapshot(
    var lastSavedDuration: Long = 0L,
    var time: Long = 0L,
    var cycles: Int = 0,
    var startTime: Long = 0L,
    var pauseTime: Long = 0L,
    var pauseDuration: Long = 0L,
    var timerState: TimerState = TimerState()
) {
    fun save(
        lastSavedDuration: Long,
        time: Long,
        cycles: Int,
        startTime: Long,
        pauseTime: Long,
        pauseDuration: Long,
        timerState: TimerState
    ) {
        this.lastSavedDuration = lastSavedDuration
        this.time = time
        this.cycles = cycles
        this.startTime = startTime
        this.pauseTime = pauseTime
        this.pauseDuration = pauseDuration
        this.timerState = timerState
    }
}
