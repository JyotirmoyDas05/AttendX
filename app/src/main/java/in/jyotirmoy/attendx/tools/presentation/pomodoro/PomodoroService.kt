package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import `in`.jyotirmoy.attendx.MainActivity
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.tools.data.repository.StatRepository
import `in`.jyotirmoy.attendx.tools.data.repository.StateRepository
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerMode
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util.millisecondsToStr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.text.Typography.middleDot

@AndroidEntryPoint
class PomodoroService : Service() {

    @Inject
    lateinit var stateRepository: StateRepository

    @Inject
    lateinit var statRepository: StatRepository

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val _timerState by lazy { stateRepository.timerState }
    private val _settingsState by lazy { stateRepository.settingsState }
    private val _time by lazy { stateRepository.time }

    private var time: Long
        get() = _time.value
        set(value) = _time.update { value }

    private var cycles = 0
    private var startTime = 0L
    private var pauseTime = 0L
    private var pauseDuration = 0L
    private var lastSavedDuration = 0L

    private val timerStateSnapshot by lazy { stateRepository.timerStateSnapshot }
    private val saveLock = Mutex()

    private var job = SupervisorJob()
    private val timerScope = CoroutineScope(Dispatchers.IO + job)
    private val skipScope = CoroutineScope(Dispatchers.IO + job)
    private var autoAlarmStopScope: Job? = null
    private var alarm: MediaPlayer? = null

    private val notificationChannelId = "attendx_pomodoro_timer"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        stateRepository.timerState.update { it.copy(serviceRunning = true) }
        alarm = initializeMediaPlayer()
    }

    override fun onDestroy() {
        stateRepository.timerState.update { it.copy(serviceRunning = false) }
        runBlocking {
            job.cancel()
            saveTimeToDb()
            lastSavedDuration = 0
            notificationManager.cancel(NOTIFICATION_ID)
            alarm?.release()
            vibrator.cancel()
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.TOGGLE.toString() -> {
                startForegroundServiceInternal()
                toggleTimer()
            }
            Actions.RESET.toString() -> {
                if (_timerState.value.timerRunning) toggleTimer()
                skipScope.launch {
                    resetTimer()
                    stopForegroundServiceInternal()
                }
            }
            Actions.UNDO_RESET.toString() -> undoReset()
            Actions.SKIP.toString() -> skipScope.launch { skipTimer(true) }
            Actions.STOP_ALARM.toString() -> stopAlarm()
            Actions.UPDATE_ALARM_TONE.toString() -> updateAlarmTone()
        }
        return START_STICKY
    }

    private fun toggleTimer() {
        if (_timerState.value.timerRunning) {
            showTimerNotification(time.toInt(), paused = true)
            _timerState.update { it.copy(timerRunning = false) }
            pauseTime = SystemClock.elapsedRealtime()
        } else {
            _timerState.update { it.copy(timerRunning = true) }
            if (pauseTime != 0L) {
                pauseDuration += SystemClock.elapsedRealtime() - pauseTime
            }

            var iterations = -1

            timerScope.launch {
                while (true) {
                    if (!_timerState.value.timerRunning) break
                    if (startTime == 0L) startTime = SystemClock.elapsedRealtime()

                    val settingsState = _settingsState.value
                    val timerState = _timerState.value

                    val focusTime = if (!timerState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE
                    time = when (_timerState.value.timerMode) {
                        TimerMode.FOCUS -> focusTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)
                        TimerMode.SHORT_BREAK -> settingsState.shortBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)
                        else -> settingsState.longBreakTime - (SystemClock.elapsedRealtime() - startTime - pauseDuration)
                    }

                    iterations = (iterations + 1) % 60

                    if (iterations == 0) {
                        showTimerNotification(time.toInt())
                    }

                    if (time < 0) {
                        skipTimer()
                        _timerState.update { it.copy(timerRunning = false) }
                        break
                    } else {
                        _timerState.update { currentState ->
                            currentState.copy(
                                timeStr = if (!currentState.infiniteFocus || currentState.timerMode != TimerMode.FOCUS)
                                    millisecondsToStr(time)
                                else millisecondsToStr(currentState.totalTime - time)
                            )
                        }

                        val totalTime = _timerState.value.totalTime
                        if (totalTime - time < lastSavedDuration) {
                            lastSavedDuration = 0
                        }
                        if (totalTime - time - lastSavedDuration > 60000) {
                            saveTimeToDb()
                        }
                    }

                    delay(100)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showTimerNotification(
        remainingTime: Int,
        paused: Boolean = false,
        complete: Boolean = false
    ) {
        val settingsState = _settingsState.value
        val timerState = _timerState.value

        val totalTime = when (timerState.timerMode) {
            TimerMode.FOCUS -> settingsState.focusTime.toInt()
            TimerMode.SHORT_BREAK -> settingsState.shortBreakTime.toInt()
            else -> settingsState.longBreakTime.toInt()
        }

        val currentTimer = when (timerState.timerMode) {
            TimerMode.FOCUS -> getString(R.string.focus)
            TimerMode.SHORT_BREAK -> getString(R.string.short_break)
            else -> getString(R.string.long_break)
        }

        val nextTimer = when (timerState.nextTimerMode) {
            TimerMode.FOCUS -> getString(R.string.focus)
            TimerMode.SHORT_BREAK -> getString(R.string.short_break)
            else -> getString(R.string.long_break)
        }

        val remainingMinutes = (remainingTime.toFloat() / 60000f).toInt().coerceAtLeast(0)
        val remainingTimeString = if (remainingMinutes < 1) "< 1" else "$remainingMinutes"

        val title = if (!complete) {
            "$currentTimer  $middleDot  ${
                if (timerState.timerMode == TimerMode.FOCUS && timerState.infiniteFocus)
                    getString(R.string.infinite)
                else
                    getString(R.string.min_remaining_notification, remainingTimeString)
            }" + if (paused) "  $middleDot  ${getString(R.string.paused)}" else ""
        } else "$currentTimer $middleDot ${getString(R.string.completed)}"

        val content = getString(R.string.up_next_notification, nextTimer, timerState.nextTimeStr)

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_pomodoro_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentPendingIntent)
            .setOngoing(!complete && timerState.timerRunning)
            .setOnlyAlertOnce(true)
            .setProgress(totalTime, (totalTime - remainingTime).coerceIn(0, totalTime), false)
            .setShowWhen(false)

        if (complete) {
            val stopAlarmIntent = Intent(this, PomodoroService::class.java).apply {
                action = Actions.STOP_ALARM.toString()
            }
            val stopAlarmPending = PendingIntent.getService(
                this, 1, stopAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.stop, getString(R.string.stop_alarm), stopAlarmPending)
        } else {
            val toggleIntent = Intent(this, PomodoroService::class.java).apply {
                action = Actions.TOGGLE.toString()
            }
            val togglePending = PendingIntent.getService(
                this, 2, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val toggleIcon = if (timerState.timerRunning) R.drawable.pause else R.drawable.play
            val toggleLabel = if (timerState.timerRunning) getString(R.string.pause) else getString(R.string.start)
            builder.addAction(toggleIcon, toggleLabel, togglePending)

            val skipIntent = Intent(this, PomodoroService::class.java).apply {
                action = Actions.SKIP.toString()
            }
            val skipPending = PendingIntent.getService(
                this, 3, skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.skip_next, getString(R.string.skip), skipPending)
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        if (complete) {
            startAlarm()
            _timerState.update { it.copy(alarmRinging = true) }
        }
    }

    private suspend fun resetTimer() {
        val settingsState = _settingsState.value
        val timerState = _timerState.value

        timerStateSnapshot.save(
            lastSavedDuration,
            time,
            cycles,
            startTime,
            pauseTime,
            pauseDuration,
            timerState
        )

        saveTimeToDb()
        lastSavedDuration = 0
        cycles = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        time = if (!timerState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE

        _timerState.update { currentState ->
            currentState.copy(
                timerMode = TimerMode.FOCUS,
                timeStr = if (!currentState.infiniteFocus) millisecondsToStr(time) else millisecondsToStr(0),
                totalTime = time,
                nextTimerMode = if (settingsState.sessionLength > 1) TimerMode.SHORT_BREAK else TimerMode.LONG_BREAK,
                nextTimeStr = millisecondsToStr(if (settingsState.sessionLength > 1) settingsState.shortBreakTime else settingsState.longBreakTime),
                currentFocusCount = 1,
                totalFocusCount = settingsState.sessionLength
            )
        }
    }

    private fun undoReset() {
        lastSavedDuration = timerStateSnapshot.lastSavedDuration
        time = timerStateSnapshot.time
        cycles = timerStateSnapshot.cycles
        startTime = timerStateSnapshot.startTime
        pauseTime = timerStateSnapshot.pauseTime
        pauseDuration = timerStateSnapshot.pauseDuration
        _timerState.update { timerStateSnapshot.timerState }
    }

    private suspend fun skipTimer(fromButton: Boolean = false) {
        val settingsState = _settingsState.value
        saveTimeToDb()
        showTimerNotification(0, paused = true, complete = !fromButton)
        lastSavedDuration = 0
        startTime = 0L
        pauseTime = 0L
        pauseDuration = 0L

        cycles = (cycles + 1) % (settingsState.sessionLength * 2)

        if (cycles % 2 == 0) {
            _timerState.update { currentState ->
                time = if (!currentState.infiniteFocus) settingsState.focusTime else Long.MAX_VALUE
                currentState.copy(
                    timerMode = TimerMode.FOCUS,
                    timeStr = if (!currentState.infiniteFocus) millisecondsToStr(time) else millisecondsToStr(0),
                    totalTime = time,
                    nextTimerMode = if (cycles == (settingsState.sessionLength - 1) * 2) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    nextTimeStr = if (cycles == (settingsState.sessionLength - 1) * 2) millisecondsToStr(settingsState.longBreakTime)
                    else millisecondsToStr(settingsState.shortBreakTime),
                    currentFocusCount = cycles / 2 + 1,
                    totalFocusCount = settingsState.sessionLength
                )
            }
        } else {
            val longBreak = cycles == (settingsState.sessionLength * 2) - 1
            time = if (longBreak) settingsState.longBreakTime else settingsState.shortBreakTime

            _timerState.update { currentState ->
                currentState.copy(
                    timerMode = if (longBreak) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK,
                    timeStr = millisecondsToStr(time),
                    totalTime = time,
                    nextTimerMode = TimerMode.FOCUS,
                    nextTimeStr = if (!currentState.infiniteFocus) millisecondsToStr(settingsState.focusTime) else getString(R.string.infinite)
                )
            }
        }
    }

    private fun startAlarm() {
        val settingsState = _settingsState.value
        if (settingsState.alarmEnabled) {
            try {
                alarm?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        autoAlarmStopScope = CoroutineScope(Dispatchers.IO).launch {
            delay(60_000L)
            stopAlarm(fromAutoStop = true)
        }

        if (settingsState.vibrateEnabled && vibrator.hasVibrator()) {
            val timings = longArrayOf(
                0,
                settingsState.vibrationOnDuration,
                settingsState.vibrationOffDuration,
                settingsState.vibrationOnDuration
            )
            val amplitudes = intArrayOf(
                0,
                settingsState.vibrationAmplitude,
                0,
                settingsState.vibrationAmplitude
            )
            val repeat = 2
            val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)
            vibrator.vibrate(effect)
        }
    }

    fun stopAlarm(fromAutoStop: Boolean = false) {
        val settingsState = _settingsState.value
        autoAlarmStopScope?.cancel()

        if (settingsState.alarmEnabled) {
            try {
                alarm?.pause()
                alarm?.seekTo(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (settingsState.vibrateEnabled) {
            vibrator.cancel()
        }

        _timerState.update { it.copy(alarmRinging = false) }
        showTimerNotification(
            when (_timerState.value.timerMode) {
                TimerMode.FOCUS -> settingsState.focusTime.toInt()
                TimerMode.SHORT_BREAK -> settingsState.shortBreakTime.toInt()
                else -> settingsState.longBreakTime.toInt()
            },
            paused = true,
            complete = false
        )

        if (settingsState.autostartNextSession && !fromAutoStop) {
            toggleTimer()
        }
    }

    private fun initializeMediaPlayer(): MediaPlayer? {
        val settingsState = _settingsState.value
        return try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(
                            if (settingsState.mediaVolumeForAlarm) AudioAttributes.USAGE_MEDIA
                            else AudioAttributes.USAGE_ALARM
                        )
                        .build()
                )
                settingsState.alarmSoundUri?.let { uriStr ->
                    if (uriStr.isNotBlank()) {
                        setDataSource(applicationContext, uriStr.toUri())
                        prepare()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updateAlarmTone() {
        alarm?.release()
        alarm = initializeMediaPlayer()
    }

    private suspend fun saveTimeToDb() {
        saveLock.withLock {
            val elapsedTime = _timerState.value.totalTime - time
            when (_timerState.value.timerMode) {
                TimerMode.FOCUS -> statRepository.addFocusTime(
                    (elapsedTime - lastSavedDuration).coerceAtLeast(0L)
                )
                else -> statRepository.addBreakTime(
                    (elapsedTime - lastSavedDuration).coerceAtLeast(0L)
                )
            }
            lastSavedDuration = elapsedTime
        }
    }

    private fun startForegroundServiceInternal() {
        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_pomodoro_notification)
            .setContentTitle(getString(R.string.pomodoro_timer))
            .setContentText(getString(R.string.start))
            .setOngoing(true)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun stopForegroundServiceInternal() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status of active Pomodoro focus sessions"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }

    enum class Actions {
        TOGGLE, SKIP, RESET, UNDO_RESET, STOP_ALARM, UPDATE_ALARM_TONE
    }
}
