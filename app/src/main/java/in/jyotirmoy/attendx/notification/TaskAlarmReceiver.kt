package `in`.jyotirmoy.attendx.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase
import `in`.jyotirmoy.attendx.notification.helper.NotificationHelper
import `in`.jyotirmoy.attendx.settings.data.local.SettingsKeys
import `in`.jyotirmoy.attendx.settings.data.local.provider.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TaskAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val taskId = intent.getIntExtra("taskId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        var subjectName = intent.getStringExtra("subjectName") ?: ""
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Task Reminder"
        val taskDescription = intent.getStringExtra("taskDescription")
        val dueDateMillis = intent.getLongExtra("dueDateMillis", 0L)
        val reminderMinutes = intent.getLongExtra("reminderMinutes", 0L)

        if (taskId == -1 || subjectId == -1) {
            Log.e(TAG, "Invalid task or subject ID, dropping.")
            return
        }

        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasNotificationPermission) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted, dropping task reminder.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SubjectDatabase.getDatabase(appContext)

                // 1. Check if task still exists and is not completed
                val task = db.subjectTaskDao().getTaskById(taskId)
                if (task == null || task.isCompleted) {
                    Log.d(TAG, "Task $taskId is already completed or deleted, skipping notification.")
                    return@launch
                }

                // 2. Check master notification settings
                val settings = appContext.settingsDataStore.data.first()
                val masterEnabled = settings[booleanPreferencesKey(SettingsKeys.ENABLE_NOTIFICATIONS.name)] ?: true
                if (!masterEnabled) {
                    Log.d(TAG, "Notifications disabled by user in settings.")
                    return@launch
                }

                // 3. Resolve Subject Name
                if (subjectName.isBlank() || subjectName.equals("Unknown", ignoreCase = true) || subjectName.equals("Class", ignoreCase = true)) {
                    val subjectEntity = db.subjectDao().getSubjectEntityById(subjectId)
                    if (subjectEntity != null && subjectEntity.subject.isNotBlank()) {
                        subjectName = subjectEntity.subject
                    }
                }

                // 4. Format due date / time message
                val formattedDueDate = if (dueDateMillis > 0) {
                    val sdf = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault())
                    sdf.format(Date(dueDateMillis))
                } else null

                val message = buildString {
                    if (reminderMinutes > 0) {
                        val reminderText = if (reminderMinutes >= 1440) {
                            "${reminderMinutes / 1440} day(s) before"
                        } else if (reminderMinutes >= 60) {
                            "${reminderMinutes / 60} hour(s) before"
                        } else {
                            "$reminderMinutes min before"
                        }
                        append("Due $reminderText")
                        if (formattedDueDate != null) append(" ($formattedDueDate)")
                    } else if (formattedDueDate != null) {
                        append("Due: $formattedDueDate")
                    }
                    if (!taskDescription.isNullOrBlank()) {
                        append("\n$taskDescription")
                    }
                }

                val title = if (subjectName.isNotBlank()) {
                    "Task Due: $taskTitle ($subjectName)"
                } else {
                    "Task Due: $taskTitle"
                }

                NotificationHelper.showNotification(
                    context = appContext,
                    channelId = NotificationSetup.TASK_REMINDER_CHANNEL_ID,
                    channelName = "Task Reminders",
                    channelDescription = "Reminders for assignments and tasks",
                    notificationId = taskId + TaskAlarmScheduler.TASK_ALARM_OFFSET,
                    title = title,
                    message = message,
                    smallIconResId = R.drawable.ic_bear_notifications_monochrome,
                    subjectId = subjectId,
                    subjectName = subjectName
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing task reminder notification", e)
            }
        }
    }
}
