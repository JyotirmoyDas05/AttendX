package `in`.jyotirmoy.attendx.settings.domain.model

data class NotificationState(
    val enableNotifications: Boolean,
    val markAttendance: Boolean,
    val missedAttendance: Boolean,
    val updateAvailable: Boolean
)
