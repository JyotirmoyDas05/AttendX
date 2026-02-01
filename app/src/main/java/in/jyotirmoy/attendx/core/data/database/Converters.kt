package `in`.jyotirmoy.attendx.core.data.database

import androidx.room.TypeConverter
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    @JvmStatic
    fun toStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
}
