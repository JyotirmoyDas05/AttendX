package `in`.jyotirmoy.attendx.notification.worker

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.jyotirmoy.attendx.notification.NotificationSetup
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimetableWorkerEntryPoint {
    fun timeTableRepository(): TimeTableRepository
}

class TimetableNotificationWorker(
    @param:ApplicationContext private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "TimetableWorker"
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "=== Checking for classes starting soon ===")
            
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                TimetableWorkerEntryPoint::class.java
            )
            val timeTableRepository = entryPoint.timeTableRepository()

            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val currentDay = now.dayOfWeek.value // 1 = Monday, 7 = Sunday

            Log.d(TAG, "Current time: $currentTime, Day: $currentDay")

            val todaysClasses = timeTableRepository.getAllClassesWithSubject().first()
                .filter { it.schedule.isActive && it.schedule.dayOfWeek == currentDay }
            Log.d(TAG, "Found ${todaysClasses.size} classes for today")

            // Ids already on screen, posted by TimetableAlarmReceiver's exact alarm.
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alreadyPosted = notificationManager.activeNotifications.map { it.id }.toSet()

            var notificationsShown = 0

            for (item in todaysClasses) {
                val subjectName = item.subject.subject
                val startTimeStr = formatMinutes(item.schedule.startTime)
                val endTimeStr = formatMinutes(item.schedule.endTime)
                val classStartTime = LocalTime.parse(startTimeStr)

                // This worker is a BACKUP for an exact alarm the OEM may have killed. It must never
                // fire before the class actually starts -- doing so posted a "Class Started"
                // alongside the T-5min "Upcoming Class" notification. Only cover the window from
                // the start time to one WorkManager cycle later, and only if the alarm did not post.
                val minutesSinceStart = java.time.Duration.between(classStartTime, currentTime).toMinutes()
                Log.d(TAG, "Subject: $subjectName, Start: $classStartTime, Minutes since: $minutesSinceStart")

                if (minutesSinceStart in 0..14 && item.schedule.id !in alreadyPosted) {
                    Log.d(TAG, "Exact alarm did not post for $subjectName - showing backup notification")
                    try {
                        // Same notificationId as TimetableAlarmReceiver, so a late alarm replaces
                        // this instead of stacking a second notification.
                        NotificationSetup.showTimetableNotification(
                            context = applicationContext,
                            subjectId = item.schedule.subjectId,
                            subjectName = subjectName,
                            startTime = startTimeStr,
                            endTime = endTimeStr,
                            location = item.schedule.room,
                            scheduleId = item.schedule.id
                        )
                        notificationsShown++
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing notification for $subjectName", e)
                    }
                }
            }
            
            Log.d(TAG, "Check complete. Notifications shown: $notificationsShown")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking timetable", e)
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun formatMinutes(minutes: Long): String =
        String.format("%02d:%02d", minutes / 60, minutes % 60)
}
