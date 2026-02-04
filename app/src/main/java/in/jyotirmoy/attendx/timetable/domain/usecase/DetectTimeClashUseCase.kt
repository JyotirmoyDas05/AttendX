package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import javax.inject.Inject

class DetectTimeClashUseCase @Inject constructor(
    private val repository: TimeTableRepository
) {
    /**
     * returns list of overlapping classes, or empty list if safe.
     */
    suspend operator fun invoke(day: Int, start: Long, end: Long): List<TimeTableScheduleEntity> {
        return repository.checkTimeClash(day, start, end)
    }
}
