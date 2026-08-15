package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoField
import javax.inject.Inject

class GetCurrentClassUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<TimeTableScheduleWithSubject?> {
        val ticker = flow {
            while (true) {
                emit(LocalDateTime.now())
                delay(10_000L) // Realtime 10-second tick
            }
        }
        val dayFlow = ticker.map { it.dayOfWeek.value }.distinctUntilChanged()

        return dayFlow.flatMapLatest { day ->
            combine(repository.getClassesForDayWithSubject(day), ticker) { classes, now ->
                val currentMinutes = now.hour * 60 + now.minute
                val currentWeekType = getCurrentWeekType(now.toLocalDate())
                classes
                    .filter { matchesWeekPattern(it.schedule.weekPattern, currentWeekType) }
                    .firstOrNull { 
                        currentMinutes >= it.schedule.startTime && currentMinutes < it.schedule.endTime 
                    }
            }
        }
    }

    private fun matchesWeekPattern(pattern: String, currentWeek: String): Boolean {
        return when (pattern) {
            "odd" -> currentWeek == "odd"
            "even" -> currentWeek == "even"
            else -> true
        }
    }

    private fun getCurrentWeekType(date: LocalDate): String {
        val weekOfYear = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        return if (weekOfYear % 2 == 1) "odd" else "even"
    }
}
