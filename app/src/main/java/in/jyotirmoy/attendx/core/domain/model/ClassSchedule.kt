package `in`.jyotirmoy.attendx.core.domain.model

import `in`.jyotirmoy.attendx.core.data.model.ClassScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity

data class ClassSchedule(
    val id: Int = 0,
    val subjectId: Int,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val location: String? = null,
    val isEnabled: Boolean = true
) {
    fun toEntity(): ClassScheduleEntity {
        return ClassScheduleEntity(
            id = id,
            subjectId = subjectId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            location = location,
            isEnabled = isEnabled
        )
    }

    // converts HH:mm to minutes from midnight
    fun toTimeTableEntity(): TimeTableScheduleEntity {
        return TimeTableScheduleEntity(
            id = id,
            subjectId = subjectId,
            dayOfWeek = dayOfWeek,
            startTime = parseTimeToMinutes(startTime),
            endTime = parseTimeToMinutes(endTime),
            room = location,
            isActive = isEnabled
        )
    }

    fun getDayName(): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Unknown"
        }
    }

    fun getFormattedTimeRange(): String {
        return "$startTime - $endTime"
    }
}

fun ClassScheduleEntity.toDomain(): ClassSchedule {
    return ClassSchedule(
        id = id,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        location = location,
        isEnabled = isEnabled
    )
}

// converts TimeTableScheduleEntity to ClassSchedule
fun TimeTableScheduleEntity.toClassSchedule(): ClassSchedule {
    return ClassSchedule(
        id = id,
        subjectId = subjectId,
        dayOfWeek = dayOfWeek,
        startTime = minutesToTimeString(startTime),
        endTime = minutesToTimeString(endTime),
        location = room,
        isEnabled = isActive
    )
}

// HH:mm -> minutes from midnight
private fun parseTimeToMinutes(time: String): Long {
    val parts = time.split(":")
    if (parts.size != 2) return 0L
    val hours = parts[0].toIntOrNull() ?: 0
    val minutes = parts[1].toIntOrNull() ?: 0
    return (hours * 60 + minutes).toLong()
}

// minutes from midnight -> HH:mm
private fun minutesToTimeString(minutes: Long): String {
    val hours = (minutes / 60).toInt()
    val mins = (minutes % 60).toInt()
    return "%02d:%02d".format(hours, mins)
}
