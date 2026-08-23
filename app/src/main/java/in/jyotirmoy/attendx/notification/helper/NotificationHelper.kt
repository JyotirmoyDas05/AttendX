package `in`.jyotirmoy.attendx.notification.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import `in`.jyotirmoy.attendx.MainActivity
import `in`.jyotirmoy.attendx.R

object NotificationHelper {

    fun getAppIconBitmap(context: Context): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher) ?: return null
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        notificationId: Int,
        title: String,
        message: String,
        @DrawableRes smallIconResId: Int = R.drawable.ic_bear_notifications_monochrome,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        subjectId: Int = -1,
        subjectName: String? = null
    ) {
        createNotificationChannel(context, channelId, channelName, channelDescription)

        val notificationSmallIcon =
            if (smallIconResId == 0 || smallIconResId == android.R.drawable.ic_dialog_info) {
                R.drawable.ic_bear_notifications_monochrome
            } else {
                smallIconResId
            }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (subjectId != -1) {
                putExtra("EXTRA_NAVIGATE_TO", "CALENDAR")
                putExtra("EXTRA_SUBJECT_ID", subjectId)
                putExtra("EXTRA_SUBJECT_NAME", subjectName ?: "")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(notificationSmallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotificationWithActions(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        notificationId: Int,
        title: String,
        message: String,
        @DrawableRes smallIconResId: Int = R.drawable.ic_bear_notifications_monochrome,
        subjectId: Int,
        scheduleId: Int,
        subjectName: String? = null,
        priority: Int = NotificationCompat.PRIORITY_HIGH
    ) {
        createNotificationChannel(context, channelId, channelName, channelDescription)

        val notificationSmallIcon =
            if (smallIconResId == 0 || smallIconResId == android.R.drawable.ic_dialog_info) {
                R.drawable.ic_bear_notifications_monochrome
            } else {
                smallIconResId
            }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (subjectId != -1) {
                putExtra("EXTRA_NAVIGATE_TO", "CALENDAR")
                putExtra("EXTRA_SUBJECT_ID", subjectId)
                putExtra("EXTRA_SUBJECT_NAME", subjectName ?: "")
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create action intents for Attended and Missed
        val attendedIntent = Intent(context, `in`.jyotirmoy.attendx.notification.AttendanceActionReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("notificationId", scheduleId)
            putExtra("action", "ATTENDED")
        }
        val attendedPendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId * 10 + 1,
            attendedIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val missedIntent = Intent(context, `in`.jyotirmoy.attendx.notification.AttendanceActionReceiver::class.java).apply {
            putExtra("subjectId", subjectId)
            putExtra("notificationId", scheduleId)
            putExtra("action", "MISSED")
        }
        val missedPendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId * 10 + 2,
            missedIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(notificationSmallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Attended", attendedPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Missed", missedPendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String,
        descriptionText: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val group = NotificationChannelGroup("attendx_notifications_group", "AttendX Notifications")
        notificationManager.createNotificationChannelGroup(group)

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            this.group = "attendx_notifications_group"
        }
        notificationManager.createNotificationChannel(channel)
    }
}