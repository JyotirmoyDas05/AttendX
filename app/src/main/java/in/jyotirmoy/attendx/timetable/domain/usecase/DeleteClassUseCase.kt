package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import javax.inject.Inject

import android.content.Context

class DeleteClassUseCase @Inject constructor(
    private val repository: TimeTableRepository,
    private val context: Context
) {
    suspend operator fun invoke(id: Int) {
        cancelAlarms(id)
        repository.deleteClass(id)
    }
    
    suspend operator fun invoke(ids: List<Int>) {
        ids.forEach { 
            cancelAlarms(it)
            repository.deleteClass(it) 
        }
    }

    private fun cancelAlarms(id: Int) {
        `in`.jyotirmoy.attendx.notification.TimetableAlarmScheduler.cancelClassAlarms(context, id)
        `in`.jyotirmoy.attendx.notification.ClassNotificationScheduler.cancelScheduleNotification(context, id)
    }
}
