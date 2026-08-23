package `in`.jyotirmoy.attendx.timetable.presentation.notification

import android.content.Context
import android.util.Log
import `in`.jyotirmoy.attendx.core.data.database.SubjectDao
import `in`.jyotirmoy.attendx.notification.TimetableAlarmScheduler
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import javax.inject.Inject

class TimeTableNotificationScheduler @Inject constructor(
    private val subjectDao: SubjectDao
) {

    companion object {
        private const val TAG = "TimeTableScheduler"
    }

    suspend fun scheduleClassAlarm(context: Context, schedule: TimeTableScheduleEntity) {
        if (!schedule.isActive) return

        val appContext = context.applicationContext
        val startFormatted = formatTime(schedule.startTime)
        val endFormatted = formatTime(schedule.endTime)

        // Resolve subject name directly from database
        val subjectName = try {
            subjectDao.getSubjectEntityById(schedule.subjectId)?.subject ?: "Class"
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subject name for schedule ${schedule.id}", e)
            "Class"
        }

        TimetableAlarmScheduler.scheduleClassAlarms(
            context = appContext,
            scheduleId = schedule.id,
            subjectId = schedule.subjectId,
            subjectName = subjectName,
            dayOfWeek = schedule.dayOfWeek,
            startTime = startFormatted,
            endTime = endFormatted,
            location = schedule.room
        )
    }

    private fun formatTime(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }
}
