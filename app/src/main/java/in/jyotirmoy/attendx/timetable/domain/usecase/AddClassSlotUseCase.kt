package `in`.jyotirmoy.attendx.timetable.domain.usecase

import android.content.Context
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import `in`.jyotirmoy.attendx.timetable.presentation.notification.TimeTableNotificationScheduler
import javax.inject.Inject

class AddClassSlotUseCase @Inject constructor(
    private val repository: TimeTableRepository,
    private val scheduler: TimeTableNotificationScheduler,
    private val context: Context // Need context for alarm manager
) {
    suspend operator fun invoke(schedule: TimeTableScheduleEntity) {
        repository.insertClass(schedule)
        scheduler.scheduleClassAlarm(context, schedule)
    }
}
