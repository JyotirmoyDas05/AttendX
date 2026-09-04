package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import javax.inject.Inject

/**
 * Republishes an already-shared template. Only the author's own uid can satisfy
 * the update rule, so ownership is enforced server-side rather than here.
 */
class UpdateTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(
        templateId: String,
        college: String,
        department: String,
        semester: Int,
        section: String,
        academicYear: String,
        authorName: String,
        classes: List<TemplateClassEntry>,
        subjects: List<TemplateSubjectEntry>
    ): Result<Unit> {
        return try {
            val template = CommunityTemplate(
                id = templateId,
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

            templateRepository.updateTemplate(templateId, template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
