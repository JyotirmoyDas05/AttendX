package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetExportableDataUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.UploadTemplateUseCase
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry

data class UploadState(
    val college: String = "",
    val department: String = "",
    val semester: String = "",
    val section: String = "",
    val academicYear: String = "2025-26",
    val availableSubjects: List<TemplateSubjectEntry> = emptyList(),
    val availableClasses: List<TemplateClassEntry> = emptyList(),
    val selectedSubjects: List<TemplateSubjectEntry> = emptyList(),
    val selectedClasses: List<TemplateClassEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class UploadEvent {
    data class EnteredCollege(val value: String): UploadEvent()
    data class EnteredDepartment(val value: String): UploadEvent()
    data class EnteredSemester(val value: String): UploadEvent()
    data class EnteredSection(val value: String): UploadEvent()
    data class EnteredYear(val value: String): UploadEvent()
    data class ToggleSubject(val subject: TemplateSubjectEntry): UploadEvent()
    data class ToggleClass(val classEntry: TemplateClassEntry): UploadEvent()
    object Upload: UploadEvent()
    object ErrorShown: UploadEvent()
}

@HiltViewModel
class UploadTemplateViewModel @Inject constructor(
    private val uploadTemplateUseCase: UploadTemplateUseCase,
    private val getExportableDataUseCase: GetExportableDataUseCase
) : ViewModel() {

    private val _state = mutableStateOf(UploadState())
    val state: State<UploadState> = _state

    init {
        loadExportableData()
    }

    private fun loadExportableData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val (subjects, classes) = getExportableDataUseCase()
                _state.value = _state.value.copy(
                    availableSubjects = subjects,
                    availableClasses = classes,
                    selectedSubjects = subjects, // Select all by default
                    selectedClasses = classes, // Select all by default
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load timetable data"
                )
            }
        }
    }

    fun onEvent(event: UploadEvent) {
        when(event) {
            is UploadEvent.EnteredCollege -> _state.value = _state.value.copy(college = event.value)
            is UploadEvent.EnteredDepartment -> _state.value = _state.value.copy(department = event.value)
            is UploadEvent.EnteredSemester -> _state.value = _state.value.copy(semester = event.value)
            is UploadEvent.EnteredSection -> _state.value = _state.value.copy(section = event.value)
            is UploadEvent.EnteredYear -> _state.value = _state.value.copy(academicYear = event.value)
            is UploadEvent.ToggleSubject -> {
                val currentSelected = _state.value.selectedSubjects.toMutableList()
                if (currentSelected.contains(event.subject)) {
                    currentSelected.remove(event.subject)
                } else {
                    currentSelected.add(event.subject)
                }
                _state.value = _state.value.copy(selectedSubjects = currentSelected)
            }
            is UploadEvent.ToggleClass -> {
                val currentSelected = _state.value.selectedClasses.toMutableList()
                if (currentSelected.contains(event.classEntry)) {
                    currentSelected.remove(event.classEntry)
                } else {
                    currentSelected.add(event.classEntry)
                }
                _state.value = _state.value.copy(selectedClasses = currentSelected)
            }
            is UploadEvent.ErrorShown -> _state.value = _state.value.copy(error = null)
            is UploadEvent.Upload -> upload()
        }
    }

    private fun upload() {
        if (_state.value.college.isBlank() || _state.value.department.isBlank()) {
            _state.value = _state.value.copy(error = "Please fill in all fields")
            return
        }

        val semInt = _state.value.semester.toIntOrNull()
        if (semInt == null) {
             _state.value = _state.value.copy(error = "Semester must be a number")
             return
        }

        if (_state.value.selectedClasses.isEmpty() && _state.value.selectedSubjects.isEmpty()) {
            _state.value = _state.value.copy(error = "Please select at least one item to export")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = uploadTemplateUseCase(
                college = _state.value.college,
                department = _state.value.department,
                semester = semInt,
                section = _state.value.section,
                academicYear = _state.value.academicYear,
                authorId = "user_id_placeholder", // TODO: Function to get or generate user ID
                authorName = "Anonymous Student", // TODO: Let user pick a name
                classes = _state.value.selectedClasses,
                subjects = _state.value.selectedSubjects
            )
            
            if (result.isSuccess) {
                 _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                 _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }
}

