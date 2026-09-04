package `in`.jyotirmoy.attendx.timetable.domain

import `in`.jyotirmoy.attendx.timetable.domain.usecase.TemplateUpdateInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shares the pending template update between the dialog (hosted in the nav graph)
 * and the badge (in the timetable header).
 *
 * A singleton rather than a shared ViewModel because those two live under
 * different ViewModelStoreOwners — the badge sits inside a NavBackStackEntry and
 * the dialog does not, so hiltViewModel() would hand them separate instances.
 */
@Singleton
class TemplateUpdateNotifier @Inject constructor() {

    private val _pending = MutableStateFlow<TemplateUpdateInfo?>(null)
    val pending: StateFlow<TemplateUpdateInfo?> = _pending.asStateFlow()

    /** Badge taps: ask whoever hosts the dialog to show it again. */
    private val _showRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val showRequests: SharedFlow<Unit> = _showRequests.asSharedFlow()

    fun set(info: TemplateUpdateInfo?) { _pending.value = info }

    fun clear() { _pending.value = null }

    fun requestShow() { _showRequests.tryEmit(Unit) }
}
