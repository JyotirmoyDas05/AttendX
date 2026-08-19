package `in`.jyotirmoy.attendx.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import `in`.jyotirmoy.attendx.core.domain.model.SubjectTask
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "subject_tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class SubjectTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val priority: Int = 0,
    val isNote: Boolean = false,
    val noteType: String? = null,
    val reminderMinutes: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

fun SubjectTaskEntity.toDomain(): SubjectTask = SubjectTask(
    id = id,
    subjectId = subjectId,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDate = dueDate,
    priority = priority,
    isNote = isNote,
    noteType = noteType,
    reminderMinutes = reminderMinutes,
    createdAt = createdAt
)

fun SubjectTask.toEntity(): SubjectTaskEntity = SubjectTaskEntity(
    id = id,
    subjectId = subjectId,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDate = dueDate,
    priority = priority,
    isNote = isNote,
    noteType = noteType,
    reminderMinutes = reminderMinutes,
    createdAt = createdAt
)
