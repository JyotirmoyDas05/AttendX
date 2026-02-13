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
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import `in`.jyotirmoy.attendx.timetable.domain.usecase.ImportTemplateUseCase

data class TemplatePreviewState(
    val template: CommunityTemplate? = null,
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

    fun importTemplate() {
        val id = templateId ?: return
        _state.value = _state.value.copy(isImporting = true)
        viewModelScope.launch {
            importTemplateUseCase(id)
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
