package `in`.jyotirmoy.attendx.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import `in`.jyotirmoy.attendx.R
import `in`.jyotirmoy.attendx.notification.helper.NotificationHelper

object NotificationSetup {
    const val GROUP_ID = "attendx_notifications_group"
    const val ATTENDANCE_CHANNEL_ID = "attendance_channel"
    const val UPDATE_CHANNEL_ID = "update_channel"
    const val TIMETABLE_CHANNEL_ID = "timetable_channel"
    const val TASK_REMINDER_CHANNEL_ID = "task_reminder_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val group = NotificationChannelGroup(GROUP_ID, "AttendX Notifications")
            notificationManager.createNotificationChannelGroup(group)

            val channels = listOf(
                NotificationChannel(
                    TIMETABLE_CHANNEL_ID,
                    "Class Timetable",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for scheduled classes"
                    this.group = GROUP_ID
                },
                NotificationChannel(
                    TASK_REMINDER_CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for assignments and tasks"
                    this.group = GROUP_ID
                },
                NotificationChannel(
                    ATTENDANCE_CHANNEL_ID,
                    context.getString(R.string.attendance),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.daily_reminder_to_mark_attendance)
                    this.group = GROUP_ID
                },
                NotificationChannel(
                    UPDATE_CHANNEL_ID,
                    context.getString(R.string.updates),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.des_notify_update_available)
                    this.group = GROUP_ID
                },
                NotificationChannel(
                    "pomodoro_timer_channel",
                    "Pomodoro Timer",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Active timer notification"
                    this.group = GROUP_ID
                }
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showAttendanceReminderNotification(context: Context) {
        NotificationHelper.showNotification(
            context = context,
            channelId = ATTENDANCE_CHANNEL_ID,
            channelName = context.getString(R.string.attendance),
            channelDescription = context.getString(R.string.daily_reminder_to_mark_attendance),
            notificationId = 1001,
            title = context.getString(R.string.mark_your_attendance),
            message = context.getString(R.string.des_mark_your_attendance),
            smallIconResId = R.drawable.ic_check_circle,
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMissedAttendanceNotification(context: Context) {
        NotificationHelper.showNotification(
            context = context,
            channelId = ATTENDANCE_CHANNEL_ID,
            channelName = context.getString(R.string.attendance),
            channelDescription = context.getString(R.string.daily_reminder_to_mark_attendance),
            notificationId = 1002,
            title = context.getString(R.string.mark_your_attendance),
            message = context.getString(R.string.des_attendance_missed),
            smallIconResId = R.drawable.ic_check_circle,
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showUpdateAvailableNotification(context: Context) {
        NotificationHelper.showNotification(
            context = context,
            channelId = UPDATE_CHANNEL_ID,
            channelName = context.getString(R.string.updates),
            channelDescription = context.getString(R.string.des_notify_update_available),
            notificationId = 1003,
            title = context.getString(R.string.update_available),
            message = context.getString(R.string.des_update_available),
            smallIconResId = R.drawable.ic_release_alert,
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTimetableNotification(
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

        NotificationHelper.showNotificationWithActions(
            context = context,
            channelId = TIMETABLE_CHANNEL_ID,
            channelName = "Class Timetable",
            channelDescription = "Notifications for scheduled classes",
            notificationId = scheduleId,
            title = "Class Started: $subjectName",
            message = message,
            smallIconResId = R.drawable.ic_bear_notifications_monochrome,
            subjectId = subjectId,
            scheduleId = scheduleId,
            subjectName = subjectName
        )
    }
}
