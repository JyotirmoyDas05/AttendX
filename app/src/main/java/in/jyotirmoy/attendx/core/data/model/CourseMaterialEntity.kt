package `in`.jyotirmoy.attendx.core.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import `in`.jyotirmoy.attendx.core.domain.model.CourseMaterial
import `in`.jyotirmoy.attendx.core.domain.model.MaterialType
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "course_materials",
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
data class CourseMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val type: String, // Stored as MaterialType enum name
    val uriOrUrl: String,
    val mimeType: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

fun CourseMaterialEntity.toDomain(): CourseMaterial = CourseMaterial(
    id = id,
    subjectId = subjectId,
    title = title,
    type = try {
        MaterialType.valueOf(type)
    } catch (_: Exception) {
        MaterialType.DOC
    },
    uriOrUrl = uriOrUrl,
    mimeType = mimeType,
    addedAt = addedAt
)

fun CourseMaterial.toEntity(): CourseMaterialEntity = CourseMaterialEntity(
    id = id,
    subjectId = subjectId,
    title = title,
    type = type.name,
    uriOrUrl = uriOrUrl,
    mimeType = mimeType,
    addedAt = addedAt
)
