package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.preview

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import `in`.jyotirmoy.attendx.timetable.domain.usecase.ImportTemplateUseCase

data class TemplatePreviewState(
    val template: CommunityTemplate? = null,
    val selectedSubjects: List<TemplateSubjectEntry> = emptyList(),
    val selectedClasses: List<TemplateClassEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TemplatePreviewViewModel @Inject constructor(
    private val repository: TemplateRepository,
    private val importTemplateUseCase: ImportTemplateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(TemplatePreviewState())
    val state: State<TemplatePreviewState> = _state
    
    private val templateId: String? = savedStateHandle["templateId"]

    init {
        templateId?.let { loadTemplate(it) }
    }

    private fun loadTemplate(id: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            repository.getTemplateDetails(id)
                .onSuccess { template ->
                    _state.value = _state.value.copy(
                        template = template,
                        selectedSubjects = template.subjects,
                        selectedClasses = template.classes,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "Failed to load template",
                        isLoading = false
                    )
                }
        }
    }

    fun toggleSubject(subject: TemplateSubjectEntry) {
        val currentSubjects = _state.value.selectedSubjects.toMutableList()
        val currentClasses = _state.value.selectedClasses.toMutableList()
        val templateClasses = _state.value.template?.classes ?: emptyList()
        
        if (currentSubjects.contains(subject)) {
            // Deselecting subject -> Deselect associated classes
            currentSubjects.remove(subject)
            val classesToRemove = templateClasses.filter { it.subject == subject.name }
            currentClasses.removeAll(classesToRemove)
        } else {
            // Selecting subject -> Select associated classes
            currentSubjects.add(subject)
            val classesToAdd = templateClasses.filter { it.subject == subject.name }
            // Add only if not already present to avoid duplicates (though Set would be better, List is used)
            classesToAdd.forEach { classEntry ->
                if (!currentClasses.contains(classEntry)) {
                    currentClasses.add(classEntry)
                }
            }
        }
        _state.value = _state.value.copy(
            selectedSubjects = currentSubjects,
            selectedClasses = currentClasses
        )
    }

    fun toggleClass(classEntry: TemplateClassEntry) {
        val current = _state.value.selectedClasses.toMutableList()
        if (current.contains(classEntry)) {
            current.remove(classEntry)
        } else {
            current.add(classEntry)
        }
        _state.value = _state.value.copy(selectedClasses = current)
    }

    fun importTemplate() {
        val id = templateId ?: return
        
        if (_state.value.selectedSubjects.isEmpty() && _state.value.selectedClasses.isEmpty()) {
            _state.value = _state.value.copy(error = "Please select at least one item to import")
            return
        }

        _state.value = _state.value.copy(isImporting = true)
        viewModelScope.launch {
            importTemplateUseCase(
                templateId = id,
                subjectsToImport = _state.value.selectedSubjects,
                classesToImport = _state.value.selectedClasses
            )
                .onSuccess {
                    _state.value = _state.value.copy(
                        isImporting = false,
                        importSuccess = true
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        error = e.message ?: "Import failed"
                    )
                }
        }
    }
}
