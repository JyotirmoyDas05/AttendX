package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import javax.inject.Inject

/** Display name for a template, derived rather than asked for. */
internal fun templateDisplayName(college: String, department: String, semester: Int) =
    "$college - $department Sem $semester"

class UploadTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    // authorId is deliberately absent: the repository stamps it from the signed-in
    // uid, so a caller cannot publish under someone else's identity.
    suspend operator fun invoke(
        college: String,
        department: String,
        semester: Int,
        section: String,
        academicYear: String,
        authorName: String,
        classes: List<TemplateClassEntry>,
        subjects: List<TemplateSubjectEntry>
    ): Result<String> {
        return try {
            val template = CommunityTemplate(
                name = templateDisplayName(college, department, semester),
                college = college,
                department = department,
                semester = semester,
                section = section,
                academicYear = academicYear,
                authorName = authorName,
                classes = classes,
                subjects = subjects
            )

            templateRepository.uploadTemplate(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
