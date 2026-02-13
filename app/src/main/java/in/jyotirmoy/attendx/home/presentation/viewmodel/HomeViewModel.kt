package `in`.jyotirmoy.attendx.home.presentation.viewmodel

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance
import `in`.jyotirmoy.attendx.core.domain.model.SubjectError
import `in`.jyotirmoy.attendx.core.domain.model.TotalAttendance
import `in`.jyotirmoy.attendx.core.domain.model.ClassSchedule
import `in`.jyotirmoy.attendx.core.domain.model.toClassSchedule
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import `in`.jyotirmoy.attendx.notification.createAppNotificationSettingsIntent
import `in`.jyotirmoy.attendx.notification.ClassNotificationScheduler
import `in`.jyotirmoy.attendx.notification.TimetableAlarmScheduler
import `in`.jyotirmoy.attendx.settings.presentation.event.SettingsUiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val subjectRepository: SubjectRepository,
    private val attendanceRepository: AttendanceRepository,
    private val timetableRepository: TimeTableRepository
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _subject = MutableStateFlow("")
    val subject: StateFlow<String> = _subject

    private val _subjectCode = MutableStateFlow("")
    val subjectCode: StateFlow<String> = _subjectCode

    private val _histogramLabel = MutableStateFlow("")
    val histogramLabel: StateFlow<String> = _histogramLabel

    private val _subjectError = MutableStateFlow<SubjectError>(SubjectError.None)
    val subjectError: StateFlow<SubjectError> = _subjectError

    fun setSubjectNamePlaceholder(value: String) {
        if (_subject.value.isBlank()) {
            _subject.value = value
        }
    }

    fun onSubjectChange(newValue: String) {
        _subject.value = newValue
        _subjectError.value = SubjectError.None
    }

    fun onSubjectCodeChange(newValue: String) {
        _subjectCode.value = newValue
    }

    fun onHistogramLabelChange(newValue: String) {
        // Limit to 5 characters
        if (newValue.length <= 5) {
            _histogramLabel.value = newValue
        }
    }

    val subjectList: StateFlow<List<SubjectEntity>> = subjectRepository.getAllSubjects().stateIn(
        viewModelScope,
        SharingStarted.Eagerly, emptyList()
    )

    fun addSubject(onSuccess: () -> Unit) {
        val isSubjectInvalid = _subject.value.trim().isBlank()
        if (isSubjectInvalid) {
            _subjectError.value = SubjectError.Empty
            return
        }

        viewModelScope.launch {
            val isSubjectExists = subjectRepository.isSubjectExists(_subject.value.trim()).first()
            if (isSubjectExists) {
                _subjectError.value = SubjectError.AlreadyExists
            } else {
                subjectRepository.insertSubject(
                    SubjectEntity(
                        subject = _subject.value.trim(),
                        subjectCode = _subjectCode.value.trim().takeIf { it.isNotBlank() },
                        histogramLabel = _histogramLabel.value.trim().takeIf { it.isNotBlank() }
                    )
                )
                _subject.value = ""
                _subjectCode.value = ""
                _histogramLabel.value = ""
                onSuccess()
            }
        }
    }

    fun resetInputFields() {
        _subject.value = ""
        _subjectCode.value = ""
        _histogramLabel.value = ""
        _subjectError.value = SubjectError.None
    }

    fun updateSubject(subjectId: Int, onSuccess: () -> Unit) {
        val isSubjectInvalid = _subject.value.trim().isBlank()
        if (isSubjectInvalid) {
            _subjectError.value = SubjectError.Empty
            return
        }

        viewModelScope.launch {
            // Get the current subject to compare names
            val currentSubject = subjectRepository.getSubjectById(subjectId).first()
            val newName = _subject.value.trim()
            val currentName = currentSubject?.subject ?: ""
            
            // Only check if subject exists if the name has actually changed
            val nameChanged = newName != currentName
            val isSubjectExists = if (nameChanged) {
                subjectRepository.isSubjectExists(newName).first()
            } else {
                false // Name hasn't changed, so no conflict
            }
            
            if (isSubjectExists) {
                _subjectError.value = SubjectError.AlreadyExists
            } else {
                subjectRepository.updateSubject(
                    subjectId = subjectId,
                    newName = newName,
                    newCode = _subjectCode.value.trim().takeIf { it.isNotBlank() },
                    histogramLabel = _histogramLabel.value.trim().takeIf { it.isNotBlank() }
                )
                _subject.value = ""
                _subjectCode.value = ""
                _histogramLabel.value = ""
                onSuccess()
            }
        }
    }

    fun updateSubjectLabel(subjectId: Int, newLabel: String, currentName: String, currentCode: String?) {
        viewModelScope.launch {
            subjectRepository.updateSubject(
                subjectId = subjectId,
                newName = currentName,
                newCode = currentCode,
                histogramLabel = newLabel.trim().takeIf { it.isNotBlank() }
            )
        }
    }

    fun deleteSubject(subjectId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Cancel notifications first
            // Fetch schedules to cancel individual alarms
            val schedules = timetableRepository.getSchedulesForSubject(subjectId).first()
            schedules.forEach { schedule ->
                ClassNotificationScheduler.cancelScheduleNotification(context, schedule.id)
                TimetableAlarmScheduler.cancelClassAlarms(context, schedule.id)
            }
            
            // Also call bulk cancel for safety (legacy WorkManager tags)
            ClassNotificationScheduler.cancelNotifications(context, subjectId)
            
            subjectRepository.deleteSubject(subjectId)
            onSuccess()
        }
    }

    val subjectCount: Flow<Int> = subjectRepository.getSubjectCount()

    fun getSubjectById(subjectId: Int): Flow<SubjectEntity> {
        return subjectRepository.getSubjectById(subjectId)
    }

    fun deleteAllAttendanceForSubject(subjectId: Int) {
        viewModelScope.launch {
            attendanceRepository.deleteAllAttendanceForSubject(subjectId)
        }
    }

    fun getSubjectAttendanceCounts(subjectId: Int): Flow<SubjectAttendance> {
        val presentFlow = attendanceRepository.getCountBySubjectAndStatus(
            subjectId,
            AttendanceStatus.PRESENT
        )
        val absentFlow =
            attendanceRepository.getCountBySubjectAndStatus(subjectId, AttendanceStatus.ABSENT)

        return combine(presentFlow, absentFlow) { present, absent ->
            SubjectAttendance(
                presentCount = present,
                absentCount = absent,
                totalCount = present + absent
            )
        }
    }

    fun getTotalAttendanceCounts(): Flow<TotalAttendance> {
        val presentFlow = attendanceRepository.getTotalCountByStatus(AttendanceStatus.PRESENT)
        val absentFlow = attendanceRepository.getTotalCountByStatus(AttendanceStatus.ABSENT)

        return combine(presentFlow, absentFlow) { present, absent ->
            TotalAttendance(
                totalPresent = present,
                totalAbsent = absent,
                totalCount = present + absent
            )
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                _uiEvent.emit(SettingsUiEvent.RequestPermission(android.Manifest.permission.POST_NOTIFICATIONS))
            } else {
                val intent = createAppNotificationSettingsIntent(context)
                _uiEvent.emit(SettingsUiEvent.LaunchIntent(intent))
            }
        }
    }

    fun updateSubjectTarget(subjectId: Int, targetPercentage: Float) {
        viewModelScope.launch {
            subjectRepository.updateTargetPercentage(subjectId, targetPercentage)
        }
    }

    // Timetable Management
    fun getSchedulesForSubject(subjectId: Int): Flow<List<ClassSchedule>> {
        return timetableRepository.getSchedulesForSubject(subjectId)
            .map { schedules -> schedules.map { it.toClassSchedule() } }
    }

    fun saveSchedulesForSubject(subjectId: Int, schedules: List<ClassSchedule>) {
        viewModelScope.launch {
            // Get current schedules from DB to identify deletions
            val currentSchedules = timetableRepository.getSchedulesForSubject(subjectId).first().map { it.toClassSchedule() }
            
            // Cancel Legacy Alarms for ALL current schedules to prevent duplication
            currentSchedules.forEach { schedule ->
                ClassNotificationScheduler.cancelScheduleNotification(context, schedule.id)
            }

            // Identify schedules to delete (present in DB but not in new list)
            val newScheduleIds = schedules.map { it.id }.toSet()
            val schedulesToDelete = currentSchedules.filter { it.id != 0 && it.id !in newScheduleIds }

            // Delete removed schedules
            if (schedulesToDelete.isNotEmpty()) {
                timetableRepository.deleteClassesByIds(schedulesToDelete.map { it.id })
            }

            // Upsert new/updated schedules
            if (schedules.isNotEmpty()) {
                schedules.forEach { schedule ->
                    timetableRepository.insertClass(schedule.copy(subjectId = subjectId).toTimeTableEntity())
                }
                
                // Check Exact Alarm Permission
                if (!TimetableAlarmScheduler.canScheduleExactAlarms(context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        _uiEvent.emit(SettingsUiEvent.LaunchIntent(intent))
                    }
                }
                
                // Fetch the updated list to get generated IDs for new items
                val updatedSchedules = timetableRepository.getSchedulesForSubject(subjectId).first().map { it.toClassSchedule() }
                
                // Schedule exact alarms using AlarmManager
                updatedSchedules.forEach { schedule ->
                    TimetableAlarmScheduler.scheduleClassAlarms(
                        context = context,
                        scheduleId = schedule.id,
                        subjectId = subjectId,
                        subjectName = getSubjectById(subjectId).first().subject,
                        dayOfWeek = schedule.dayOfWeek,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        location = schedule.location
                    )
                }
            }
        }
    }

    fun deleteSchedulesForSubject(subjectId: Int) {
        viewModelScope.launch {
            val schedules = timetableRepository.getSchedulesForSubject(subjectId).first()
            TimetableAlarmScheduler.cancelAllAlarmsForSubject(
                context,
                schedules.map { it.id }
            )
            timetableRepository.deleteSchedulesForSubject(subjectId)
        }
    }
}

