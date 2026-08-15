package `in`.jyotirmoy.attendx.peer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import `in`.jyotirmoy.attendx.core.domain.repository.AttendanceRepository
import `in`.jyotirmoy.attendx.peer.data.local.PeerComparisonCacheEntity
import `in`.jyotirmoy.attendx.peer.data.model.PeerGroup
import `in`.jyotirmoy.attendx.peer.domain.model.PeerComparisonData
import `in`.jyotirmoy.attendx.peer.domain.model.PeerComparisonResult
import `in`.jyotirmoy.attendx.peer.domain.usecase.GetPeerComparisonUseCase
import `in`.jyotirmoy.attendx.peer.domain.usecase.JoinOrCreatePeerGroupUseCase
import `in`.jyotirmoy.attendx.peer.domain.usecase.SearchPeerGroupsUseCase
import `in`.jyotirmoy.attendx.peer.domain.usecase.SubmitPeerDataUseCase
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class PeerSettingsTuple(
    val optedIn: Boolean,
    val groupId: String,
    val groupDisplayName: String,
    val consentShown: Boolean
)

data class PeerComparisonState(
    val result: PeerComparisonResult = PeerComparisonResult.Loading,
    val groupId: String = "",
    val groupDisplayName: String = "",
    val isOptedIn: Boolean = false,
    val consentShown: Boolean = false,
    val userAttendancePercentage: Float = 0f,
    // Search sub-state
    val searchQuery: String = "",
    val searchResults: List<PeerGroup> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    // Create group dialog sub-state
    val showCreateGroupDialog: Boolean = false,
    val createCollege: String = "",
    val createDepartment: String = "",
    val createSemester: Int = 0,
    val isCreatingGroup: Boolean = false
)

@HiltViewModel
class PeerComparisonViewModel @Inject constructor(
    private val getPeerComparisonUseCase: GetPeerComparisonUseCase,
    private val submitPeerDataUseCase: SubmitPeerDataUseCase,
    private val searchPeerGroupsUseCase: SearchPeerGroupsUseCase,
    private val joinOrCreatePeerGroupUseCase: JoinOrCreatePeerGroupUseCase,
    private val settingsRepository: SettingsRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PeerComparisonState())
    val state: StateFlow<PeerComparisonState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeSettingsAndAttendance()
        // Kick off initial empty search so groups appear immediately
        onSearchQueryChanged("")
    }

    private fun observeSettingsAndAttendance() {
        viewModelScope.launch {
            val settingsFlow = combine(
                settingsRepository.getBoolean(SettingsKeys.PEER_COMPARISON_ENABLED),
                settingsRepository.getString(SettingsKeys.PEER_COMPARISON_GROUP_ID),
                settingsRepository.getString(SettingsKeys.PEER_COMPARISON_GROUP_DISPLAY_NAME),
                settingsRepository.getBoolean(SettingsKeys.PEER_COMPARISON_CONSENT_SHOWN)
            ) { optedIn, groupId, groupDisplayName, consentShown ->
                PeerSettingsTuple(optedIn, groupId, groupDisplayName, consentShown)
            }

            val attendanceFlow = combine(
                attendanceRepository.getTotalCountByStatus(AttendanceStatus.PRESENT),
                attendanceRepository.getTotalCountByStatus(AttendanceStatus.ABSENT)
            ) { present, absent ->
                val total = present + absent
                if (total > 0) (present.toFloat() / total.toFloat()) * 100f else 0f
            }

            combine(settingsFlow, attendanceFlow) { settings, percentage ->
                val wasOptedIn = _state.value.isOptedIn
                val oldGroupId = _state.value.groupId
                val oldResult = _state.value.result

                _state.update {
                    it.copy(
                        isOptedIn = settings.optedIn,
                        groupId = settings.groupId,
                        groupDisplayName = settings.groupDisplayName,
                        consentShown = settings.consentShown,
                        userAttendancePercentage = percentage,
                        result = if (!settings.optedIn) PeerComparisonResult.OptedOut else it.result
                    )
                }

                if (settings.optedIn && settings.groupId.isNotBlank()) {
                    if (!wasOptedIn || oldGroupId != settings.groupId || oldResult is PeerComparisonResult.Loading) {
                        loadPeerDataInternal(settings.groupId, percentage)
                    }
                } else if (!settings.optedIn) {
                    _state.update { it.copy(result = PeerComparisonResult.OptedOut) }
                }
            }.collect {}
        }
    }

    fun setUserAttendance(percentage: Float) {
        _state.update { it.copy(userAttendancePercentage = percentage) }
        val s = _state.value
        if (s.isOptedIn && s.groupId.isNotBlank()) {
            loadPeerDataInternal(s.groupId, percentage)
        }
    }

    // --- Search ---

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query, isSearching = true) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L) // Debounce — same pattern as MarketplaceViewModel
            searchPeerGroupsUseCase(query)
                .catch { _state.update { it.copy(isSearching = false, searchError = it.searchError) } }
                .onEach { results ->
                    _state.update { it.copy(searchResults = results, isSearching = false, searchError = null) }
                }
                .launchIn(viewModelScope)
        }
    }

    // --- Join existing ---

    fun onGroupSelected(group: PeerGroup) {
        viewModelScope.launch {
            val percentage = _state.value.userAttendancePercentage
            settingsRepository.setString(SettingsKeys.PEER_COMPARISON_GROUP_ID, group.id)
            settingsRepository.setString(SettingsKeys.PEER_COMPARISON_GROUP_DISPLAY_NAME, group.displayName)
            settingsRepository.setBoolean(SettingsKeys.PEER_COMPARISON_ENABLED, true)
            settingsRepository.setBoolean(SettingsKeys.PEER_COMPARISON_CONSENT_SHOWN, true)
            _state.update {
                it.copy(
                    groupId = group.id,
                    groupDisplayName = group.displayName,
                    isOptedIn = true,
                    consentShown = true
                )
            }
            submitPeerDataUseCase(group.id, percentage)
            loadPeerDataInternal(group.id, percentage)
        }
    }

    // --- Create new group dialog ---

    fun onShowCreateGroupDialog() {
        _state.update { it.copy(showCreateGroupDialog = true) }
    }

    fun onDismissCreateGroupDialog() {
        _state.update { it.copy(showCreateGroupDialog = false) }
    }

    fun onCreateCollegeChanged(value: String) {
        _state.update { it.copy(createCollege = value) }
    }

    fun onCreateDepartmentChanged(value: String) {
        _state.update { it.copy(createDepartment = value) }
    }

    fun onCreateSemesterChanged(value: Int) {
        _state.update { it.copy(createSemester = value) }
    }

    fun onCreateGroupConfirmed() {
        val s = _state.value
        if (s.createCollege.isBlank() || s.createDepartment.isBlank() || s.createSemester == 0) return
        viewModelScope.launch {
            _state.update { it.copy(isCreatingGroup = true) }
            val result = joinOrCreatePeerGroupUseCase.createNew(
                college = s.createCollege,
                department = s.createDepartment,
                semester = s.createSemester
            )
            result.onSuccess { group -> onGroupSelected(group) }
            result.onFailure { e ->
                _state.update { it.copy(isCreatingGroup = false, searchError = e.message) }
            }
            _state.update { it.copy(isCreatingGroup = false, showCreateGroupDialog = false) }
        }
    }

    // --- Opt-out ---

    fun disablePeerComparison() {
        viewModelScope.launch {
            settingsRepository.setBoolean(SettingsKeys.PEER_COMPARISON_ENABLED, false)
            settingsRepository.setString(SettingsKeys.PEER_COMPARISON_GROUP_ID, "")
            settingsRepository.setString(SettingsKeys.PEER_COMPARISON_GROUP_DISPLAY_NAME, "")
            _state.update {
                it.copy(
                    isOptedIn = false,
                    groupId = "",
                    groupDisplayName = "",
                    result = PeerComparisonResult.OptedOut
                )
            }
        }
    }

    // --- Load data ---

    fun loadPeerData() {
        val s = _state.value
        if (!s.isOptedIn || s.groupId.isBlank()) {
            _state.update { it.copy(result = PeerComparisonResult.OptedOut) }
            return
        }
        loadPeerDataInternal(s.groupId, s.userAttendancePercentage)
    }

    private fun loadPeerDataInternal(groupId: String, percentage: Float) {
        if (groupId.isBlank()) return
        viewModelScope.launch {
            if (_state.value.result is PeerComparisonResult.OptedOut || _state.value.result is PeerComparisonResult.Loading) {
                _state.update { it.copy(result = PeerComparisonResult.Loading) }
            }
            val data = getPeerComparisonUseCase(groupId, percentage)
            _state.update {
                it.copy(
                    result = if (data != null) PeerComparisonResult.Success(data)
                    else PeerComparisonResult.InsufficientPeers
                )
            }
        }
    }

    fun observeCache(): Flow<PeerComparisonData?> {
        val s = _state.value
        if (s.groupId.isBlank()) return flowOf(null)
        return getPeerComparisonUseCase.observeCache(s.groupId)
            .map { cached: PeerComparisonCacheEntity? ->
                cached?.let {
                    PeerComparisonData(
                        averageAttendance = it.averageAttendance,
                        stdDev = it.stdDev,
                        sampleSize = it.sampleSize,
                        userPercentile = it.userPercentile,
                        isStale = true,
                        fetchedAt = it.fetchedAt
                    )
                }
            }
    }
}
