package `in`.jyotirmoy.attendx.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.first

import `in`.jyotirmoy.attendx.core.data.database.SubjectTaskDao
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository

@InstallIn(SingletonComponent::class)
@EntryPoint
interface RescheduleWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun timeTableRepository(): TimeTableRepository
    fun subjectTaskDao(): SubjectTaskDao
}

class RescheduleAlarmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, RescheduleWorkerEntryPoint::class.java)
            val subjectRepository = entryPoint.subjectRepository()
            val timeTableRepository = entryPoint.timeTableRepository()
            val subjectTaskDao = entryPoint.subjectTaskDao()

            // Cleanup stale legacy channel
            deleteLegacyChannel(applicationContext)

            val subjects = subjectRepository.getAllSubjects().first()
            val subjectsMap = subjects.associateBy { it.id }

            // 1. Reschedule Timetable classes
            try {
                val timetableClasses = timeTableRepository.getAllClassesWithSubject().first()
                for (item in timetableClasses) {
                    if (!item.schedule.isActive) continue
                    val startH = item.schedule.startTime / 60
                    val startM = item.schedule.startTime % 60
                    val startTimeStr = String.format("%02d:%02d", startH, startM)

                    val endH = item.schedule.endTime / 60
                    val endM = item.schedule.endTime % 60
                    val endTimeStr = String.format("%02d:%02d", endH, endM)

                    TimetableAlarmScheduler.scheduleClassAlarms(
                        context = applicationContext,
                        scheduleId = item.schedule.id,
                        subjectId = item.schedule.subjectId,
                        subjectName = item.subject.subject,
                        dayOfWeek = item.schedule.dayOfWeek,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        location = item.schedule.room
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Reschedule Pending Task Reminders
            // NOTE: the legacy class_schedules table is intentionally NOT rescheduled -- its
            // row ids collide with class_schedule in the alarm request-code space.
            try {
                val pendingTasks = subjectTaskDao.getPendingTasksWithReminders()
                for (task in pendingTasks) {
                    if (task.dueDate != null && task.reminderMinutes != null) {
                        val subjectName = subjectsMap[task.subjectId]?.subject ?: "Class"
                        TaskAlarmScheduler.scheduleTaskReminder(
                            context = applicationContext,
                            taskId = task.id,
                            subjectId = task.subjectId,
                            subjectName = subjectName,
                            taskTitle = task.title,
                            taskDescription = task.description,
                            dueDateMillis = task.dueDate,
                            reminderMinutes = task.reminderMinutes
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun deleteLegacyChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.deleteNotificationChannel("class_timetable_alarms")
    }
}
