package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import `in`.jyotirmoy.attendx.timetable.domain.usecase.UploadTemplateUseCase

data class UploadState(
    val college: String = "",
    val department: String = "",
    val semester: String = "",
    val section: String = "",
    val academicYear: String = "2025-26",
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
    object Upload: UploadEvent()
    object ErrorShown: UploadEvent()
}

@HiltViewModel
class UploadTemplateViewModel @Inject constructor(
    private val uploadTemplateUseCase: UploadTemplateUseCase
) : ViewModel() {

    private val _state = mutableStateOf(UploadState())
    val state: State<UploadState> = _state

    fun onEvent(event: UploadEvent) {
        when(event) {
            is UploadEvent.EnteredCollege -> _state.value = _state.value.copy(college = event.value)
            is UploadEvent.EnteredDepartment -> _state.value = _state.value.copy(department = event.value)
            is UploadEvent.EnteredSemester -> _state.value = _state.value.copy(semester = event.value)
            is UploadEvent.EnteredSection -> _state.value = _state.value.copy(section = event.value)
            is UploadEvent.EnteredYear -> _state.value = _state.value.copy(academicYear = event.value)
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

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = uploadTemplateUseCase(
                college = _state.value.college,
                department = _state.value.department,
                semester = semInt,
                section = _state.value.section,
                academicYear = _state.value.academicYear,
                authorId = "user_id_placeholder", // TODO: Function to get or generate user ID
                authorName = "Anonymous Student" // TODO: Let user pick a name
            )
            
            if (result.isSuccess) {
                 _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                 _state.value = _state.value.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }
}
