package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.timetable.data.model.community.CommunityTemplate
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UploadTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val timeTableRepository: TimeTableRepository,
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(
        college: String,
        department: String,
        semester: Int,
        section: String,
        academicYear: String,
        authorId: String,
        authorName: String
    ): Result<String> {
        return try {
            val classes = timeTableRepository.getAllClassesWithSubject().first().map {
                TemplateClassEntry(
                    dayOfWeek = it.schedule.dayOfWeek,
                    startTime = it.schedule.startTime,
                    endTime = it.schedule.endTime,
                    subject = it.subject.subject,
                    room = it.schedule.room,
                    type = it.schedule.classType
                )
            }

            val subjects = subjectRepository.getAllSubjectsOnce().map { subject ->
                TemplateSubjectEntry(
                    name = subject.subject,
                    code = subject.subjectCode,
                    histogramLabel = subject.histogramLabel
                )
            }

            val template = CommunityTemplate(
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
