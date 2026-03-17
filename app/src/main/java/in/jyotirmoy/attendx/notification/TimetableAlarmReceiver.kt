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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "❌ POST_NOTIFICATIONS permission missing! Notification dropped.")
                rescheduleForNextWeek(appContext, intent)
                return
            }
        }

        // Check settings and show notification in Coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = appContext.settingsDataStore.data.first()
                val notificationsEnabled = settings[androidx.datastore.preferences.core.booleanPreferencesKey(`in`.jyotirmoy.attendx.settings.data.local.SettingsKeys.ENABLE_TIMETABLE_NOTIFICATIONS.name)] ?: true
                
                Log.d(TAG, "Timetable notifications enabled in settings: $notificationsEnabled")
                
                if (!notificationsEnabled) {
                    Log.d(TAG, "⚠️ Notifications disabled by user.")
                    rescheduleForNextWeek(appContext, intent)
                    return@launch
                }

                // 3. Show Notification
                if (type == "START") {
                    try {
                        Log.d(TAG, "Showing START notification for $subjectName")
                        showNotification(appContext, subjectId, subjectName, startTime, endTime, location, scheduleId)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error showing notification", e)
                    }
                } else if (type == "UPCOMING") {
                    try {
                        Log.d(TAG, "Showing UPCOMING notification for $subjectName")
                        showUpcomingNotification(appContext, subjectId, subjectName, startTime, location, scheduleId)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error showing upcoming notification", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error accessing settings", e)
            }

            // 4. Reschedule
            try {
                rescheduleForNextWeek(appContext, intent)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error rescheduling", e)
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
            scheduleId = scheduleId
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
            smallIconResId = iconRes
        )
    }

    private fun rescheduleForNextWeek(context: Context, intent: Intent) {
        val scheduleId = intent.getIntExtra("scheduleId", -1)
        val subjectId = intent.getIntExtra("subjectId", -1)
        val subjectName = intent.getStringExtra("subjectName") ?: return
        val startTime = intent.getStringExtra("startTime") ?: return
        val endTime = intent.getStringExtra("endTime") ?: return
        val location = intent.getStringExtra("location")
        val type = intent.getStringExtra("type") ?: "START"
        val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

        if (scheduleId == -1 || dayOfWeek == -1) return

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
