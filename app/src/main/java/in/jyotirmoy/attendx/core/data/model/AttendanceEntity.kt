package `in`.jyotirmoy.attendx.core.data.model

import androidx.room.Entity
import `in`.jyotirmoy.attendx.core.domain.model.AttendanceStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "attendance",
    primaryKeys = ["subjectId", "date"]
)
data class AttendanceEntity(
    val subjectId: Int,
    val date: String,
    val status: AttendanceStatus = AttendanceStatus.UNMARKED
)