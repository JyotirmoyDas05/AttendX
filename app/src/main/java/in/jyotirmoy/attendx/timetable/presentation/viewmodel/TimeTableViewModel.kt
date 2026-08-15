package `in`.jyotirmoy.attendx.timetable.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TimeTableUseCases
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private val subjectRepository: `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val subjects = subjectRepository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySchedule: StateFlow<List<TimeTableScheduleWithSubject>> = _selectedDay
        .flatMapLatest { day ->
            useCases.getDailySchedule(day)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
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

    // Starting day of 1-day or 3-day view (defaults to today)
    private val _threeDayStartDay = MutableStateFlow(LocalDate.now().dayOfWeek.value)
    val threeDayStartDay: StateFlow<Int> = _threeDayStartDay.asStateFlow()

    fun setThreeDayStartDay(day: Int) {
        _threeDayStartDay.value = day.coerceIn(1, 7)
        _selectedDay.value = day.coerceIn(1, 7)
    }

    fun shiftStartDay(delta: Int) {
        val current = _threeDayStartDay.value
        val newDay = ((current - 1 + delta) % 7 + 7) % 7 + 1
        _threeDayStartDay.value = newDay
        _selectedDay.value = newDay
    }

    fun snapToToday() {
        val today = LocalDate.now().dayOfWeek.value
        _selectedDay.value = today
        _threeDayStartDay.value = today
    }

    // Weekly schedule for calendar view - combines Mon-Sun schedules
    @Suppress("UNCHECKED_CAST")
    val weeklySchedule: StateFlow<Map<Int, List<TimeTableScheduleWithSubject>>> = 
        kotlinx.coroutines.flow.combine(
            listOf(
                useCases.getDailySchedule(1), // Mon
                useCases.getDailySchedule(2), // Tue
                useCases.getDailySchedule(3), // Wed
                useCases.getDailySchedule(4), // Thu
                useCases.getDailySchedule(5), // Fri
                useCases.getDailySchedule(6), // Sat
                useCases.getDailySchedule(7)  // Sun
            )
        ) { schedules ->
            mapOf(
                1 to (schedules[0] as List<TimeTableScheduleWithSubject>),
                2 to (schedules[1] as List<TimeTableScheduleWithSubject>),
                3 to (schedules[2] as List<TimeTableScheduleWithSubject>),
                4 to (schedules[3] as List<TimeTableScheduleWithSubject>),
                5 to (schedules[4] as List<TimeTableScheduleWithSubject>),
                6 to (schedules[5] as List<TimeTableScheduleWithSubject>),
                7 to (schedules[6] as List<TimeTableScheduleWithSubject>)
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap()
        )

    // Sheet state
    private val _showAddEditSheet = MutableStateFlow(false)
    val showAddEditSheet: StateFlow<Boolean> = _showAddEditSheet.asStateFlow()

    private val _editingSchedule = MutableStateFlow<TimeTableScheduleWithSubject?>(null)
    val editingSchedule: StateFlow<TimeTableScheduleWithSubject?> = _editingSchedule.asStateFlow()

    private val _initialSlotTime = MutableStateFlow<Pair<Long, Long>?>(null)
    val initialSlotTime: StateFlow<Pair<Long, Long>?> = _initialSlotTime.asStateFlow()

    fun onDaySelected(day: Int) {
        _selectedDay.value = day
        _threeDayStartDay.value = day
    }

    fun showAddSheet() {
        _editingSchedule.value = null
        _initialSlotTime.value = null
        _showAddEditSheet.value = true
    }

    fun showAddSheetForSlot(dayOfWeek: Int, startMinutes: Int) {
        _editingSchedule.value = null
        _selectedDay.value = dayOfWeek
        _threeDayStartDay.value = dayOfWeek
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
