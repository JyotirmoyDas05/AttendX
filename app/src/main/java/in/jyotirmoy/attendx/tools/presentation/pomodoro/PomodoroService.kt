package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PomodoroService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow(TimerState())
    val timerState = _timerState.asStateFlow()

    override fun onBind(intent: Intent?): IBinder = binder

    inner class LocalBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimerService()
            ACTION_RESET -> resetToMode(_timerState.value.mode)
        }
        return START_STICKY
    }

    fun startTimer(durationMillis: Long) {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = true,
            totalTime = durationMillis,
            timeLeft = durationMillis,
            isPaused = false
        )
        startForegroundService()

        timerJob = serviceScope.launch {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + durationMillis

            while (isActive && System.currentTimeMillis() < endTime) {
                if (!_timerState.value.isPaused) {
                    val remaining = endTime - System.currentTimeMillis()
                    _timerState.value = _timerState.value.copy(timeLeft = remaining)
                    updateNotification(remaining, false)
                    delay(1000) // Update every second
                } else {
                    // Adjust end time to account for pause
                    val pauseStart = System.currentTimeMillis()
                    while (isActive && _timerState.value.isPaused) {
                        delay(100)
                    }
                    val pauseDuration = System.currentTimeMillis() - pauseStart
                    // this logic is flawed for a simple delay loop, better to use ticker or logic based on timeLeft
                    // simpler approach: explicit tick
                }
            }
            
            // If we are here, logic is complicated. Let's switch to a simpler ticker.
        }
        // Restarting with simpler logic
        startTicker(durationMillis)
    }

    private fun startTicker(durationMillis: Long) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var remaining = durationMillis
            val tickInterval = 100L
            var lastTick = System.currentTimeMillis()

            while (isActive && remaining > 0) {
                val now = System.currentTimeMillis()
                if (!_timerState.value.isPaused) {
                    val elapsed = now - lastTick
                    remaining -= elapsed
                    if (remaining < 0) remaining = 0
                    
                    _timerState.value = _timerState.value.copy(timeLeft = remaining)
                    
                    if (remaining % 1000 < tickInterval * 2) { // Update notification roughly every second
                         updateNotification(remaining, false)
                    }
                }
                lastTick = now
                delay(tickInterval)
            }

            if (remaining <= 0) {
                stopTimerService()
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    timeLeft = 0,
                    isPaused = false,
                    isCompleted = true
                )
                updateNotification(0, true)
            }
        }
    }


    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isPaused = true)
        updateNotification(_timerState.value.timeLeft, false)
    }

    fun resumeTimer() {
        _timerState.value = _timerState.value.copy(isPaused = false)
    }

    fun stopTimerService() {
        timerJob?.cancel()
        _timerState.value = TimerState() // Reset
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun resetToMode(mode: PomodoroMode) {
        timerJob?.cancel()
        _timerState.value = TimerState(
            mode = mode,
            totalTime = mode.durationMillis,
            timeLeft = mode.durationMillis,
            isRunning = false,
            isPaused = false,
            isCompleted = false
        )
        // Update notification to show new mode?
        // Maybe start foreground service with initial state if we want persistent notification, 
        // but typically we stop service when not running.
        // For now, just update state. Service might be stopped if not running.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
             stopForeground(true)
        }
        stopSelf() // Stop service when resetting/changing mode if not running
    }

    private fun startForegroundService() {
        startForeground(NOTIFICATION_ID, buildNotification(_timerState.value.timeLeft, false))
    }

    private fun updateNotification(timeLeft: Long, isDone: Boolean) {
        val notification = buildNotification(timeLeft, isDone)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(timeLeft: Long, isDone: Boolean): android.app.Notification {
        val timeString = if (isDone) "Time's up!" else formatTime(timeLeft)
        val contentText = if (isDone) "Pomodoro session completed." else "Stay focused!"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(timeString)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(!isDone)
            .setOnlyAlertOnce(true)

        if (!isDone) {
            // Pause/Resume action
            val pauseAction = if (_timerState.value.isPaused) ACTION_RESUME else ACTION_PAUSE
            val pauseIntent = Intent(this, PomodoroService::class.java).apply { action = pauseAction }
            val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pauseText = if (_timerState.value.isPaused) "▶ Resume" else "⏸ Pause"
            builder.addAction(0, pauseText, pausePendingIntent)

            // Reset action
            val resetIntent = Intent(this, PomodoroService::class.java).apply { action = ACTION_RESET }
            val resetPendingIntent = PendingIntent.getService(this, 3, resetIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, "↺ Reset", resetPendingIntent)

            // Exit action
            val stopIntent = Intent(this, PomodoroService::class.java).apply { action = ACTION_STOP }
            val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(0, "✕ Exit", stopPendingIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "pomodoro_channel"
        const val NOTIFICATION_ID = 101
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESET = "ACTION_RESET"
    }
}

enum class PomodoroMode(val label: String, val durationMillis: Long) {
    FOCUS("Focus", 25 * 60 * 1000L),
    SHORT_BREAK("Short Break", 5 * 60 * 1000L),
    LONG_BREAK("Long Break", 15 * 60 * 1000L)
}

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val mode: PomodoroMode = PomodoroMode.FOCUS,
    val totalTime: Long = 25 * 60 * 1000L,
    val timeLeft: Long = 25 * 60 * 1000L
)
