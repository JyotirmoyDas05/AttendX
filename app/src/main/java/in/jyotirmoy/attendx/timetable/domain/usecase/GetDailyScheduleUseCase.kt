package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Every class scheduled on [dayOfWeek], including ones that only run on odd or
 * even weeks.
 *
 * Week-pattern filtering deliberately does not happen here any more. It used to,
 * against `LocalDate.now()`, which was correct only while the timetable could
 * show nothing but the current week. The calendar views now page through weeks,
 * and a class's visibility depends on the week being looked at — so the caller
 * applies `WeekPattern.matches` for the date it is drawing. That also keeps this
 * flow shareable across every page instead of needing one flow per week.
 */
class GetDailyScheduleUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    operator fun invoke(dayOfWeek: Int): Flow<List<TimeTableScheduleWithSubject>> =
        repository.getClassesForDayWithSubject(dayOfWeek)
}
