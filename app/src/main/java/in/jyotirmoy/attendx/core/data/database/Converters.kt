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

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(value: String?): java.time.LocalDate? = value?.let { java.time.LocalDate.parse(it) }

    @TypeConverter
    @JvmStatic
    fun localDateToString(date: java.time.LocalDate?): String? = date?.toString()
}
