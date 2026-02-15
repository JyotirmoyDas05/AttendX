package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var pomodoroService: PomodoroService? = null
    private var isBound = false

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PomodoroService.LocalBinder
            pomodoroService = binder.getService()
            isBound = true
            
            viewModelScope.launch {
                pomodoroService?.timerState?.collect { state ->
                    _uiState.value = state
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            pomodoroService = null
            isBound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        Intent(context, PomodoroService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun startTimer() {
        if (isBound) {
            val currentState = _uiState.value
            // If already running, do nothing or handle resume? 
            // Reuse existing duration or restart if completed?
            if (!currentState.isRunning && !currentState.isPaused) {
                // If starting fresh
                startTimer(currentState.mode.durationMillis)
            } else if (currentState.isPaused) {
                resumeTimer()
            }
        }
    }

    private fun startTimer(durationMillis: Long) {
        if (isBound) {
            val intent = Intent(context, PomodoroService::class.java)
            context.startForegroundService(intent) // Start service first
            pomodoroService?.startTimer(durationMillis)
        }
    }

    fun skipSession() {
        val currentMode = _uiState.value.mode
        val nextMode = when (currentMode) {
            PomodoroMode.FOCUS -> PomodoroMode.SHORT_BREAK // Simple toggle for now
            PomodoroMode.SHORT_BREAK -> PomodoroMode.FOCUS
            PomodoroMode.LONG_BREAK -> PomodoroMode.FOCUS
        }
        changeMode(nextMode)
    }
    
    fun changeMode(mode: PomodoroMode) {
        stopTimer()
        // We need to update the UI state immediately even if service is stopped
        // But service is source of truth. We should update service state if bound?
        // Or just local state? 
        // Ideally tell service to reset to this mode.
        pomodoroService?.resetToMode(mode)
    }

    fun resetTimer() {
        stopTimer()
        pomodoroService?.resetToMode(_uiState.value.mode)
    }


    fun pauseTimer() {
        pomodoroService?.pauseTimer()
    }

    fun resumeTimer() {
        pomodoroService?.resumeTimer()
    }

    fun stopTimer() {
        pomodoroService?.stopTimerService()
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}
