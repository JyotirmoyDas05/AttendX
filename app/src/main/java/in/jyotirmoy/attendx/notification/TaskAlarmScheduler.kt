package `in`.jyotirmoy.attendx.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object TaskAlarmScheduler {
    private const val TAG = "TaskAlarmScheduler"
    const val TASK_ALARM_OFFSET = 5_000_000

    fun scheduleTaskReminder(
        context: Context,
        taskId: Int,
        subjectId: Int,
        subjectName: String,
        taskTitle: String,
        taskDescription: String?,
        dueDateMillis: Long,
        reminderMinutes: Long
    ): Boolean {
        if (taskId == 0 || reminderMinutes <= 0 || dueDateMillis <= 0) return false

        val triggerAtMillis = dueDateMillis - (reminderMinutes * 60 * 1000L)
        val now = System.currentTimeMillis()

        if (triggerAtMillis <= now) {
            Log.d(TAG, "Task $taskId reminder trigger time is in past ($triggerAtMillis <= $now), skipping.")
            return false
        }

        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        val intent = Intent(appContext, TaskAlarmReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("subjectId", subjectId)
            putExtra("subjectName", subjectName)
            putExtra("taskTitle", taskTitle)
            putExtra("taskDescription", taskDescription)
            putExtra("dueDateMillis", dueDateMillis)
            putExtra("reminderMinutes", reminderMinutes)
        }

        val requestCode = taskId + TASK_ALARM_OFFSET
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = TimetableAlarmScheduler.canScheduleExactAlarms(appContext)

        return try {
            if (canScheduleExact) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val clockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
                    alarmManager.setAlarmClock(clockInfo, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled task reminder for '$taskTitle' (taskId=$taskId) at $triggerAtMillis")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule task reminder for '$taskTitle'", e)
            false
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Int) {
        if (taskId == 0) return
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(appContext, TaskAlarmReceiver::class.java)
        val requestCode = taskId + TASK_ALARM_OFFSET
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled task reminder for taskId=$taskId")
        }
    }
}
