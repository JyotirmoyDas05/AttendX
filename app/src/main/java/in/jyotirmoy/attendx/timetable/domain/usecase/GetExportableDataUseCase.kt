package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.core.domain.repository.SubjectRepository
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TimeTableRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetExportableDataUseCase @Inject constructor(
    private val timeTableRepository: TimeTableRepository,
    private val subjectRepository: SubjectRepository
) {
    suspend operator fun invoke(): Pair<List<TemplateSubjectEntry>, List<TemplateClassEntry>> {
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

        return Pair(subjects, classes)
    }
}
