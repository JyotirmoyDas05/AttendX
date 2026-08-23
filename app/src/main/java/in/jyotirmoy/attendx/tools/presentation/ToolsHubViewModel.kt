package `in`.jyotirmoy.attendx.tools.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.core.domain.model.SubjectTask
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.core.domain.repository.TaskRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.jyotirmoy.attendx.notification.TaskAlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolsHubViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val pendingTasks: StateFlow<List<SubjectTask>> = taskRepository.getAllPendingTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskCompletion(taskId, isCompleted)
            if (isCompleted) {
                TaskAlarmScheduler.cancelTaskReminder(context, taskId)
            }
        }
    }

    fun deleteTask(task: SubjectTask) {
        viewModelScope.launch {
            TaskAlarmScheduler.cancelTaskReminder(context, task.id)
            taskRepository.deleteTask(task)
        }
    }
}
