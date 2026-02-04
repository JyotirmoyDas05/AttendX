package `in`.jyotirmoy.attendx.timetable.presentation.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import `in`.jyotirmoy.attendx.notification.TimetableAlarmReceiver
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class TimeTableNotificationScheduler @Inject constructor() {

    companion object {
        private const val TAG = "TimeTableScheduler"
        private const val REMINDER_OFFSET_MINUTES = 10L
    }

    fun scheduleClassAlarm(context: Context, schedule: TimeTableScheduleEntity) {
        if (!schedule.isActive) return
        
        // Check week pattern
        if (!matchesCurrentWeek(schedule.weekPattern)) {
            Log.d(TAG, "Skipping schedule ${schedule.id} - week pattern doesn't match")
            return
        }

        val triggerTime = calculateTriggerTime(schedule.dayOfWeek, schedule.startTime) ?: return
        scheduleAlarmExact(context, schedule, triggerTime)
    }

    private fun matchesCurrentWeek(pattern: String): Boolean {
        val weekOfYear = LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        val currentWeekType = if (weekOfYear % 2 == 1) "odd" else "even"
        return when (pattern) {
            "odd" -> currentWeekType == "odd"
            "even" -> currentWeekType == "even"
            else -> true
        }
    }

    private fun calculateTriggerTime(dayOfWeek: Int, startMinutes: Long): Long? {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val today = LocalDate.now()
        val targetDay = DayOfWeek.of(dayOfWeek)
        
        val time = LocalTime.of((startMinutes / 60).toInt(), (startMinutes % 60).toInt())
        val notifyTime = time.minusMinutes(REMINDER_OFFSET_MINUTES)
        
        var targetDate = today.with(TemporalAdjusters.nextOrSame(targetDay))
        var targetDateTime = ZonedDateTime.of(targetDate, notifyTime, ZoneId.systemDefault())

        if (targetDateTime.isBefore(now)) {
            targetDate = today.with(TemporalAdjusters.next(targetDay))
            targetDateTime = ZonedDateTime.of(targetDate, notifyTime, ZoneId.systemDefault())
        }
        
        return targetDateTime.toInstant().toEpochMilli()
    }

    private fun scheduleAlarmExact(context: Context, schedule: TimeTableScheduleEntity, triggerMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Cannot schedule exact alarms")
            return
        }

        val intent = Intent(context, TimetableAlarmReceiver::class.java).apply {
            putExtra("scheduleId", schedule.id)
            putExtra("subjectId", schedule.subjectId)
            putExtra("startTime", formatTime(schedule.startTime))
            putExtra("room", schedule.room)
            putExtra("type", "START")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
            Log.d(TAG, "Scheduled alarm for schedule ${schedule.id} at $triggerMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to set alarm", e)
        }
    }

    private fun formatTime(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }
}
