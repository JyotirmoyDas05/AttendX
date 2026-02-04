package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import javax.inject.Inject

class GetNextClassUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    operator fun invoke(): Flow<TimeTableScheduleWithSubject?> {
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek.value
        val currentMinutes = now.hour * 60 + now.minute
        val currentWeekType = getCurrentWeekType()

        return repository.getClassesForDayWithSubject(currentDay).map { classes ->
            classes
                .filter { matchesWeekPattern(it.schedule.weekPattern, currentWeekType) }
                .firstOrNull { it.schedule.startTime > currentMinutes }
        }
    }

    private fun matchesWeekPattern(pattern: String, currentWeek: String): Boolean {
        return when (pattern) {
            "odd" -> currentWeek == "odd"
            "even" -> currentWeek == "even"
            else -> true
        }
    }

    private fun getCurrentWeekType(): String {
        val weekOfYear = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        return if (weekOfYear % 2 == 1) "odd" else "even"
    }
}
