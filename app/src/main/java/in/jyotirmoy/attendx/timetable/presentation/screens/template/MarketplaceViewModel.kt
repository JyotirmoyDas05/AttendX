package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository

data class MarketplaceState(
    val templates: List<CommunityTemplate> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MarketplaceEvent {
    data class SearchQueryChanged(val query: String): MarketplaceEvent()
    object Refresh: MarketplaceEvent()
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: TemplateRepository
) : ViewModel() {

    private val _state = mutableStateOf(MarketplaceState())
    val state: State<MarketplaceState> = _state
    
    private var searchJob: Job? = null

    init {
        searchTemplates("")
    }

    fun onEvent(event: MarketplaceEvent) {
        when(event) {
            is MarketplaceEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L) // Debounce
                    searchTemplates(event.query)
                }
            }
            is MarketplaceEvent.Refresh -> searchTemplates(_state.value.searchQuery)
        }
    }

    private fun searchTemplates(query: String) {
        _state.value = _state.value.copy(isLoading = true)
        repository.searchTemplates(query, null, null)
            .onEach { templates ->
                _state.value = _state.value.copy(
                    templates = templates,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}
