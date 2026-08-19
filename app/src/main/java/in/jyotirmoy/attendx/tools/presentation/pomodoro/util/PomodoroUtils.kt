package `in`.jyotirmoy.attendx.tools.presentation.pomodoro.util

import android.provider.Settings
import java.util.Locale
import java.util.concurrent.TimeUnit

fun millisecondsToStr(t: Long): String {
    return String.format(
        Locale.getDefault(),
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(t),
        TimeUnit.MILLISECONDS.toSeconds(t) % TimeUnit.MINUTES.toSeconds(1)
    )
}

fun millisecondsToHours(t: Long, format: String = "%dh"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t)
    )
}

fun millisecondsToMinutes(t: Long, format: String = "%dm"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toMinutes(t)
    )
}

fun millisecondsToHoursMinutes(t: Long, format: String = "%1\$dh %2\$dm"): String {
    return String.format(
        Locale.getDefault(),
        format,
        TimeUnit.MILLISECONDS.toHours(t),
        TimeUnit.MILLISECONDS.toMinutes(t) % TimeUnit.HOURS.toMinutes(1)
    )
}

fun getDefaultAlarmTone(): String? =
    (Settings.System.DEFAULT_ALARM_ALERT_URI ?: Settings.System.DEFAULT_RINGTONE_URI)?.toString()
