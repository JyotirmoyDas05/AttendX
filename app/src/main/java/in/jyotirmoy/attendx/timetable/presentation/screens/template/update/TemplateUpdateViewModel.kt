package `in`.jyotirmoy.attendx.timetable.presentation.screens.template.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.domain.repository.SettingsRepository
import `in`.jyotirmoy.attendx.timetable.domain.TemplateUpdateNotifier
import `in`.jyotirmoy.attendx.timetable.domain.usecase.CheckTemplateUpdateUseCase
import `in`.jyotirmoy.attendx.timetable.domain.usecase.TemplateUpdateInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateUpdateViewModel @Inject constructor(
    private val checkTemplateUpdateUseCase: CheckTemplateUpdateUseCase,
    private val settingsRepository: SettingsRepository,
    private val notifier: TemplateUpdateNotifier
) : ViewModel() {

    val pending: StateFlow<TemplateUpdateInfo?> = notifier.pending
    val showRequests: SharedFlow<Unit> = notifier.showRequests

    /** Badge tap: ask the dialog host to show the pending update again. */
    fun requestShow() = notifier.requestShow()

    fun check() {
        viewModelScope.launch {
            runCatching { checkTemplateUpdateUseCase() }
                .onSuccess { if (it != null) notifier.set(it) }
        }
    }

    /** "Not now" — stay quiet for this version, speak up on the next one. */
    fun dismissVersion() {
        val version = notifier.pending.value?.version ?: return
        viewModelScope.launch {
            settingsRepository.setInt(SettingsKeys.IMPORTED_TEMPLATE_DISMISSED_VERSION, version)
            notifier.clear()
        }
    }

    /** "Stop checking" — forget the link to the origin template for good. */
    fun stopChecking() {
        viewModelScope.launch {
            settingsRepository.setString(SettingsKeys.IMPORTED_TEMPLATE_ID, "")
            notifier.clear()
        }
    }

    /**
     * The user is off to review the changes in the template preview, where the
     * existing per-item checkboxes let them pick exactly what to take. Importing
     * there rewrites the stored version, so just drop the pending notice.
     */
    fun onReviewing() = notifier.clear()
}
