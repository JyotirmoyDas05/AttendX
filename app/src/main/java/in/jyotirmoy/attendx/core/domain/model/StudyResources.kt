package `in`.jyotirmoy.attendx.core.domain.model

enum class MaterialType {
    PDF,
    LINK,
    IMAGE,
    DOC,
    AUDIO,
    VIDEO
}

data class CourseMaterial(
    val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val type: MaterialType,
    val uriOrUrl: String,
    val mimeType: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

data class SubjectTask(
    val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val isNote: Boolean = false,
    val noteType: String? = null, // "Lecture", "Review", "Lab", "Assignment", etc.
    val reminderMinutes: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
