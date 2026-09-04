package `in`.jyotirmoy.attendx.timetable.data.model.community

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

@Keep
data class CommunityTemplate(
    val id: String = "",
    val name: String = "",
    val college: String = "",
    val department: String = "",
    val semester: Int = 1,
    val section: String = "",
    val academicYear: String = "",
    val authorId: String = "",
    val authorName: String = DEFAULT_AUTHOR_NAME,
    val likes: Int = 0,
    val downloads: Int = 0,
    val classes: List<TemplateClassEntry> = emptyList(),
    val subjects: List<TemplateSubjectEntry> = emptyList(),
    // Server-authored: the security rules require createdAt/updatedAt == request.time,
    // which a client clock can never satisfy. @ServerTimestamp substitutes a server
    // value only while the field is null, so reads come back populated as normal.
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    // Bumped on every author edit. Templates published before this feature have no
    // version field and deserialize to 1; the rules default to 0 for those so their
    // first edit still lands.
    val version: Int = 1
) {
    companion object {
        const val DEFAULT_AUTHOR_NAME = "Anonymous Student"
    }
}

@Keep
data class TemplateClassEntry(
    val dayOfWeek: Int = 1,
    val startTime: Long = 0,
    val endTime: Long = 0,
    val subject: String = "",
    val room: String? = null,
    val type: String = "Lecture"
)

@Keep
data class TemplateSubjectEntry(
    val name: String = "",
    val code: String? = null,
    val histogramLabel: String? = null
)
