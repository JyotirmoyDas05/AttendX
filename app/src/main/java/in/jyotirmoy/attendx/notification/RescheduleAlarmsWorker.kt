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

@InstallIn(SingletonComponent::class)
@EntryPoint
interface RescheduleWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun classScheduleRepository(): ClassScheduleRepository
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

            // Cleanup stale legacy channel
            deleteLegacyChannel(applicationContext)

            val subjects = subjectRepository.getAllSubjects().first()

            subjects.forEach { subject ->
                val schedules = classScheduleRepository.getSchedulesForSubject(subject.id).first()

                schedules.forEach { schedule ->
                    if (schedule.isEnabled) {
                        // Use the new unified scheduler
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

    // Remove old channel created by ClassAlarmReceiver
    private fun deleteLegacyChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.deleteNotificationChannel("class_timetable_alarms")
    }
}
