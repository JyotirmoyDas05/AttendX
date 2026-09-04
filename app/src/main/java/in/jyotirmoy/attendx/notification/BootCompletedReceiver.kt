package `in`.jyotirmoy.attendx.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles device boot to reschedule all timetable alarms.
 * This ensures notifications survive device reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        ) {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔄 Device boot/permission change detected - rescheduling alarms")
            Log.d(TAG, "   Action: ${intent.action}")

            // Cleanup stale legacy notification channel
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.deleteNotificationChannel("class_timetable_alarms")

            // Use goAsync() for long-running work in receiver
            val pendingResult = goAsync()
            
            // Schedule alarms in background
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    rescheduleAllAlarms(context.applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to reschedule alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun rescheduleAllAlarms(context: Context) {
        Log.d(TAG, "📚 Fetching all schedules from database...")

        val database = `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase.getDatabase(context)
        val timeTableDao = database.timeTableDao()
        val subjectDao = database.subjectDao()

        var successCount = 0
        var failCount = 0

        // 1. Reschedule Timetable classes (TimeTableScheduleEntity)
        try {
            val timetableClasses = timeTableDao.getAllClassesWithSubject().first()
            Log.d(TAG, "📋 Found ${timetableClasses.size} timetable classes to reschedule")
            for (item in timetableClasses) {
                if (!item.schedule.isActive) continue
                val startH = item.schedule.startTime / 60
                val startM = item.schedule.startTime % 60
                val startTimeStr = String.format("%02d:%02d", startH, startM)

                val endH = item.schedule.endTime / 60
                val endM = item.schedule.endTime % 60
                val endTimeStr = String.format("%02d:%02d", endH, endM)

                val scheduled = TimetableAlarmScheduler.scheduleClassAlarms(
                    context = context,
                    scheduleId = item.schedule.id,
                    subjectId = item.schedule.subjectId,
                    subjectName = item.subject.subject,
                    dayOfWeek = item.schedule.dayOfWeek,
                    startTime = startTimeStr,
                    endTime = endTimeStr,
                    location = item.schedule.room
                )
                if (scheduled) successCount++ else failCount++
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to reschedule timetable classes", e)
        }

        // 2. Reschedule Pending Task Reminders
        // NOTE: the legacy class_schedules table is intentionally NOT rescheduled. Its row ids
        // share the alarm request-code / notification-id space with class_schedule, so stale
        // legacy rows would silently overwrite real timetable alarms. It is kept only for
        // backup restore compatibility (BackupAndRestoreRepositoryImpl).
        try {
            val pendingTasks = database.subjectTaskDao().getPendingTasksWithReminders()
            Log.d(TAG, "📋 Found ${pendingTasks.size} pending task reminders to reschedule")
            for (task in pendingTasks) {
                if (task.dueDate != null && task.reminderMinutes != null) {
                    val subject = subjectDao.getSubjectEntityById(task.subjectId)
                    val subjectName = subject?.subject ?: "Class"
                    val scheduled = TaskAlarmScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = task.id,
                        subjectId = task.subjectId,
                        subjectName = subjectName,
                        taskTitle = task.title,
                        taskDescription = task.description,
                        dueDateMillis = task.dueDate,
                        reminderMinutes = task.reminderMinutes
                    )
                    if (scheduled) successCount++
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to reschedule task reminders", e)
        }

        Log.d(TAG, "📊 Boot/Permission Reschedule Summary: Success=$successCount, Failed=$failCount")
    }
}
