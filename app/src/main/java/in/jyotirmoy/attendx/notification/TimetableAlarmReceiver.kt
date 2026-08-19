package `in`.jyotirmoy.attendx.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.settings.data.local.provider.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that fires at the exact scheduled time.
 */
class TimetableAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "TimetableAlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Mandatory Debug Log
        Log.e("ALARM_DEBUG", "🔥 Timetable alarm RECEIVED 🔥")
        Log.d(TAG, "Action: ${intent.action}")
        Log.d(TAG, "Subject: ${intent.getStringExtra("subjectName")}")
        Log.d(TAG, "Type: ${intent.getStringExtra("type")}")
        Log.d(TAG, "Schedule ID: ${intent.getIntExtra("scheduleId", -1)}")

        val appContext = context.applicationContext
        
        // Extract data
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        val subjectName = intent.getStringExtra("subjectName") ?: "Unknown"
        val startTime = intent.getStringExtra("startTime") ?: ""
        val endTime = intent.getStringExtra("endTime") ?: ""
        val location = intent.getStringExtra("location")
        val type = intent.getStringExtra("type") ?: "START"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        if (scheduleId == -1 || subjectId == -1) {
            Log.e(TAG, "❌ Invalid IDs, aborting.")
            return
        }

        // 2. Runtime Permission Check (Android 13+)
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Check settings and show notification in Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = appContext.settingsDataStore.data.first()
                val masterEnabled = settings[androidx.datastore.preferences.core.booleanPreferencesKey(`in`.jyotirmoy.attendx.settings.data.local.SettingsKeys.ENABLE_NOTIFICATIONS.name)] ?: true
                val timetableEnabled = settings[androidx.datastore.preferences.core.booleanPreferencesKey(`in`.jyotirmoy.attendx.settings.data.local.SettingsKeys.ENABLE_TIMETABLE_NOTIFICATIONS.name)] ?: true
                val notificationsEnabled = masterEnabled && timetableEnabled
                
                Log.d(TAG, "Timetable notifications enabled in settings: $notificationsEnabled (master=$masterEnabled, timetable=$timetableEnabled)")
                
                val db = `in`.jyotirmoy.attendx.core.data.database.SubjectDatabase.getDatabase(appContext)

                // Fallback: resolve subject name from Room DB if missing or "Unknown"
                var resolvedSubjectName = subjectName
                if (resolvedSubjectName == "Unknown" || resolvedSubjectName.isBlank()) {
                    try {
                        val entity = db.subjectDao().getSubjectById(subjectId).first()
                        if (entity != null && entity.subject.isNotBlank()) {
                            resolvedSubjectName = entity.subject
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to resolve subject from DB", e)
                    }
                }

                // Fallback: resolve times from Room DB if missing
                var resolvedStartTime = startTime
                var resolvedEndTime = endTime
                var resolvedDayOfWeek = dayOfWeek
                var resolvedLocation = location

                if (resolvedStartTime.isBlank() || resolvedEndTime.isBlank() || resolvedDayOfWeek == -1) {
                    try {
                        val classes = db.timeTableDao().getAllClasses().first()
                        val classSchedule = classes.find { it.id == scheduleId }
                        if (classSchedule != null) {
                            if (resolvedStartTime.isBlank()) {
                                resolvedStartTime = String.format("%02d:%02d", classSchedule.startTime / 60, classSchedule.startTime % 60)
                            }
                            if (resolvedEndTime.isBlank()) {
                                resolvedEndTime = String.format("%02d:%02d", classSchedule.endTime / 60, classSchedule.endTime % 60)
                            }
                            if (resolvedDayOfWeek == -1) {
                                resolvedDayOfWeek = classSchedule.dayOfWeek
                            }
                            if (resolvedLocation.isNullOrBlank()) {
                                resolvedLocation = classSchedule.room
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to resolve schedule from DB", e)
                    }
                }

                if (!notificationsEnabled) {
                    Log.d(TAG, "⚠️ Notifications disabled by user.")
                    rescheduleForNextWeek(appContext, scheduleId, subjectId, resolvedSubjectName, resolvedStartTime, resolvedEndTime, resolvedLocation, type, resolvedDayOfWeek)
                    return@launch
                }

                // 3. Show Notification
                if (hasNotificationPermission) {
                    if (type == "START") {
                        try {
                            Log.d(TAG, "Showing START notification for $resolvedSubjectName")
                            showNotification(appContext, subjectId, resolvedSubjectName, resolvedStartTime, resolvedEndTime, resolvedLocation, scheduleId)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error showing notification", e)
                        }
                    } else if (type == "UPCOMING") {
                        try {
                            Log.d(TAG, "Showing UPCOMING notification for $resolvedSubjectName")
                            showUpcomingNotification(appContext, subjectId, resolvedSubjectName, resolvedStartTime, resolvedLocation, scheduleId)
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error showing upcoming notification", e)
                        }
                    }
                } else {
                    Log.e(TAG, "❌ POST_NOTIFICATIONS permission missing! Notification dropped.")
                }

                // 4. Reschedule
                try {
                    rescheduleForNextWeek(appContext, scheduleId, subjectId, resolvedSubjectName, resolvedStartTime, resolvedEndTime, resolvedLocation, type, resolvedDayOfWeek)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error rescheduling", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error accessing settings", e)
            }
        }
    }

    private fun showNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        scheduleId: Int
    ) {
        val formattedStart = `in`.jyotirmoy.attendx.core.utils.TimeUtils.format24To12Hour(startTime)
        val formattedEnd = `in`.jyotirmoy.attendx.core.utils.TimeUtils.format24To12Hour(endTime)
        val duration = `in`.jyotirmoy.attendx.core.utils.TimeUtils.formatDuration(
            `in`.jyotirmoy.attendx.core.utils.TimeUtils.calculateDuration(startTime, endTime)
        )

        val message = buildString {
            append("$formattedStart - $formattedEnd ($duration)")
            if (!location.isNullOrBlank()) {
                append("\nLocation: $location")
            }
        }

        // Use valid app icon
        val iconRes = R.drawable.ic_notifications

        `in`.jyotirmoy.attendx.notification.helper.NotificationHelper.showNotificationWithActions(
            context = context,
            channelId = NotificationSetup.TIMETABLE_CHANNEL_ID,
            channelName = "Class Timetable",
            channelDescription = "Notifications for scheduled classes",
            notificationId = scheduleId,
            title = "Class Started: $subjectName",
            message = message,
            smallIconResId = iconRes,
            subjectId = subjectId,
            scheduleId = scheduleId,
            subjectName = subjectName
        )
    }

    private fun showUpcomingNotification(
        context: Context,
        subjectId: Int,
        subjectName: String,
        startTime: String,
        location: String?,
        scheduleId: Int
    ) {
        val formattedStart = `in`.jyotirmoy.attendx.core.utils.TimeUtils.format24To12Hour(startTime)
        
        val message = buildString {
            append("$subjectName Class will be starting in 5 minutes.")
            if (!location.isNullOrBlank()) {
                append("\nGo to: $location")
            } else {
                append("\nGo to your assigned room.")
            }
        }

        // Use valid app icon
        val iconRes = R.drawable.ic_notifications

        `in`.jyotirmoy.attendx.notification.helper.NotificationHelper.showNotification(
            context = context,
            channelId = NotificationSetup.TIMETABLE_CHANNEL_ID,
            channelName = "Class Timetable",
            channelDescription = "Notifications for scheduled classes",
            notificationId = scheduleId + 2_000_000, // Use unique ID for upcoming
            title = "Upcoming Class: $subjectName",
            message = message,
            smallIconResId = iconRes,
            subjectId = subjectId,
            subjectName = subjectName
        )
    }

    private fun rescheduleForNextWeek(
        context: Context,
        scheduleId: Int,
        subjectId: Int,
        subjectName: String,
        startTime: String,
        endTime: String,
        location: String?,
        type: String,
        dayOfWeek: Int
    ) {
        if (scheduleId == -1 || dayOfWeek == -1 || startTime.isBlank() || endTime.isBlank()) return

        Log.d(TAG, "🔄 Rescheduling $type for next week...")
        TimetableAlarmScheduler.scheduleAlarm(
            context = context,
            scheduleId = scheduleId,
            subjectId = subjectId,
            subjectName = subjectName,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            location = location,
            type = type
        )
    }
}
