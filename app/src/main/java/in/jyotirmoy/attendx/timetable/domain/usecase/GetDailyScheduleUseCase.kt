package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoField
import javax.inject.Inject

class GetDailyScheduleUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    operator fun invoke(dayOfWeek: Int): Flow<List<TimeTableScheduleWithSubject>> {
        return repository.getClassesForDayWithSubject(dayOfWeek).map { schedules ->
            val currentWeekType = getCurrentWeekType()
            schedules.filter { item ->
                when (item.schedule.weekPattern) {
                    "odd" -> currentWeekType == "odd"
                    "even" -> currentWeekType == "even"
                    else -> true // "all" shows always
                }
            }
        }
    }

    /**
     * Determines if current week is "odd" or "even" based on ISO week number.
     */
    private fun getCurrentWeekType(): String {
        val weekOfYear = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        return if (weekOfYear % 2 == 1) "odd" else "even"
    }
}
