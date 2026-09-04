package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleWithSubject
import `in`.jyotirmoy.attendx.timetable.domain.WeekPattern
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
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
                val date = now.toLocalDate()
                classes
                    .filter { WeekPattern.matches(it.schedule.weekPattern, date) }
                    .firstOrNull { 
                        currentMinutes >= it.schedule.startTime && currentMinutes < it.schedule.endTime 
                    }
            }
        }
    }

}
