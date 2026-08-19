package `in`.jyotirmoy.attendx.tools.presentation.pomodoro

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.jyotirmoy.attendx.tools.data.model.Topic
import `in`.jyotirmoy.attendx.tools.data.repository.StateRepository
import `in`.jyotirmoy.attendx.tools.data.repository.TopicRepository
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.SettingsAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerAction
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerMode
import `in`.jyotirmoy.attendx.tools.presentation.pomodoro.model.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stateRepository: StateRepository,
    private val topicRepository: TopicRepository
) : ViewModel() {

    val timerState: StateFlow<TimerState> = stateRepository.timerState
    val settingsState = stateRepository.settingsState
    val time = stateRepository.time

    val topics: StateFlow<List<Topic>> = topicRepository.getAllTopics()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf(Topic(id = 1, name = "Default", colorHex = "#A8C7FA"))
        )

    private val _selectedTopic = MutableStateFlow(Topic(id = 1, name = "Default", colorHex = "#A8C7FA"))
    val selectedTopic: StateFlow<Topic> = _selectedTopic.asStateFlow()

    private val _isCreateTopicSheetOpen = MutableStateFlow(false)
    val isCreateTopicSheetOpen: StateFlow<Boolean> = _isCreateTopicSheetOpen.asStateFlow()

    private val _isEditTopicSheetOpen = MutableStateFlow(false)
    val isEditTopicSheetOpen: StateFlow<Boolean> = _isEditTopicSheetOpen.asStateFlow()

    init {
        viewModelScope.launch {
            topicRepository.ensureDefaultTopic()
        }
    }

    val progress: StateFlow<Float> = combine(
        stateRepository.time,
        stateRepository.timerState,
        stateRepository.settingsState
    ) { time, timerState, settingsState ->
        val total = when (timerState.timerMode) {
            TimerMode.FOCUS -> settingsState.focusTime
            TimerMode.SHORT_BREAK -> settingsState.shortBreakTime
            else -> settingsState.longBreakTime
        }
        if (total <= 0) 1f else ((total - time).toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun openCreateTopicSheet() {
        _isCreateTopicSheetOpen.value = true
    }

    fun closeCreateTopicSheet() {
        _isCreateTopicSheetOpen.value = false
    }

    fun openEditTopicSheet() {
        _isEditTopicSheetOpen.value = true
    }

    fun closeEditTopicSheet() {
        _isEditTopicSheetOpen.value = false
    }

    fun selectTopic(topic: Topic) {
        _selectedTopic.value = topic
        viewModelScope.launch {
            stateRepository.settingsState.update {
                it.copy(
                    focusTime = topic.focusTime,
                    shortBreakTime = topic.shortBreakTime,
                    longBreakTime = topic.longBreakTime
                )
            }
            if (!timerState.value.timerRunning) {
                stateRepository.resetTimer(topic.focusTime)
            }
        }
    }

    fun saveTopic(topic: Topic) {
        viewModelScope.launch {
            val id = topicRepository.saveTopic(topic)
            val updated = topic.copy(id = if (topic.id != 0L) topic.id else id)
            selectTopic(updated)
            closeCreateTopicSheet()
            closeEditTopicSheet()
        }
    }

    fun deleteTopic(topic: Topic) {
        viewModelScope.launch {
            if (topic.id != 1L) {
                topicRepository.deleteTopic(topic)
                val defaultTopic = topicRepository.ensureDefaultTopic()
                selectTopic(defaultTopic)
            }
        }
    }

    fun onAction(action: TimerAction) {
        when (action) {
            is TimerAction.ToggleTimer -> {
                sendServiceIntent(PomodoroService.Actions.TOGGLE.toString())
            }
            is TimerAction.ResetTimer -> {
                stateRepository.resetTimer()
                if (timerState.value.serviceRunning) {
                    sendServiceIntent(PomodoroService.Actions.RESET.toString())
                }
            }
            is TimerAction.UndoReset -> {
                stateRepository.undoReset()
                if (timerState.value.serviceRunning) {
                    sendServiceIntent(PomodoroService.Actions.UNDO_RESET.toString())
                }
            }
            is TimerAction.SkipTimer -> {
                sendServiceIntent(PomodoroService.Actions.SKIP.toString())
            }
            is TimerAction.StopAlarm -> {
                sendServiceIntent(PomodoroService.Actions.STOP_ALARM.toString())
            }
            is TimerAction.SetInfiniteFocus -> {
                stateRepository.timerState.update { it.copy(infiniteFocus = action.value) }
            }
        }
    }

    private fun sendServiceIntent(actionName: String) {
        val intent = Intent(context, PomodoroService::class.java).apply {
            action = actionName
        }
        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
