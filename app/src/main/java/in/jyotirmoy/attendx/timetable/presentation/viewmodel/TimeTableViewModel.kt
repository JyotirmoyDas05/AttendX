package `in`.jyotirmoy.attendx.timetable.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.model.SubjectAttendance
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.classesOn
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TimeTableUseCases
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class TimetableViewMode {
    SCHEDULE,
    DAY,
    THREE_DAY,
    WEEK
}

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    private val useCases: TimeTableUseCases,
    private val subjectRepository: SubjectRepository,
    private val settingsRepository: SettingsRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val subjects = subjectRepository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * The date the calendar views are anchored to. This replaced a pair of
     * day-of-week indices (1..7) that wrapped at Sunday, which meant swiping
     * forward past Sunday moved the displayed date a week *backwards*, and made
     * a week view impossible because every week was the same seven indices.
     */
    private val _anchorDate = MutableStateFlow(LocalDate.now())
    val anchorDate: StateFlow<LocalDate> = _anchorDate.asStateFlow()

    /**
     * Today, kept current across a midnight rollover.
     *
     * The next-up card already refreshed itself on its own ticker, so at 00:00
     * it moved on to the new day while the day strip and the agenda under it
     * stayed on yesterday. This is the missing half of that.
     *
     * One coroutine that sleeps to the next midnight, so the rollover lands on
     * the second rather than whenever a poll happens to fall. The minute cap is
     * for the other case: a delay's clock stops while the device is in deep
     * sleep, so a sleep set at 23:00 can run hours late. Capping it means the
     * date is at worst a minute stale after a wake, instead of a day.
     *
     * A no-op wakeup a minute is a sixth of what the next-class ticker on this
     * same screen already costs, and it does not query anything.
     */
    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    /** Day of week currently focused, for callers that only care about that. */
    val selectedDay: StateFlow<Int> = _anchorDate
        .map { it.dayOfWeek.value }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LocalDate.now().dayOfWeek.value
        )

    val nextClass = useCases.getNextClass()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentClass = useCases.getCurrentClass()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // View mode (Schedule List, 1-Day Grid, 3-Day Grid, or Full Week Grid)
    private val _viewMode = MutableStateFlow(TimetableViewMode.DAY)
    val viewMode: StateFlow<TimetableViewMode> = _viewMode.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val untilMidnight = ChronoUnit.MILLIS.between(
                    now, now.toLocalDate().plusDays(1).atStartOfDay()
                )
                // +1s so we land past the boundary rather than racing it.
                delay((untilMidnight + 1_000L).coerceIn(1_000L, 60_000L))

                val fresh = LocalDate.now()
                val previous = _today.value
                if (fresh != previous) {
                    _today.value = fresh
                    // Follow the rollover only for someone who was looking at
                    // today; anyone who paged somewhere else meant to be there.
                    if (_anchorDate.value == previous) _anchorDate.value = fresh
                }
            }
        }

        viewModelScope.launch {
            settingsRepository.getString(SettingsKeys.TIMETABLE_VIEW_MODE).collect { saved ->
                if (saved.isNotBlank()) {
                    try {
                        _viewMode.value = TimetableViewMode.valueOf(saved)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Backwards compatibility for boolean calendar view
    val isCalendarView: StateFlow<Boolean> = _viewMode
        .let { flow ->
            kotlinx.coroutines.flow.flow {
                flow.collect { emit(it != TimetableViewMode.SCHEDULE) }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        }

    fun setViewMode(mode: TimetableViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            settingsRepository.setString(SettingsKeys.TIMETABLE_VIEW_MODE, mode.name)
        }
    }

    fun toggleViewMode() {
        val next = when (_viewMode.value) {
            TimetableViewMode.SCHEDULE -> TimetableViewMode.DAY
            TimetableViewMode.DAY -> TimetableViewMode.THREE_DAY
            TimetableViewMode.THREE_DAY -> TimetableViewMode.WEEK
            TimetableViewMode.WEEK -> TimetableViewMode.SCHEDULE
        }
        setViewMode(next)
    }

    /** Moves the anchor, e.g. after a swipe settles or a day is tapped. */
    fun setAnchorDate(date: LocalDate) {
        _anchorDate.value = date
    }

    fun snapToToday() {
        _anchorDate.value = LocalDate.now()
    }

    /**
     * Every class of the recurring week, keyed by day of week and *not* filtered
     * by odd/even week. The timetable repeats, so one map serves every page the
     * user can swipe to; only the odd/even decision varies, and that is applied
     * per displayed date via [WeekPattern]. Filtering here instead would need a
     * separate flow per week.
     */
    @Suppress("UNCHECKED_CAST")
    val weeklyClasses: StateFlow<Map<Int, List<TimeTableScheduleWithSubject>>> =
        kotlinx.coroutines.flow.combine(
            (1..7).map { day -> useCases.getDailySchedule(day) }
        ) { schedules ->
            (1..7).associateWith { day ->
                schedules[day - 1] as List<TimeTableScheduleWithSubject>
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    /** Classes on the anchored date, with odd/even weeks resolved for it. */
    val dailySchedule: StateFlow<List<TimeTableScheduleWithSubject>> =
        combine(weeklyClasses, _anchorDate) { weekly, date ->
            classesOn(weekly, date)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Add / Edit Sheet state
    private val _showAddEditSheet = MutableStateFlow(false)
    val showAddEditSheet: StateFlow<Boolean> = _showAddEditSheet.asStateFlow()

    private val _editingSchedule = MutableStateFlow<TimeTableScheduleWithSubject?>(null)
    val editingSchedule: StateFlow<TimeTableScheduleWithSubject?> = _editingSchedule.asStateFlow()

    private val _initialSlotTime = MutableStateFlow<Pair<Long, Long>?>(null)
    val initialSlotTime: StateFlow<Pair<Long, Long>?> = _initialSlotTime.asStateFlow()

    // View Class Details Sheet state
    /**
     * The class being inspected, together with the date it was opened from.
     *
     * The date matters: attendance is stored per calendar date, and the sheet
     * used to work it out itself as "this class's weekday in the current week".
     * That was invisible while only the current week could ever be shown, but
     * the views now page through weeks, so marking a class present would have
     * written the mark to the wrong day.
     */
    data class ViewedClass(
        val schedule: TimeTableScheduleWithSubject,
        val date: LocalDate
    )

    private val _viewingSchedule = MutableStateFlow<ViewedClass?>(null)
    val viewingSchedule: StateFlow<ViewedClass?> = _viewingSchedule.asStateFlow()

    fun showViewSheet(schedule: TimeTableScheduleWithSubject, date: LocalDate) {
        _viewingSchedule.value = ViewedClass(schedule, date)
    }

    /**
     * Opens [scheduleId]'s sheet for [date] with the views anchored there, as if the
     * class had been tapped on that day. Used by the home-screen Schedule widget.
     * Gives up quietly if the class was deleted after the widget last drew it.
     */
    fun openClassOn(scheduleId: Int, date: LocalDate) {
        viewModelScope.launch {
            val match = kotlinx.coroutines.withTimeoutOrNull(3_000) {
                weeklyClasses.first { weekly -> weekly.values.any { list -> list.any { it.schedule.id == scheduleId } } }
            }?.values?.flatten()?.firstOrNull { it.schedule.id == scheduleId } ?: return@launch
            _anchorDate.value = date
            _viewingSchedule.value = ViewedClass(match, date)
        }
    }

    fun hideViewSheet() {
        _viewingSchedule.value = null
    }

    fun getAttendanceForSubject(subjectId: Int): Flow<List<AttendanceEntity>> {
        return attendanceRepository.getAttendanceForSubject(subjectId)
    }

    fun getSubjectAttendanceCounts(subjectId: Int): Flow<SubjectAttendance> {
        val presentFlow = attendanceRepository.getCountBySubjectAndStatus(subjectId, AttendanceStatus.PRESENT)
        val absentFlow = attendanceRepository.getCountBySubjectAndStatus(subjectId, AttendanceStatus.ABSENT)
        return combine(presentFlow, absentFlow) { p, a ->
            SubjectAttendance(p, a, p + a)
        }
    }

    fun markAttendance(subjectId: Int, date: String, status: AttendanceStatus) {
        viewModelScope.launch {
            if (status == AttendanceStatus.UNMARKED) {
                attendanceRepository.deleteAttendance(subjectId, date)
            } else {
                attendanceRepository.insertAttendance(
                    AttendanceEntity(
                        subjectId = subjectId,
                        date = date,
                        status = status
                    )
                )
            }
        }
    }

    fun onDaySelected(date: LocalDate) {
        _anchorDate.value = date
    }

    fun showAddSheet() {
        _editingSchedule.value = null
        _initialSlotTime.value = null
        _showAddEditSheet.value = true
    }

    fun showAddSheetForSlot(date: LocalDate, startMinutes: Int) {
        _editingSchedule.value = null
        _anchorDate.value = date
        _initialSlotTime.value = Pair(startMinutes.toLong(), (startMinutes + 60).toLong())
        _showAddEditSheet.value = true
    }

    fun showEditSheet(schedule: TimeTableScheduleWithSubject) {
        _editingSchedule.value = schedule
        _showAddEditSheet.value = true
    }

    fun hideAddEditSheet() {
        _showAddEditSheet.value = false
        _editingSchedule.value = null
        _initialSlotTime.value = null
    }

    fun saveSchedule(schedule: TimeTableScheduleEntity) {
        viewModelScope.launch {
            useCases.addClassSlot(schedule)
            hideAddEditSheet()
        }
    }

    fun deleteSchedule(scheduleId: Int) {
        viewModelScope.launch {
            useCases.deleteClass(scheduleId)
        }
    }

    // Selection mode for batch deletion
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedClassIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedClassIds: StateFlow<Set<Int>> = _selectedClassIds.asStateFlow()

    private val _showDeleteConfirmDialog = MutableStateFlow(false)
    val showDeleteConfirmDialog: StateFlow<Boolean> = _showDeleteConfirmDialog.asStateFlow()

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedClassIds.value = emptySet()
        }
    }

    fun toggleClassSelection(classId: Int) {
        _selectedClassIds.value = if (_selectedClassIds.value.contains(classId)) {
            _selectedClassIds.value - classId
        } else {
            _selectedClassIds.value + classId
        }
        if (_selectedClassIds.value.isEmpty()) {
            _isSelectionMode.value = false
        }
    }

    fun selectAllClasses() {
        val currentClasses = dailySchedule.value
        _selectedClassIds.value = currentClasses.map { it.schedule.id }.toSet()
    }

    fun deselectAllClasses() {
        _selectedClassIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun showDeleteConfirmation() {
        if (_selectedClassIds.value.isNotEmpty()) {
            _showDeleteConfirmDialog.value = true
        }
    }

    fun hideDeleteConfirmation() {
        _showDeleteConfirmDialog.value = false
    }

    fun deleteSelectedClasses() {
        viewModelScope.launch {
            useCases.deleteClass(_selectedClassIds.value.toList())
            _selectedClassIds.value = emptySet()
            _isSelectionMode.value = false
            _showDeleteConfirmDialog.value = false
        }
    }
}
