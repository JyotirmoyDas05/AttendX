package `in`.jyotirmoy.attendx.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.core.domain.repository.ClassScheduleRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.first

import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository

@InstallIn(SingletonComponent::class)
@EntryPoint
interface RescheduleWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun classScheduleRepository(): ClassScheduleRepository
    fun timeTableRepository(): TimeTableRepository
}

class RescheduleAlarmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, RescheduleWorkerEntryPoint::class.java)
            val subjectRepository = entryPoint.subjectRepository()
            val classScheduleRepository = entryPoint.classScheduleRepository()
            val timeTableRepository = entryPoint.timeTableRepository()

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

            // 2. Reschedule legacy classes
            subjects.forEach { subject ->
                val schedules = classScheduleRepository.getSchedulesForSubject(subject.id).first()

                schedules.forEach { schedule ->
                    if (schedule.isEnabled) {
                        TimetableAlarmScheduler.scheduleClassAlarms(
                            context = applicationContext,
                            scheduleId = schedule.id,
                            subjectId = schedule.subjectId,
                            subjectName = subject.subject,
                            dayOfWeek = schedule.dayOfWeek,
                            startTime = schedule.startTime,
                            endTime = schedule.endTime,
                            location = schedule.location
                        )
                    }
                }
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
