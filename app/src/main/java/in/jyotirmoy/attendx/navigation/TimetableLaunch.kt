package `in`.jyotirmoy.attendx.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

/** Open the Timetable tab with a class's sheet for [date], or the add-class sheet when [addClass]. */
data class TimetableLaunchRequest(val scheduleId: Int, val date: LocalDate?, val addClass: Boolean)

/**
 * Hands a launch (from the Schedule widget) to the Timetable *tab*, so it opens inside
 * MainScreen with the floating nav bar rather than as a pushed screen without one.
 * MainScreen switches tabs when a request appears; the Timetable screen acts on it and clears it.
 */
object TimetableLaunch {
    val pending = MutableStateFlow<TimetableLaunchRequest?>(null)
}
