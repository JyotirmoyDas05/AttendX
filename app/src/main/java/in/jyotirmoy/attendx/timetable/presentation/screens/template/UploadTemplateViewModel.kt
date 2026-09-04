package `in`.jyotirmoy.attendx.timetable.presentation.screens.template

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar
import java.util.Locale
import `in`.jyotirmoy.attendx.core.data.service.AnonymousAuthService
import `in`.jyotirmoy.attendx.core.data.service.GoogleIdTokenResult
import `in`.jyotirmoy.attendx.core.data.service.GoogleSignInHelper
import `in`.jyotirmoy.attendx.core.data.service.LinkResult
import `in`.jyotirmoy.attendx.core.utils.UsernameFilter
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.data.repository.UsernameRepository
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetExportableDataUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.GetMyTemplatesUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.UpdateTemplateUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.UploadTemplateUseCase

private fun getDefaultAcademicYear(): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) // 0-indexed: 0 = Jan, 5 = June

    // Academic year begins around June/July: if month >= June, session is Y-(Y+1) (e.g. 2026-27), else (Y-1)-Y
    val startYear = if (month >= Calendar.JUNE) year else year - 1
    val endYearShort = (startYear + 1) % 100
    return String.format(Locale.US, "%d-%02d", startYear, endYearShort)
}

/** Modal surfaces owned by this screen. */
sealed interface UploadDialog {
    /** Explains what linking a Google account does before anything happens. */
    data object LinkExplainer : UploadDialog

    /**
     * The chosen Google account already belongs to another Firebase uid.
     * Continuing abandons the current anonymous identity.
     */
    data object LinkCollision : UploadDialog

    data object UsernameEditor : UploadDialog
}

/** Tabs of the share screen, in display order. */
enum class UploadTab { Details, Subjects, Timetable }

data class UploadState(
    val college: String = "",
    val department: String = "",
    val semester: String = "",
    val section: String = "",
    val academicYear: String = getDefaultAcademicYear(),
    val availableSubjects: List<TemplateSubjectEntry> = emptyList(),
    val availableClasses: List<TemplateClassEntry> = emptyList(),
    val selectedSubjects: List<TemplateSubjectEntry> = emptyList(),
    val selectedClasses: List<TemplateClassEntry> = emptyList(),
    val myTemplates: List<CommunityTemplate> = emptyList(),
    val editingId: String? = null,
    val isLinked: Boolean = false,
    val username: String? = null,
    val usernameInput: String = "",
    val usernameError: String? = null,
    val isAccountBusy: Boolean = false,
    val dialog: UploadDialog? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val info: String? = null,
    val error: String? = null,
    /**
     * Tab the pending error belongs to. Validation can reject a field on a tab
     * that is not showing, and a snackbar pointing at something invisible is not
     * an explanation.
     */
    val focusTab: UploadTab? = null
) {
    val isEditing: Boolean get() = editingId != null
    val authorName: String get() = username ?: CommunityTemplate.DEFAULT_AUTHOR_NAME
}

sealed class UploadEvent {
    data class EnteredCollege(val value: String): UploadEvent()
    data class EnteredDepartment(val value: String): UploadEvent()
    data class EnteredSemester(val value: String): UploadEvent()
    data class EnteredSection(val value: String): UploadEvent()
    data class EnteredYear(val value: String): UploadEvent()
    data class ToggleSubject(val subject: TemplateSubjectEntry): UploadEvent()
    data class ToggleClass(val classEntry: TemplateClassEntry): UploadEvent()
    data class StartEditing(val templateId: String): UploadEvent()
    object CancelEditing: UploadEvent()
    object ToggleAllSubjects: UploadEvent()
    object ToggleAllClasses: UploadEvent()
    object FocusHandled: UploadEvent()
    object ShowLinkExplainer: UploadEvent()
    data class ConfirmLink(val context: Context): UploadEvent()
    object ConfirmCollisionSignIn: UploadEvent()
    object ShowUsernameEditor: UploadEvent()
    data class UsernameChanged(val value: String): UploadEvent()
    object SaveUsername: UploadEvent()
    object DismissDialog: UploadEvent()
    object Upload: UploadEvent()
    object ErrorShown: UploadEvent()
    object InfoShown: UploadEvent()
}

@HiltViewModel
class UploadTemplateViewModel @Inject constructor(
    private val uploadTemplateUseCase: UploadTemplateUseCase,
    private val updateTemplateUseCase: UpdateTemplateUseCase,
    private val getMyTemplatesUseCase: GetMyTemplatesUseCase,
    private val getExportableDataUseCase: GetExportableDataUseCase,
    private val usernameRepository: UsernameRepository,
    private val googleSignInHelper: GoogleSignInHelper,
    private val anonymousAuthService: AnonymousAuthService
) : ViewModel() {

    private val _state = mutableStateOf(UploadState())
    val state: State<UploadState> = _state

    /** Held across the collision confirmation; a secret, so never in UI state. */
    private var pendingIdToken: String? = null

    init {
        loadExportableData()
        observeMyTemplates()
        refreshAccount()
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

    // Ownership lives server-side as authorId, so this needs no local bookkeeping
    // and repopulates by itself once an account is linked on a new device.
    private fun observeMyTemplates() {
        viewModelScope.launch {
            getMyTemplatesUseCase()
                .catch { /* offline or not yet signed in — leave the list empty */ }
                .collect { templates ->
                    _state.value = _state.value.copy(myTemplates = templates)
                }
        }
    }

    private fun refreshAccount() {
        _state.value = _state.value.copy(
            isLinked = anonymousAuthService.isLinked(),
            username = anonymousAuthService.displayName()
        )
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
            is UploadEvent.ToggleAllSubjects -> {
                val all = _state.value.availableSubjects
                _state.value = _state.value.copy(
                    selectedSubjects = if (_state.value.selectedSubjects.size == all.size) {
                        emptyList()
                    } else {
                        all
                    }
                )
            }
            is UploadEvent.ToggleAllClasses -> {
                val all = _state.value.availableClasses
                _state.value = _state.value.copy(
                    selectedClasses = if (_state.value.selectedClasses.size == all.size) {
                        emptyList()
                    } else {
                        all
                    }
                )
            }
            is UploadEvent.FocusHandled -> _state.value = _state.value.copy(focusTab = null)
            is UploadEvent.StartEditing -> startEditing(event.templateId)
            is UploadEvent.CancelEditing -> _state.value = _state.value.copy(editingId = null)
            is UploadEvent.ShowLinkExplainer ->
                _state.value = _state.value.copy(dialog = UploadDialog.LinkExplainer)
            is UploadEvent.ConfirmLink -> linkAccount(event.context)
            is UploadEvent.ConfirmCollisionSignIn -> signInDiscardingAnonymous()
            is UploadEvent.ShowUsernameEditor -> _state.value = _state.value.copy(
                dialog = UploadDialog.UsernameEditor,
                usernameInput = _state.value.username.orEmpty(),
                usernameError = null
            )
            is UploadEvent.UsernameChanged ->
                _state.value = _state.value.copy(usernameInput = event.value, usernameError = null)
            is UploadEvent.SaveUsername -> saveUsername()
            is UploadEvent.DismissDialog -> _state.value = _state.value.copy(dialog = null)
            is UploadEvent.ErrorShown -> _state.value = _state.value.copy(error = null)
            is UploadEvent.InfoShown -> _state.value = _state.value.copy(info = null)
            is UploadEvent.Upload -> upload()
        }
    }

    /**
     * Metadata comes from the published document; subjects and classes keep
     * coming from the *local* timetable, because "my schedule changed, push the
     * new version" is the entire point of editing.
     */
    private fun startEditing(templateId: String) {
        val template = _state.value.myTemplates.firstOrNull { it.id == templateId } ?: return
        _state.value = _state.value.copy(
            editingId = templateId,
            college = template.college,
            department = template.department,
            semester = template.semester.toString(),
            section = template.section,
            academicYear = template.academicYear
        )
    }

    private fun linkAccount(context: Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isAccountBusy = true, dialog = null)
            when (val token = googleSignInHelper.getIdToken(context)) {
                is GoogleIdTokenResult.Cancelled ->
                    _state.value = _state.value.copy(isAccountBusy = false)

                is GoogleIdTokenResult.NotConfigured -> _state.value = _state.value.copy(
                    isAccountBusy = false,
                    error = "Google sign-in isn't set up for this build yet."
                )

                is GoogleIdTokenResult.Failed -> _state.value = _state.value.copy(
                    isAccountBusy = false,
                    error = token.error.message ?: "Couldn't reach Google sign-in."
                )

                is GoogleIdTokenResult.Success -> {
                    pendingIdToken = token.idToken
                    applyLinkResult(anonymousAuthService.linkWithGoogle(token.idToken))
                }
            }
        }
    }

    private fun applyLinkResult(result: LinkResult) {
        when (result) {
            is LinkResult.Linked -> {
                pendingIdToken = null
                refreshAccount()
                _state.value = _state.value.copy(
                    isAccountBusy = false,
                    info = "Account linked. You can now edit your templates from any device."
                )
            }
            is LinkResult.AlreadyLinkedElsewhere -> _state.value = _state.value.copy(
                isAccountBusy = false,
                dialog = UploadDialog.LinkCollision
            )
            is LinkResult.Failed -> {
                pendingIdToken = null
                _state.value = _state.value.copy(
                    isAccountBusy = false,
                    error = result.error.message ?: "Couldn't link that account."
                )
            }
        }
    }

    private fun signInDiscardingAnonymous() {
        val token = pendingIdToken ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isAccountBusy = true, dialog = null)
            applyLinkResult(anonymousAuthService.signInWithGoogleDiscardingAnonymous(token))
        }
    }

    private fun saveUsername() {
        val candidate = _state.value.usernameInput.trim()
        when (val verdict = UsernameFilter.validate(candidate)) {
            is UsernameFilter.Result.Invalid -> {
                _state.value = _state.value.copy(usernameError = verdict.reason)
                return
            }
            is UsernameFilter.Result.Valid -> Unit
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isAccountBusy = true, usernameError = null)
            when (val result = usernameRepository.claim(candidate, _state.value.username)) {
                is UsernameRepository.ClaimResult.Claimed -> {
                    refreshAccount()
                    _state.value = _state.value.copy(
                        isAccountBusy = false,
                        dialog = null,
                        username = result.name,
                        info = "You'll appear as ${result.name} on templates you share."
                    )
                }
                is UsernameRepository.ClaimResult.Taken -> _state.value = _state.value.copy(
                    isAccountBusy = false,
                    usernameError = "That name is already taken."
                )
                is UsernameRepository.ClaimResult.Failed -> _state.value = _state.value.copy(
                    isAccountBusy = false,
                    usernameError = result.error.message ?: "Couldn't save that name."
                )
            }
        }
    }

    private fun upload() {
        // Each rejection carries the tab that owns the offending field so the
        // screen can bring it into view alongside the message.
        if (_state.value.college.isBlank() || _state.value.department.isBlank()) {
            _state.value = _state.value.copy(
                error = "College and department are required",
                focusTab = UploadTab.Details
            )
            return
        }

        val semInt = _state.value.semester.toIntOrNull()
        if (semInt == null) {
            _state.value = _state.value.copy(
                error = "Semester must be a number",
                focusTab = UploadTab.Details
            )
            return
        }
        if (semInt !in 1..12) {
            _state.value = _state.value.copy(
                error = "Semester must be between 1 and 12",
                focusTab = UploadTab.Details
            )
            return
        }

        if (_state.value.selectedClasses.isEmpty() && _state.value.selectedSubjects.isEmpty()) {
            _state.value = _state.value.copy(
                error = "Select at least one subject or class to share",
                focusTab = UploadTab.Subjects
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // No offline uid fallback: a synthetic authorId can never match
            // request.auth.uid, so it would mint a permanently unowned template.
            val editingId = _state.value.editingId
            val result = if (editingId != null) {
                updateTemplateUseCase(
                    templateId = editingId,
                    college = _state.value.college,
                    department = _state.value.department,
                    semester = semInt,
                    section = _state.value.section,
                    academicYear = _state.value.academicYear,
                    authorName = _state.value.authorName,
                    classes = _state.value.selectedClasses,
                    subjects = _state.value.selectedSubjects
                ).map { }
            } else {
                uploadTemplateUseCase(
                    college = _state.value.college,
                    department = _state.value.department,
                    semester = semInt,
                    section = _state.value.section,
                    academicYear = _state.value.academicYear,
                    authorName = _state.value.authorName,
                    classes = _state.value.selectedClasses,
                    subjects = _state.value.selectedSubjects
                ).map { }
            }

            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, isSuccess = true)
            } else {
                _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Upload failed"
                )
            }
        }
    }
}
