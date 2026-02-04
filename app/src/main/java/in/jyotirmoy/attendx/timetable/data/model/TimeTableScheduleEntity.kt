package `in`.jyotirmoy.attendx.timetable.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "class_schedule",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["dayOfWeek", "startTime"])
    ]
)
data class TimeTableScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val dayOfWeek: Int, // 1=Monday, 7=Sunday
    val startTime: Long, // Minutes from midnight (e.g., 600 = 10:00 AM)
    val endTime: Long,
    val room: String? = null,
    val professor: String? = null,
    val classType: String = "Lecture", // Lecture, Lab, Tutorial, etc.
    val isActive: Boolean = true,
    val weekPattern: String = "all", // "all", "odd", "even"
    val notes: String? = null,
    val colorHex: String? = null // Optional override
)
