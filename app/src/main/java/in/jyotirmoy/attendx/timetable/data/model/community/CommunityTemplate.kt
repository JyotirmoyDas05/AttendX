package `in`.jyotirmoy.attendx.timetable.data.model.community

import com.google.firebase.Timestamp

data class CommunityTemplate(
    val id: String = "",
    val name: String = "",
    val college: String = "",
    val department: String = "",
    val semester: Int = 1,
    val section: String = "",
    val academicYear: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val likes: Int = 0,
    val downloads: Int = 0,
    val classes: List<TemplateClassEntry> = emptyList(),
    val subjects: List<TemplateSubjectEntry> = emptyList(),
    val createdAt: Timestamp = Timestamp.now()
)

data class TemplateClassEntry(
    val dayOfWeek: Int = 1,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val subject: String = "",
    val room: String? = null,
    val type: String = "Lecture"
)

data class TemplateSubjectEntry(
    val name: String = "",
    val code: String? = null,
    val histogramLabel: String? = null
)
