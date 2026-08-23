package `in`.jyotirmoy.attendx.calender.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.calender.domain.usecase.GetWeekDayLabelsUseCase
import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.model.StreakType
import `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance
import `in`.jyotirmoy.attendx.core.domain.model.CourseMaterial
import `in`.jyotirmoy.attendx.core.domain.model.SubjectTask
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.core.domain.repository.CourseMaterialRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.core.domain.repository.TaskRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.jyotirmoy.attendx.notification.TaskAlarmScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val courseMaterialRepository: CourseMaterialRepository,
    private val getWeekDayLabelsUseCase: GetWeekDayLabelsUseCase,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _subjectId = MutableStateFlow<Int?>(null)

    private val _selectedMonthYear = mutableStateOf(YearMonth.now())
    val selectedMonthYear: State<YearMonth> = _selectedMonthYear

    private val _markedDates = MutableStateFlow<Map<LocalDate, AttendanceStatus>>(emptyMap())
    val markedDatesFlow: StateFlow<Map<LocalDate, AttendanceStatus>> = _markedDates

    private val _streakMap = MutableStateFlow<Map<LocalDate, StreakType>>(emptyMap())
    val streakMapFlow: StateFlow<Map<LocalDate, StreakType>> = _streakMap

    fun getTasksForSubject(subjectId: Int): Flow<List<SubjectTask>> =
        taskRepository.getTasksForSubject(subjectId)

    fun getMaterialsForSubject(subjectId: Int): Flow<List<CourseMaterial>> =
        courseMaterialRepository.getMaterialsForSubject(subjectId)

    fun insertTask(task: SubjectTask) {
        viewModelScope.launch {
            val newId = taskRepository.insertTask(task)
            val taskId = if (task.id == 0 && newId > 0) newId.toInt() else task.id
            if (task.reminderMinutes != null && task.dueDate != null && !task.isCompleted) {
                try {
                    val subject = subjectRepository.getSubjectById(task.subjectId).first()
                    TaskAlarmScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = taskId,
                        subjectId = task.subjectId,
                        subjectName = subject.subject,
                        taskTitle = task.title,
                        taskDescription = task.description,
                        dueDateMillis = task.dueDate!!,
                        reminderMinutes = task.reminderMinutes!!
                    )
                } catch (_: Exception) {}
            }
        }
    }

    fun updateTask(task: SubjectTask) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
            if (task.reminderMinutes != null && task.dueDate != null && !task.isCompleted) {
                try {
                    val subject = subjectRepository.getSubjectById(task.subjectId).first()
                    TaskAlarmScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = task.id,
                        subjectId = task.subjectId,
                        subjectName = subject.subject,
                        taskTitle = task.title,
                        taskDescription = task.description,
                        dueDateMillis = task.dueDate!!,
                        reminderMinutes = task.reminderMinutes!!
                    )
                } catch (_: Exception) {}
            } else {
                TaskAlarmScheduler.cancelTaskReminder(context, task.id)
            }
        }
    }

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

    fun insertMaterial(material: CourseMaterial) {
        viewModelScope.launch { courseMaterialRepository.insertMaterial(material) }
    }

    fun deleteMaterial(material: CourseMaterial) {
        viewModelScope.launch { courseMaterialRepository.deleteMaterial(material) }
    }

    init {
        savedStateHandle.get<Int>("subjectId")?.also { id ->
            _subjectId.value = id
            loadAttendanceData(id)
        }
    }

    fun updateMonthYear(year: Int, month: Int) {
        _selectedMonthYear.value = YearMonth.of(year, month)
    }

    fun upsert(att: AttendanceEntity, newStatus: AttendanceStatus) {
        viewModelScope.launch {
            attendanceRepository.insertAttendance(att.copy(status = newStatus))
        }
    }

    fun clear(subjectId: Int, date: String) {
        viewModelScope.launch { attendanceRepository.deleteAttendance(subjectId, date) }
    }

    fun getMonthlyAttendanceCounts(
        subjectId: Int, year: Int, month: Int
    ): Flow<SubjectAttendance> {
        val presentFlow = attendanceRepository.getPresentCountForMonth(subjectId, year, month)
        val absentFlow = attendanceRepository.getAbsentCountForMonth(subjectId, year, month)
        return combine(presentFlow, absentFlow) { p, a ->
            SubjectAttendance(p, a, p + a)
        }
    }

    fun getSubjectEntityById(subjectId: Int): Flow<SubjectEntity> {
        return subjectRepository.getSubjectById(subjectId)
    }

    fun saveMonthYearForSubject(subjectId: Int) {
        viewModelScope.launch {
            subjectRepository.updateSavedMonthYear(
                subjectId = subjectId,
                month = _selectedMonthYear.value.monthValue,
                year = _selectedMonthYear.value.year
            )
        }
    }

    fun resetYearMonthToCurrent(){
        _selectedMonthYear.value = YearMonth.now()
    }

    private fun loadAttendanceData(subjectId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            attendanceRepository.getAttendanceForSubject(subjectId)
                .collect { list ->
                    val newMap = list
                        .mapNotNull { e ->
                            if (e.status == AttendanceStatus.UNMARKED) null
                            else LocalDate.parse(e.date) to e.status
                        }
                        .toMap()
                    _markedDates.value = newMap

                    _streakMap.value = calculateStreaks(newMap)
                }
        }
    }

    fun onStatusChange(subjectId: Int, date: String, newStatus: AttendanceStatus?) {
        when (newStatus) {
            AttendanceStatus.PRESENT, AttendanceStatus.ABSENT -> {
                upsert(
                    AttendanceEntity(subjectId = subjectId, date = date),
                    newStatus
                )
            }

            AttendanceStatus.UNMARKED, null -> {
                clear(subjectId, date)
            }
        }
    }


    private fun calculateStreaks(
        marked: Map<LocalDate, AttendanceStatus>
    ): Map<LocalDate, StreakType> {
        val streaks = mutableMapOf<LocalDate, StreakType>()

        marked.entries.groupBy({ it.value }, { it.key }).forEach { (status, dates) ->
            if (status == AttendanceStatus.UNMARKED) return@forEach

            val sorted = dates.sorted()
            var i = 0
            while (i < sorted.size) {
                var end = i
                while (end + 1 < sorted.size && sorted[end + 1] == sorted[end].plusDays(1)) {
                    end++
                }
                val len = end - i + 1
                for (j in i..end) {
                    val d = sorted[j]
                    streaks[d] = when {
                        len < 3 -> StreakType.NONE
                        j == i -> StreakType.START
                        j == end -> StreakType.END
                        else -> StreakType.MIDDLE
                    }
                }
                i = end + 1
            }
        }
        return streaks
    }

    private val _weekdayLabels = mutableStateOf<List<String>>(emptyList())
    val weekdayLabels: State<List<String>> = _weekdayLabels

    fun loadWeekdayLabels(isMondayFirstDay: Boolean) {
        _weekdayLabels.value = getWeekDayLabelsUseCase(isMondayFirstDay)
    }
}
