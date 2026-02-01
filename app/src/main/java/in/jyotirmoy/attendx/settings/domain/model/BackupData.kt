package `in`.jyotirmoy.attendx.settings.domain.model

import `in`.jyotirmoy.attendx.core.data.model.AttendanceEntity
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.core.data.model.ClassScheduleEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val settings: Map<String, String?>? = null,
    val attendance: List<AttendanceEntity>? = null,
    val subjects: List<SubjectEntity>? = null,
    val classSchedules: List<ClassScheduleEntity>? = null,
    val backupTime: String
)
