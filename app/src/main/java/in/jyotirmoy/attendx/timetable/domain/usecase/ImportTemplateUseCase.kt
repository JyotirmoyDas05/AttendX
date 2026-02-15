package `in`.jyotirmoy.attendx.timetable.domain.usecase

import `in`.jyotirmoy.attendx.core.data.database.SubjectDao
import `in`.jyotirmoy.attendx.core.data.model.SubjectEntity
import `in`.jyotirmoy.attendx.timetable.data.dao.TimeTableDao
import `in`.jyotirmoy.attendx.timetable.data.model.TimeTableScheduleEntity
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateClassEntry
import `in`.jyotirmoy.attendx.timetable.data.model.community.TemplateSubjectEntry
import `in`.jyotirmoy.attendx.timetable.domain.repository.TemplateRepository
import javax.inject.Inject

class ImportTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository,
    private val timeTableDao: TimeTableDao,
    private val subjectDao: SubjectDao
) {
    suspend operator fun invoke(
        templateId: String,
        subjectsToImport: List<TemplateSubjectEntry>,
        classesToImport: List<TemplateClassEntry>
    ): Result<Unit> {
        return try {
            // 1. Process Subjects (Metadata)
            subjectsToImport.forEach { subjectEntry ->
                if (subjectDao.getSubjectIdByName(subjectEntry.name) == null) {
                    val newSubject = SubjectEntity(
                        subject = subjectEntry.name,
                        subjectCode = subjectEntry.code,
                        histogramLabel = subjectEntry.histogramLabel,
                        targetPercentage = 75.0f
                    )
                    subjectDao.insertSubject(newSubject)
                }
            }
            
            // 2. Process each class
            classesToImport.forEach { classEntry ->
                
                // 3. Find or Create Subject
                // Check if subject exists by name (case-insensitive)
                var subjectId = subjectDao.getSubjectIdByName(classEntry.subject)
                
                if (subjectId == null) {
                    // Create new subject
                    val newSubject = SubjectEntity(
                        subject = classEntry.subject,
                        targetPercentage = 75.0f // default
                    )
                    subjectId = subjectDao.insertSubject(newSubject).toInt()
                }

                // 4. Create Schedule Entry
                val schedule = TimeTableScheduleEntity(
                    subjectId = subjectId,
                    dayOfWeek = classEntry.dayOfWeek,
                    startTime = classEntry.startTime,
                    endTime = classEntry.endTime,
                    room = classEntry.room,
                    classType = classEntry.type
                )
                
                timeTableDao.insertClass(schedule)
            }
            
            // 5. Update download count
            repository.incrementDownloads(templateId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
