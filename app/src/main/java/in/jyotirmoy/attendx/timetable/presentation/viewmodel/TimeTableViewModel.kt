package `in`.jyotirmoy.attendx.timetable.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class TimeTableViewModel @Inject constructor(
    private val useCases: TimeTableUseCases,
    private val subjectRepository: `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
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

    // View mode toggle (list or calendar)
    private val _isCalendarView = MutableStateFlow(false)
    val isCalendarView: StateFlow<Boolean> = _isCalendarView.asStateFlow()

    fun toggleViewMode() {
        _isCalendarView.value = !_isCalendarView.value
    }

    // Weekly schedule for calendar view - combines Mon-Sat schedules
    @Suppress("UNCHECKED_CAST")
    val weeklySchedule: StateFlow<Map<Int, List<TimeTableScheduleWithSubject>>> = 
        kotlinx.coroutines.flow.combine(
            listOf(
                useCases.getDailySchedule(1), // Mon
                useCases.getDailySchedule(2), // Tue
                useCases.getDailySchedule(3), // Wed
                useCases.getDailySchedule(4), // Thu
                useCases.getDailySchedule(5), // Fri
                useCases.getDailySchedule(6)  // Sat
            )
        ) { schedules ->
            mapOf(
                1 to (schedules[0] as List<TimeTableScheduleWithSubject>),
                2 to (schedules[1] as List<TimeTableScheduleWithSubject>),
                3 to (schedules[2] as List<TimeTableScheduleWithSubject>),
                4 to (schedules[3] as List<TimeTableScheduleWithSubject>),
                5 to (schedules[4] as List<TimeTableScheduleWithSubject>),
                6 to (schedules[5] as List<TimeTableScheduleWithSubject>)
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

    fun onDaySelected(day: Int) {
        _selectedDay.value = day
    }

    fun showAddSheet() {
        _editingSchedule.value = null
        _showAddEditSheet.value = true
    }

    fun showEditSheet(schedule: TimeTableScheduleWithSubject) {
        _editingSchedule.value = schedule
        _showAddEditSheet.value = true
    }

    fun dismissSheet() {
        _showAddEditSheet.value = false
        _editingSchedule.value = null
    }

    fun saveClass(schedule: TimeTableScheduleEntity) {
        viewModelScope.launch {
            useCases.addClassSlot(schedule)
            dismissSheet()
        }
    }

    // Selection state for multi-select delete
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow()

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow) { it.isNotEmpty() }
                .stateIn(viewModelScope, SharingStarted.Eagerly, false)
        }

    fun toggleSelection(id: Int) {
        _selectedIds.value = if (id in _selectedIds.value) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            useCases.deleteClass(id)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            useCases.deleteClass(_selectedIds.value.toList())
            clearSelection()
        }
    }
}
