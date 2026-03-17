package `in`.jyotirmoy.attendx.notification.worker

import android.Manifest
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
import `in`.jyotirmoy.attendx.core.domain.repository.ClassScheduleRepository
import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.core.domain.model.toDomain
import `in`.jyotirmoy.attendx.notification.NotificationSetup
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.DayOfWeek

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TimetableWorkerEntryPoint {
    fun subjectRepository(): SubjectRepository
    fun classScheduleRepository(): ClassScheduleRepository
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
            val subjectRepository = entryPoint.subjectRepository()
            val classScheduleRepository = entryPoint.classScheduleRepository()

            val now = LocalDateTime.now()
            val currentTime = now.toLocalTime()
            val currentDay = now.dayOfWeek.value // 1 = Monday, 7 = Sunday
            
            Log.d(TAG, "Current time: $currentTime, Day: $currentDay")

            // Get all subjects
            val subjects = subjectRepository.getAllSubjects().first()
            Log.d(TAG, "Found ${subjects.size} subjects")
            
            var notificationsShown = 0

            for (subject in subjects) {
                // Get all schedules for this subject
                val schedules = classScheduleRepository.getSchedulesForSubject(subject.id)
                    .first()
                    .map { it.toDomain() }

                for (schedule in schedules) {
                    // Check if this schedule is for today
                    if (schedule.dayOfWeek == currentDay) {
                        val classStartTime = LocalTime.parse(schedule.startTime)
                        val classEndTime = LocalTime.parse(schedule.endTime)
                        
                        // Check if class is starting within the next 15 minutes or just started (within 5 min)
                        val minutesUntilStart = java.time.Duration.between(currentTime, classStartTime).toMinutes()
                        val minutesSinceStart = java.time.Duration.between(classStartTime, currentTime).toMinutes()
                        
                        Log.d(TAG, "Subject: ${subject.subject}, Start: $classStartTime, Minutes until: $minutesUntilStart")
                        
                        // Show notification if class starts within 15 minutes or just started (within 10 min)
                        // Changed from -5..5 to -10..15 for more reliable coverage
                        if (minutesUntilStart in -10..15) {
                            Log.d(TAG, "Showing notification for ${subject.subject}")
                            try {
                                NotificationSetup.showTimetableNotification(
                                    context = applicationContext,
                                    subjectId = subject.id,
                                    subjectName = subject.subject,
                                    startTime = schedule.startTime,
                                    endTime = schedule.endTime,
                                    location = schedule.location,
                                    scheduleId = schedule.id
                                )
                                notificationsShown++
                            } catch (e: Exception) {
                                Log.e(TAG, "Error showing notification for ${subject.subject}", e)
                            }
                        }
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
}
