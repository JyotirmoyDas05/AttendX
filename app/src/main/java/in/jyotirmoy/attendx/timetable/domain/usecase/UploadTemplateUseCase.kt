package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import javax.inject.Inject

class UploadTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(
        college: String,
        department: String,
        semester: Int,
        section: String,
        academicYear: String,
        authorId: String,
        authorName: String,
        classes: List<TemplateClassEntry>,
        subjects: List<TemplateSubjectEntry>
    ): Result<String> {
        return try {
            val generatedName = "$college - $department Sem $semester"

            val template = CommunityTemplate(
                name = generatedName,
                college = college,
                department = department,
                semester = semester,
                section = section,
                academicYear = academicYear,
                authorId = authorId,
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
